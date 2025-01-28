package kdt.web_ide.boards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kdt.web_ide.boards.entity.Board;
import kdt.web_ide.boards.entity.BoardUser;
import kdt.web_ide.members.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardSaveRequesetDto {

    @NotBlank
    private String title;

    @Size(min = 1, max = 5, message = "제목 텍스트는 1~5글자여야 합니다.")
    private String titleText;


    public Board toEntity() {
        return Board.builder()
                .title(title)
                .titleText(titleText)
                .build();
    }

    public BoardUser toEntity(Board board, Member member) {
        return BoardUser.builder()
                .board(board)
                .isLeader(true)
                .member(member)
                .build();
    }
}
