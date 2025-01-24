package kdt.web_ide.boards.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    public Board toEntity() {
        return Board.builder()
                .title(title)
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
