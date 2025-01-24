package kdt.web_ide.boards.dto.request;

import kdt.web_ide.boards.entity.Board;
import kdt.web_ide.boards.entity.BoardUser;
import kdt.web_ide.members.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardUserInviteRequestDto {
    private String loginId;

    public BoardUser toEntity(Board board, Member member) {
        return BoardUser.builder()
                .board(board)
                .isLeader(false)
                .member(member)
                .build();
    }
}
