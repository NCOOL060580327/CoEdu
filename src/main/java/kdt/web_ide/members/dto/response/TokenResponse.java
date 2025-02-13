package kdt.web_ide.members.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenResponse {
  String accessToken;
  String refreshToken;
}
