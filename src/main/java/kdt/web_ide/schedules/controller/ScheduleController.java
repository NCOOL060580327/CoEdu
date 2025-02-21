package kdt.web_ide.schedules.controller;

import java.time.LocalDate;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.schedules.dto.request.ScheduleSaveRequestDto;
import kdt.web_ide.schedules.dto.request.ScheduleUpdateRequestDto;
import kdt.web_ide.schedules.dto.response.ScheduleResponseDto;
import kdt.web_ide.schedules.service.ScheduleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "일정 API")
public class ScheduleController {

  private final ScheduleService scheduleService;

  // 일정 생성
  @PostMapping("/{boardId}/schedules")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "일정 생성",
      description = "게시판 ID, 카카오맵 장소 정보, 초대할 멤버의 식별코드 리스트 기반으로 일정을 생성합니다.")
  public ResponseEntity<?> saveSchedule(
      @PathVariable Long boardId,
      @RequestBody @Valid ScheduleSaveRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Member creator = userDetails.getMember();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(scheduleService.saveSchedule(requestDto, boardId, creator));
  }

  // 일정 조회
  @GetMapping("/boards/{boardId}/schedules")
  @Operation(summary = "게시판 일정 목록 조회", description = "게시판 ID 기반으로 일정 목록을 조회합니다.")
  public ResponseEntity<?> findByBoardId(
      @PathVariable Long boardId,
      @RequestParam LocalDate date,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Member member = userDetails.getMember();
    return ResponseEntity.status(HttpStatus.OK)
        .body(scheduleService.findByBoardIdAndDate(boardId, date));
  }

  // 일정 상세 조회
  @GetMapping("/schedules/{scheduleId}")
  @Operation(summary = "일정 상세 조회", description = "일정 ID에 해당하는 일정을 상세 조회합니다.")
  public ResponseEntity<?> findByScheduleId(@PathVariable Long scheduleId) {
    return ResponseEntity.status(HttpStatus.OK).body(scheduleService.findByScheduleId(scheduleId));
  }

  // 일정 삭제
  @DeleteMapping("/schedules/{scheduleId}")
  @Operation(summary = "일정 삭제", description = "일정 ID에 해당하는 일정을 삭제합니다.")
  public ResponseEntity<?> deleteSchedule(
      @PathVariable Long scheduleId, @AuthenticationPrincipal CustomUserDetails userDetail) {
    Member currentMember = userDetail.getMember();
    scheduleService.deleteById(scheduleId, currentMember);
    return ResponseEntity.ok("Schedule deleted successfully");
  }

  // 일정 수정
  @PutMapping("/schedules/{scheduleId}")
  @Operation(summary = "일정 수정", description = "일정 ID에 해당하는 일정을 수정합니다. 식별 코드로 멤버를 추가로 초대합니다.")
  public ResponseEntity<?> updateSchedule(
      @PathVariable Long scheduleId,
      @RequestBody @Valid ScheduleUpdateRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Member currentMember = userDetails.getMember();
    ScheduleResponseDto scheduleResponse =
        scheduleService.updateSchedule(requestDto, scheduleId, currentMember);
    return ResponseEntity.status(HttpStatus.OK).body(scheduleResponse);
  }

  @DeleteMapping("/schedules/{scheduleId}/members/{memberId}")
  @Operation(summary = "멤버 삭제", description = "일정 ID에 해당하는 일정의 멤버를 삭제합니다.")
  public ResponseEntity<?> deleteMemberById(
      @PathVariable Long scheduleId,
      @PathVariable Long memberId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Member currentMember = userDetails.getMember();
    scheduleService.removeMemberFromSchedule(scheduleId, memberId, currentMember);
    return ResponseEntity.ok("Member deleted successfully ");
  }

  // 카카오톡 일정 추가
  @PostMapping("/schedules/{scheduleId}/kakao")
  @Operation(summary = "카카오톡 일정 추가", description = "일정 ID에 해당하는 카카오톡 일정을 추가합니다.")
  public ResponseEntity<?> registerKakaoSchedule(
      @PathVariable Long scheduleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Member member = userDetails.getMember();
    scheduleService.registerKakaoSchedule(scheduleId, member);
    return ResponseEntity.ok("Schedule added to kakao calendar successfully");
  }
}
