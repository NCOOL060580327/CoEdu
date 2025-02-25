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

  public void sendNotification(Member member, NotificationType type, String message) {
    Notification notification =
        Notification.builder()
            .member(member)
            .type(type)
            .message(message)
            .createdAt(LocalDateTime.now())
            .build();

    notificationRepository.save(notification);
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
}
