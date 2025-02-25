package kdt.web_ide.notification.dto;

import java.time.LocalDateTime;

import kdt.web_ide.notification.entity.Notification;
import kdt.web_ide.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

  private Long id;
  private NotificationType type;
  private String message;
  private boolean isRead;
  private LocalDateTime createdAt;

  public static NotificationResponseDto fromEntity(Notification notification) {
    return NotificationResponseDto.builder()
        .id(notification.getId())
        .type(notification.getType())
        .message(notification.getMessage())
        .isRead(notification.isRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
