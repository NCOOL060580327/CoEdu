package kdt.web_ide.members.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RefreshTokenRequestDto {

  private String refreshToken;
}
