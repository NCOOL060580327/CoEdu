package kdt.web_ide.members.dto.request;

import java.util.Collections;

import jakarta.validation.constraints.Pattern;

import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.RoleType;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class JoinRequestDto {

  private final String loginId;

  @Pattern(
      regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
      message = "닉네임은 최소 2자 이상, 10자 이하이며, 영문, 숫자, 한글만 입력하세요.")
  private final String nickName;

  @Pattern(
      regexp = "(?=.*[a-zA-Z])(?=.*[0-9])^[a-zA-Z0-9~!@#$%^&*()+|=]{8}$",
      message = "비밀번호는 8자, 소문자와 숫자만 입력하세요.")
  private final String password;

  @Pattern(
      regexp = "(?=.*[a-zA-Z])(?=.*[0-9])^[a-zA-Z0-9~!@#$%^&*()+|=]{8}$",
      message = "비밀번호를 확인해주세요.")
  private final String password2;

  @Builder
  public JoinRequestDto(String loginId, String nickName, String password, String password2) {
    this.loginId = loginId;
    this.nickName = nickName;
    this.password = password;
    this.password2 = password2;
  }

  public Member toEntity(RoleType role, String encodedPassword) {
    return Member.builder()
        .loginId(loginId)
        .password(encodedPassword)
        .nickName(nickName)
        .roles(Collections.singletonList(role))
        .build();
  }
}
