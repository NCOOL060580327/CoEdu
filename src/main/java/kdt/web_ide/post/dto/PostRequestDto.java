package kdt.web_ide.post.dto;

import kdt.web_ide.post.entity.Language;
import lombok.Data;

@Data
public class PostRequestDto {
  private Long boardId; // 게시판 ID
  private String name; // 게시글 이름
  private Language language; // 언어 종류
}
