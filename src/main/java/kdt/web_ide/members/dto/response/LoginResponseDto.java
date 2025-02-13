package kdt.web_ide.members.dto.response;

import kdt.web_ide.members.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {

  private Long memberId;
  private String nickName;
  private String profileImage;
  private TokenResponse tokenResponse;

  @Builder
  public LoginResponseDto(Member member, TokenResponse tokenResponse) {
    this.memberId = member.getMemberId();
    this.nickName = member.getNickName();
    this.profileImage = member.getProfileImage();
    this.tokenResponse = tokenResponse;
  }
}
