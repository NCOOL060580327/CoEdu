package kdt.web_ide.members.controller;

import kdt.web_ide.members.dto.request.JoinRequestDto;
import kdt.web_ide.members.dto.request.LoginRequestDto;
import kdt.web_ide.members.dto.response.LoginResponseDto;
import kdt.web_ide.members.dto.response.MemberResponse;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.members.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody JoinRequestDto joinRequestDto){
        MemberResponse responseDto = memberService.signUp(joinRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto){
        LoginResponseDto memberResponse = memberService.login(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(memberResponse);
    }

    // 회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getMember(@AuthenticationPrincipal CustomUserDetails userDetails){
        MemberResponse memberResponse = memberService.getMember(userDetails.getMember());
        return  ResponseEntity.status(HttpStatus.OK).body(memberResponse);
    }


    // 회원탈퇴
}
