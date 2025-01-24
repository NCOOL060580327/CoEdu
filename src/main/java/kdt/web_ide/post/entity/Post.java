package kdt.web_ide.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Integer boardId; // 게시판 고유 ID

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
