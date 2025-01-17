package kdt.web_ide.members.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.dto.request.CustomUserInfoDto;
import kdt.web_ide.members.dto.request.JoinRequestDto;
import kdt.web_ide.members.dto.request.LoginRequestDto;
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

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final RefreshTokenRepository refreshTokenRepository;

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
        String email = joinRequest.getEmail();
        String name = joinRequest.getName();
        String password = passwordEncoder.encode(joinRequest.getPassword());

        // 닉네임 중복 확인
        if (memberRepository.findByName(name).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NAME);
        }

        // 이메일 중복 확인
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.HAS_EMAIL);
        }

        RoleType role = RoleType.USER;
        Member member = joinRequest.toEntity(role, password);

        // 기본 프로필 이미지 설정
        final String DEFAULT_PROFILE_IMAGE_URL = "https://yourdomain.com/images/default-profile.png";
        member.setProfileImage(DEFAULT_PROFILE_IMAGE_URL);

        memberRepository.save(member); // 회원 정보 저장
        return new MemberResponse(member);
    }

    // 로그인
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) throws CustomException {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        CustomUserInfoDto userInfoDto = new CustomUserInfoDto(member.getMemberId(), member.getEmail(), member.getRoles());
        TokenResponse tokenDto = jwtProvider.createTokenByLogin(userInfoDto);
        saveRefreshToken(tokenDto);
        return new LoginResponseDto(member,tokenDto);
    }


    // 멤버 정보 반환
    public MemberResponse getMember(Member member) {
        MemberResponse memberResponse = new MemberResponse(member);
        return memberResponse;
    }

}