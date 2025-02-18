package kdt.web_ide.members.controller;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kdt.web_ide.members.dto.request.TestSignUpRequest;
import kdt.web_ide.members.dto.response.LoginResponseWithToken;
import kdt.web_ide.members.dto.response.MemberResponse;
import kdt.web_ide.members.dto.response.RefreshResponseDto;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.kakao.KakaoLoginParams;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.members.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원 API")
@RequestMapping("/api/auth")
@Slf4j
public class MemberController {

  private final JobLauncher jobLauncher;
  private final Job tokenCleanupJob;

  private final MemberService memberService;

  @Operation(summary = "임시 로그인 API", description = "임시 로그인")
  @PostMapping("/testlogin")
  public ResponseEntity<?> testLogin(@RequestBody TestSignUpRequest request) {

    LoginResponseWithToken response = memberService.testLogin(request);

    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", response.refreshToken())
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(7))
            .build();

    return ResponseEntity.status(HttpStatus.OK)
        .header("Set-Cookie", refreshTokenCookie.toString())
        .body(response.responseDto());
  }

  // 로그인
  @Operation(summary = "카카오 로그인/회원가입 API", description = "카카오 로그인/회원가입")
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody KakaoLoginParams params) {

    LoginResponseWithToken response = memberService.login(params);

    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", response.refreshToken())
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(7))
            .build();

    return ResponseEntity.status(HttpStatus.OK)
        .header("Set-Cookie", refreshTokenCookie.toString())
        .body(response.responseDto());
  }

  // 회원 정보 조회
  @GetMapping("/profile")
  public ResponseEntity<?> getMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
    MemberResponse memberResponse = memberService.getMember(userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body(memberResponse);
  }

  // 프로필 이미지 수정
  @Operation(summary = "프로필 이미지 수정 API", description = "새로운 프로필 이미지 업로드")
  @PatchMapping(
      value = "/profile/profile-image",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<MemberResponse> updateProfileImage(
      @RequestPart("image") MultipartFile image,
      @AuthenticationPrincipal CustomUserDetails userDetails)
      throws IOException {
    MemberResponse updatedMember = memberService.updateProfileImage(image, userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body(updatedMember);
  }

  @Operation(summary = "닉네임 수정 API")
  @PatchMapping("/profile/nickname")
  public ResponseEntity<MemberResponse> updateNickName(
      @RequestParam("nickName") String newNickName,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MemberResponse updatedMember =
        memberService.updateNickName(newNickName, userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body(updatedMember);
  }

  @Operation(summary = "엑세스 토큰 재발급 API")
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> recreateAccessToken(
      HttpServletRequest request, HttpServletResponse response) {

    RefreshResponseDto responseDto = memberService.refresh(request, response);

    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", responseDto.refreshToken())
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(7))
            .build();

    return ResponseEntity.status(HttpStatus.OK)
        .header("Set-Cookie", refreshTokenCookie.toString())
        .body(new TokenResponse(responseDto.accessToken()));
  }

  @Operation(summary = "카카오 엑세스 토큰 재발급 API")
  @PostMapping("/kakao")
  public ResponseEntity<TokenResponse> getKakaoAccessToken(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(memberService.getKakaoAccessToken(userDetails.getMember().getMemberId()));
  }

  @Operation(summary = "블랙리스트 토큰 수동 삭제")
  @PostMapping("/cleanup")
  public ResponseEntity<String> runTokenCleanUp() {
    try {
      jobLauncher.run(tokenCleanupJob, new JobParameters());
      return ResponseEntity.ok("Token cleanup batch job started.");
    } catch (Exception e) {
      log.error("에러", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to start batch job.");
    }
  }
}
