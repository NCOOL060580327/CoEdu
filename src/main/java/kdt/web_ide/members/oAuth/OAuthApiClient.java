package kdt.web_ide.members.oAuth;

import kdt.web_ide.members.kakao.KakaoReissueParams;

public interface OAuthApiClient {

  String requestAccessToken(OAuthLoginParams params);

  String reissueAccessToken(KakaoReissueParams params);

  OAuthInfoResponse requestOauthInfo(String accessToken);
}
