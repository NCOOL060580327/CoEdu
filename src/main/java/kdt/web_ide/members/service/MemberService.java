package kdt.web_ide.members.service;

import java.io.IOException;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.dto.response.LoginResponseDto;
import kdt.web_ide.members.dto.response.MemberResponse;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import kdt.web_ide.members.kakao.KakaoReissueParams;
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

  private final S3Uploader s3Uploader;

  public LoginResponseDto login(OAuthLoginParams params) {
    OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);

    Long memberId = findOrCreateMember(oAuthInfoResponse);

    String accessToken = jwtProvider.generateAccessToken(memberId);
    String refreshToken = jwtProvider.generateRefreshToken(memberId);

    Member member = findMember(memberId);
    member.setRefreshToken(refreshToken);
    memberRepository.save(member);

    return LoginResponseDto.builder()
        .member(member)
        .tokenResponse(new TokenResponse(accessToken, refreshToken))
        .build();
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

  public TokenResponse reissue(KakaoReissueParams params) {

    String refreshToken = params.getRefreshToken();

    Long memberId = jwtProvider.parseRefreshToken(refreshToken);

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!refreshToken.equals(member.getRefreshToken())) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    String newAccessToken = jwtProvider.generateAccessToken(member.getMemberId());
    String newRefreshToken = jwtProvider.generateRefreshToken(member.getMemberId());

    member.setRefreshToken(newRefreshToken);

    return new TokenResponse(newAccessToken, newRefreshToken);
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
}
