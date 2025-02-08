package kdt.web_ide.members.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kdt.web_ide.members.dto.request.JoinRequestDto;
import kdt.web_ide.members.dto.request.LoginRequestDto;
import kdt.web_ide.members.dto.request.RefreshTokenRequestDto;
import kdt.web_ide.members.dto.response.AccessTokenResponseDto;
import kdt.web_ide.members.dto.response.LoginResponseDto;
import kdt.web_ide.members.dto.response.MemberResponse;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.members.service.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원 API")
@RequestMapping("/api/auth")
public class MemberController {

  private final MemberService memberService;

  // 회원가입
  @PostMapping("/join")
  public ResponseEntity<?> join(@RequestBody JoinRequestDto joinRequestDto) {
    MemberResponse responseDto = memberService.signUp(joinRequestDto);
    return ResponseEntity.status(HttpStatus.OK).body(responseDto);
  }

  // 로그인
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
    LoginResponseDto memberResponse = memberService.login(loginRequestDto);
    return ResponseEntity.status(HttpStatus.OK).body(memberResponse);
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

  @Operation(summary = "로그인 아이디 수정 API")
  @PatchMapping("/profile/login-id")
  public ResponseEntity<MemberResponse> updateLoginId(
      @RequestParam("loginId") String newLoginId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MemberResponse updatedMember = memberService.updateLoginId(newLoginId, userDetails.getMember());
    return ResponseEntity.ok(updatedMember);
  }

  @Operation(summary = "엑세스 토큰 재발급 API")
  @PostMapping("/refresh")
  public ResponseEntity<AccessTokenResponseDto> recreateAccessToken(
      @RequestBody RefreshTokenRequestDto requestDto) {
    String accessToken = memberService.validateRefreshToken(requestDto.getRefreshToken());
    AccessTokenResponseDto responseDto = new AccessTokenResponseDto(accessToken);
    return ResponseEntity.status(HttpStatus.OK).body(responseDto);
  }
}
