package kdt.web_ide.members.oAuth;

import kdt.web_ide.members.kakao.KakaoReissueParams;
import kdt.web_ide.members.kakao.KakaoToken;

public interface OAuthApiClient {

  KakaoToken requestAccessToken(OAuthLoginParams params);

  KakaoToken reissueAccessToken(KakaoReissueParams params);

  OAuthInfoResponse requestOauthInfo(String accessToken);
}
