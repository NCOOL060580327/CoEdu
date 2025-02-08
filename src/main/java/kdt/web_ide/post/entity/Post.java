package kdt.web_ide.post.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import kdt.web_ide.boards.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id; // 게시글 고유 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id", nullable = false)
  private Board board; // 게시판과의 관계

  @Column(nullable = false)
  private String name; // 생성할 게시글의 이름

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Language language; // 파일의 언어 (Enum)

  @Column(nullable = false)
  private String filePath; // S3에 저장된 파일 경로

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 시간

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
