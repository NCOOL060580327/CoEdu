package kdt.web_ide.schedules.entity;

import jakarta.persistence.*;

import kdt.web_ide.members.entity.Member;
import lombok.*;

@Entity
@Table(name = "schedule_members")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ScheduleMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "schedule_id")
  private Schedule schedule;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @Setter
  @Column(name = "event_id")
  private String eventId;
}
