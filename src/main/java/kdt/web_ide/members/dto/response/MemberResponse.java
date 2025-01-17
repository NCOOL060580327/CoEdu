package kdt.web_ide.members.dto.response;

import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(force = true)
public class MemberResponse {
    public MemberResponse(Member member) {
        this.memberId = member.getMemberId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.profileImage = member.getProfileImage();
    }

    private final Long memberId;

    private final String email;
    private final String name;

    private final String profileImage;

    public static MemberResponse of(Member member){
        return new MemberResponse(member);
    }
}
