package kdt.web_ide.members.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.dto.request.TestSignUpRequest;
import kdt.web_ide.members.dto.response.*;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import kdt.web_ide.members.kakao.KakaoReissueParams;
import kdt.web_ide.members.kakao.KakaoToken;
import kdt.web_ide.members.oAuth.OAuthInfoResponse;
import kdt.web_ide.members.oAuth.OAuthLoginParams;
import kdt.web_ide.members.oAuth.RequestOAuthInfoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final RequestOAuthInfoService requestOAuthInfoService;
  private final JwtProvider jwtProvider;
  private final String DEFAULT_PROFILE_IMAGE_URL =
      "https://ide-project-bucket.s3.ap-northeast-2.amazonaws.com/profile-image/4510b03e-aded-43f1-b063-ccda7c734681_79516d5a-bdb1-4fbd-918e-6c56a38705c75070529700289430514_코에듀_기본_프로필.png";

  private final S3Uploader s3Uploader;

  private final ConcurrentHashMap<Long, String> kakaoAccessTokenCache = new ConcurrentHashMap<>();

  public LoginResponseWithToken testLogin(TestSignUpRequest request) {
    Member member =
        memberRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String accessToken = jwtProvider.generateAccessToken(member.getMemberId());
    String refreshToken = jwtProvider.generateRefreshToken(member.getMemberId());

    return new LoginResponseWithToken(
        LoginResponseDto.builder()
            .member(member)
            .tokenResponse(new TokenResponse(accessToken))
            .build(),
        refreshToken);
  }

  public LoginResponseWithToken login(OAuthLoginParams params) {

    KakaoToken kakaoToken = requestOAuthInfoService.login(params);

    OAuthInfoResponse oAuthInfoResponse =
        requestOAuthInfoService.request(kakaoToken.getAccessToken());

    Long memberId = findOrCreateMember(oAuthInfoResponse);

    String accessToken = jwtProvider.generateAccessToken(memberId);
    String refreshToken = jwtProvider.generateRefreshToken(memberId);

    kakaoAccessTokenCache.put(memberId, kakaoToken.getAccessToken());
    scheduleTokenExpiration(memberId, Long.parseLong(kakaoToken.getExpiresIn()), TimeUnit.SECONDS);

    Member member = findMember(memberId);
    member.setRefreshToken(refreshToken);
    member.setKakaoRefreshToken(kakaoToken.getRefreshToken());
    memberRepository.save(member);

    return new LoginResponseWithToken(
        LoginResponseDto.builder()
            .member(member)
            .tokenResponse(new TokenResponse(accessToken))
            .build(),
        refreshToken);
  }

  public Long findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
    return memberRepository
        .findByKakaoId(oAuthInfoResponse.getKakaoId())
        .map(Member::getMemberId)
        .orElseGet(() -> newUser(oAuthInfoResponse));
  }

  private Long newUser(OAuthInfoResponse oAuthInfoResponse) {
    return memberRepository
        .save(
            Member.builder()
                .kakaoId(oAuthInfoResponse.getKakaoId())
                .nickName(oAuthInfoResponse.getKakaoNickname())
                .identificationCode(
                    oAuthInfoResponse.getKakaoNickname()
                        + oAuthInfoResponse.getKakaoId().toString())
                .profileImage(oAuthInfoResponse.getKakaoProfileImage())
                .build())
        .getMemberId();
  }

  // 자체 refresh 토큰 발급
  public RefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {

    String refreshToken =
        jwtProvider
            .extractRefreshToken(request)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

    Long memberId = jwtProvider.parseRefreshToken(refreshToken);

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String newAccessToken = jwtProvider.generateAccessToken(memberId);
    String newRefreshToken = jwtProvider.generateRefreshToken(memberId);

    member.setRefreshToken(newRefreshToken);
    memberRepository.save(member);

    return new RefreshResponseDto(newAccessToken, newRefreshToken);
  }

  public Member findMember(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  // 멤버 정보 반환
  @Transactional
  public MemberResponse getMember(Member member) {
    MemberResponse memberResponse = new MemberResponse(member);
    return memberResponse;
  }

  //  프로필 이미지 수정
  @Transactional
  public MemberResponse updateProfileImage(MultipartFile image, Member member) throws IOException {
    if (image == null || image.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_IMAGE);
    }
    String uploadedUrl = s3Uploader.upload(image, "profile-image");
    member.setProfileImage(uploadedUrl);
    memberRepository.save(member);
    return new MemberResponse(member);
  }

  //  닉네임 수정
  @Transactional
  public MemberResponse updateNickName(String newNickName, Member member) {
    if (newNickName == null || newNickName.trim().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_NICKNAME);
    }
    if (memberRepository.findByNickName(newNickName).isPresent()) {
      throw new CustomException(ErrorCode.DUPLICATE_NAME);
    }
    member.setNickName(newNickName);
    memberRepository.save(member);
    return new MemberResponse(member);
  }

  private void scheduleTokenExpiration(Long memberId, long duration, TimeUnit unit) {
    Executors.newSingleThreadScheduledExecutor()
        .schedule(
            () -> {
              kakaoAccessTokenCache.remove(memberId);
            },
            duration,
            unit);
  }

  // 카카오 액세스 토큰 조회
  public TokenResponse getKakaoAccessToken(Long memberId) {

    if (kakaoAccessTokenCache.containsKey(memberId)) {
      return new TokenResponse(kakaoAccessTokenCache.get(memberId));
    }

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String kakaoRefreshToken =
        Optional.ofNullable(member.getKakaoRefreshToken())
            .orElseThrow(() -> new CustomException(ErrorCode.KAKAO_REFRESH_TOKEN_NOT_FOUND));

    KakaoReissueParams params = new KakaoReissueParams(kakaoRefreshToken);

    KakaoToken kakaoToken = requestOAuthInfoService.reissue(params);

    kakaoAccessTokenCache.put(memberId, kakaoToken.getAccessToken());
    scheduleTokenExpiration(memberId, Long.parseLong(kakaoToken.getExpiresIn()), TimeUnit.SECONDS);

    return new TokenResponse(kakaoToken.getAccessToken());
  }
}
