package kdt.web_ide.boards.dto.response;

import kdt.web_ide.boards.entity.BoardUser;
import kdt.web_ide.members.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BoardUserResponseDto {
    private Long memberId;
    private String nickName;
    private String profileImg;

    @Builder
    public BoardUserResponseDto(BoardUser boardUser) {
        this.memberId = boardUser.getMember().getMemberId();
        this.nickName = boardUser.getMember().getNickName();
        this.profileImg = boardUser.getMember().getProfileImage();
    }
}
