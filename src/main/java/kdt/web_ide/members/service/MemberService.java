package kdt.web_ide.members.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.dto.request.CustomUserInfoDto;
import kdt.web_ide.members.dto.request.JoinRequestDto;
import kdt.web_ide.members.dto.request.LoginRequestDto;
import kdt.web_ide.members.dto.request.UserInfoUpdateRequestDto;
import kdt.web_ide.members.dto.response.LoginResponseDto;
import kdt.web_ide.members.dto.response.MemberResponse;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.RefreshToken;
import kdt.web_ide.members.entity.RoleType;
import kdt.web_ide.members.entity.repository.MemberRepository;
import kdt.web_ide.members.entity.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final RefreshTokenRepository refreshTokenRepository;

    private final S3Uploader s3Uploader;

    @Transactional
    public void saveRefreshToken(TokenResponse tokenDto) {
        RefreshToken refreshToken = RefreshToken.builder().keyUserId(tokenDto.getKey()).refreshToken(tokenDto.getRefreshToken()).build();
        String userId = refreshToken.getKeyUserId();

        if (refreshTokenRepository.existsByKeyUserId(userId)) {
            refreshTokenRepository.deleteByKeyUserId(userId);
        }
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken getRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
    }

    public String validateRefreshToken(String refreshToken) {
        RefreshToken getRefreshToken = getRefreshToken(refreshToken);
        String createdAccessToken = jwtProvider.validateRefreshToken(getRefreshToken);

        if (createdAccessToken == null) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        return createdAccessToken;
    }

    // 회원가입
    public MemberResponse signUp(@Valid JoinRequestDto joinRequest) throws CustomException {
        String loginId = joinRequest.getLoginId();
        String nickName = joinRequest.getNickName();
        String password = passwordEncoder.encode(joinRequest.getPassword());

        // 닉네임 중복 확인
        if (memberRepository.findByNickName(nickName).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NAME);
        }

        // 아이디 중복 확인
        if (memberRepository.findByLoginId(loginId).isPresent()) {
            throw new CustomException(ErrorCode.HAS_ID);
        }

        // 비밀번호 일치 확인
        if (password.equals(joinRequest.getPassword2())){
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        RoleType role = RoleType.USER;
        Member member = joinRequest.toEntity(role, password);

        // 기본 프로필 이미지 설정
        final String DEFAULT_PROFILE_IMAGE_URL = "https://ide-project-bucket.s3.ap-northeast-2.amazonaws.com/profile-image/4510b03e-aded-43f1-b063-ccda7c734681_79516d5a-bdb1-4fbd-918e-6c56a38705c75070529700289430514_코에듀_기본_프로필.png";
        member.updateImage(DEFAULT_PROFILE_IMAGE_URL);


        memberRepository.save(member); // 회원 정보 저장
        return new MemberResponse(member);
    }

    // 로그인
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) throws CustomException {
        String loginId = loginRequest.getLoginId();
        String password = loginRequest.getPassword();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGINID));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        CustomUserInfoDto userInfoDto = new CustomUserInfoDto(member.getMemberId(), member.getLoginId(), member.getRoles());
        TokenResponse tokenDto = jwtProvider.createTokenByLogin(userInfoDto);
        saveRefreshToken(tokenDto);
        return new LoginResponseDto(member,tokenDto);
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
        member.updateImage(uploadedUrl);

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

        member.updateNickName(newNickName);

        return new MemberResponse(member);
    }

    // 로그인 아이디 수정
    @Transactional
    public MemberResponse updateLoginId(String newLoginId, Member member) {
        if (newLoginId == null || newLoginId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_LOGINID);
        }

        if (memberRepository.findByLoginId(newLoginId).isPresent()) {
            throw new CustomException(ErrorCode.HAS_ID);
        }

        member.updateLoginId(newLoginId);

        return new MemberResponse(member);
    }


}