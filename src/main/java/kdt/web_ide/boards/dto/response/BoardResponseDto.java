package kdt.web_ide.boards.service;

import kdt.web_ide.boards.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardReponseDto {

    private Long boardId;
    private String title;
    private int userCount;

    public BoardReponseDto(Board board){
        this.boardId = board.getId();
        this.title = board.getTitle();
        this.userCount = board.getUserCount();
    }
}
