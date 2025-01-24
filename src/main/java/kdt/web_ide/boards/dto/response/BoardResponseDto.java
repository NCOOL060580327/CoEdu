package kdt.web_ide.boards.dto.response;

import kdt.web_ide.boards.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BoardResponseDto {

    private Long boardId;
    private String title;
    private int userCount;

    private LocalDateTime createdDate;
    private LocalDateTime modifedDate;

    public BoardResponseDto(Board board){
        this.boardId = board.getId();
        this.title = board.getTitle();
        this.userCount = board.getUserCount();
        this.createdDate = board.getCreatedDate();
        this.modifedDate = board.getModifiedDate();
    }
}
