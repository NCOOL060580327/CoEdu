package kdt.web_ide.members.dto.response;

import kdt.web_ide.members.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(force = true)
public class MemberResponse {
  public MemberResponse(Member member) {
    this.memberId = member.getMemberId();
    this.loginId = member.getLoginId();
    this.nickName = member.getNickName();
    this.profileImage = member.getProfileImage();
  }

  private final Long memberId;

  private final String loginId;
  private final String nickName;

  private final String profileImage;

  public static MemberResponse of(Member member) {
    return new MemberResponse(member);
  }
}
