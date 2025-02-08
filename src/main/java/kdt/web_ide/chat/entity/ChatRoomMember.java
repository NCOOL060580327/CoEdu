package kdt.web_ide.chat.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.ColumnDefault;

import kdt.web_ide.members.entity.Member;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long ChatRoomMemberId;

  @Setter
  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer notReadCount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id")
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;
}
