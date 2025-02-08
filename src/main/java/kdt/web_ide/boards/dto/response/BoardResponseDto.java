package kdt.web_ide.boards.dto.response;

import java.time.LocalDateTime;

import kdt.web_ide.boards.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardResponseDto {

  private Long boardId;
  private String title;
  private int userCount;

  private LocalDateTime createdDate;
  private LocalDateTime modifedDate;

  public BoardResponseDto(Board board) {
    this.boardId = board.getId();
    this.title = board.getTitle();
    this.userCount = board.getUserCount();
    this.createdDate = board.getCreatedDate();
    this.modifedDate = board.getModifiedDate();
  }
}
