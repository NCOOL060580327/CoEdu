package kdt.web_ide.chat.entity;

import jakarta.persistence.*;

import kdt.web_ide.post.entity.Post;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Chat_Room")
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long chatRoomId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;
}
