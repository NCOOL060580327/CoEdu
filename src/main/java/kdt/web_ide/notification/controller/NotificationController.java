package kdt.web_ide.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kdt.web_ide.boards.service.BoardService;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.notification.dto.NotificationResponseDto;
import kdt.web_ide.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API")
public class NotificationController {

  private final BoardService boardService;
  private final NotificationService notificationService;

  @PostMapping(value = "/{notificationId}/accept", produces = "application/json; charset=UTF-8")
  @Operation(summary = "알림 수락 API", description = "알림을 수락합니다.")
  public ResponseEntity<String> acceptInvitation(
      @PathVariable Long notificationId,
      @RequestParam Long boardId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    boardService.acceptInvitation(notificationId, boardId, userDetails.getMember());
    notificationService.sendInvitationAcceptedNotification(
        userDetails.getMember(), boardService.getBoardDetails(boardId).getTitle());
    return ResponseEntity.ok("초대를 수락하였습니다.");
  }

  @PostMapping(value = "/{notificationId}/reject", produces = "application/json; charset=UTF-8")
  @Operation(summary = "알림 거절 API", description = "알림을 거절합니다.")
  public ResponseEntity<String> rejectInvitation(
      @PathVariable Long notificationId,
      @RequestParam Long boardId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    boardService.rejectInvitation(notificationId, boardId, userDetails.getMember());
    notificationService.sendInvitationRejectedNotification(
        userDetails.getMember(), boardService.getBoardDetails(boardId).getTitle());
    return ResponseEntity.ok("초대를 거절하였습니다.");
  }

  @GetMapping
  @Operation(summary = "알림 조회 API", description = "사용자의 알림을 조회합니다.")
  public ResponseEntity<List<NotificationResponseDto>> getUserNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<NotificationResponseDto> notifications =
        notificationService.getUserNotifications(userDetails.getMember());
    return ResponseEntity.ok(notifications);
  }
}
