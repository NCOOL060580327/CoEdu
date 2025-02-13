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
import kdt.web_ide.members.dto.response.MemberResponse;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.kakao.KakaoLoginParams;
import kdt.web_ide.members.kakao.KakaoReissueParams;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.members.service.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원 API")
@RequestMapping("/api/auth")
public class MemberController {

  private final MemberService memberService;

  // 로그인
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody KakaoLoginParams params) {
    return ResponseEntity.status(HttpStatus.OK).body(memberService.login(params));
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
  public ResponseEntity<TokenResponse> recreateAccessToken(@RequestBody KakaoReissueParams params) {
    return ResponseEntity.status(HttpStatus.OK).body(memberService.reissue(params));
  }
}
