package kdt.web_ide.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import kdt.web_ide.members.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  private NotificationType type; // INVITATION, SCHEDULE 등

  private String message;

  private boolean isRead = false;

  private LocalDateTime createdAt;
}
