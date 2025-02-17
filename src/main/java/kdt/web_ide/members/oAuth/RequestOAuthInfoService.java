package kdt.web_ide.members.oAuth;

import org.springframework.stereotype.Component;

import kdt.web_ide.members.kakao.KakaoReissueParams;
import kdt.web_ide.members.kakao.KakaoToken;

@Component
public class RequestOAuthInfoService {

  private final OAuthApiClient client;

  public RequestOAuthInfoService(OAuthApiClient client) {
    this.client = client;
  }

  public OAuthInfoResponse request(String accessToken) {
    return client.requestOauthInfo(accessToken);
  }

  public KakaoToken login(OAuthLoginParams params) {
    return client.requestAccessToken(params);
  }

  public KakaoToken reissue(KakaoReissueParams params) {
    return client.reissueAccessToken(params);
  }
}
