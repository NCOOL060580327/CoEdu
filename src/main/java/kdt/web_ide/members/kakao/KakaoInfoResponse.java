package kdt.web_ide.members.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import kdt.web_ide.members.oAuth.OAuthInfoResponse;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoInfoResponse implements OAuthInfoResponse {

  @JsonProperty("id")
  private Long kakaoId;

  @JsonProperty("kakao_account")
  private KakaoAccount kakaoAccount;

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class KakaoAccount {
    private KakaoProfile profile;
  }

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class KakaoProfile {
    private String nickname;
    private String profile_image_url;
  }

  @Override
  public String getKakaoNickname() {
    return kakaoAccount.profile.nickname;
  }

  @Override
  public String getKakaoProfileImage() {
    return kakaoAccount.profile.profile_image_url;
  }
}
