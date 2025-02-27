package kdt.web_ide.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.notification.dto.NotificationResponseDto;
import kdt.web_ide.notification.entity.Notification;
import kdt.web_ide.notification.entity.NotificationType;
import kdt.web_ide.notification.entity.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public void sendNotification(Member member, NotificationType type, String boardTitle) {

    boolean exists =
        notificationRepository.findByMemberOrderByCreatedAtDesc(member).stream()
            .anyMatch(
                notification ->
                    notification.getType() == type
                        && boardTitle.equals(notification.getBoardTitle()));

    if (exists) {
      return; // 동일한 알림이 있으면 새로 생성하지 않음
    }

    String message = generateMessage(type, boardTitle);

    Notification notification =
        Notification.builder()
            .member(member)
            .type(type)
            .message(message)
            .boardTitle(boardTitle)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();

    notificationRepository.save(notification);
  }

  private String generateMessage(NotificationType type, String boardTitle) {
    return switch (type) {
      case INVITATION -> boardTitle + " 교실에 초대 되었습니다.";
      case INVITATION_ACCEPTED -> boardTitle + " 교실 초대를 수락하였습니다.";
      case INVITATION_REJECTED -> boardTitle + " 교실 초대를 거절하였습니다.";
      case SCHEDULE_CREATED -> boardTitle + " 회의 일정이 생성되었습니다.";
    };
  }

  public List<NotificationResponseDto> getUserNotifications(Member member) {
    return notificationRepository.findByMemberOrderByCreatedAtDesc(member).stream()
        .map(NotificationResponseDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteNotification(Long notificationId, Member member) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getMember().getMemberId().equals(member.getMemberId())) {
      throw new CustomException(ErrorCode.NO_PERMISSION);
    }

    notificationRepository.delete(notification);
  }

  public void sendInvitationAcceptedNotification(Member member, String boardTitle) {
    sendNotification(member, NotificationType.INVITATION_ACCEPTED, boardTitle);
  }

  public void sendInvitationRejectedNotification(Member member, String boardTitle) {
    sendNotification(member, NotificationType.INVITATION_REJECTED, boardTitle);
  }
}
