package kdt.web_ide.members.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import kdt.web_ide.members.oAuth.OAuthApiClient;
import kdt.web_ide.members.oAuth.OAuthInfoResponse;
import kdt.web_ide.members.oAuth.OAuthLoginParams;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthApiClient {

  private static final String LOGIN_GRANT_TYPE = "authorization_code";
  private static final String REFRESH_GRANT_TYPE = "refresh_token";

  @Value("${oauth.kakao.url.auth}")
  private String authUrl;

  @Value("${oauth.kakao.url.api}")
  private String apiUrl;

  @Value("${oauth.kakao.client-id}")
  private String clientId;

  @Value("${oauth.kakao.client-secret}")
  private String clientSecret;

  private final RestTemplate restTemplate;

  @Override
  public KakaoToken requestAccessToken(OAuthLoginParams params) {

    String url = authUrl + "/oauth/token";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(
        MediaType.valueOf("application/x-www-form-urlencoded;charset=utf-8"));

    MultiValueMap<String, String> body = params.makeBody();
    body.add("grant_type", LOGIN_GRANT_TYPE);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

    KakaoToken response = restTemplate.postForObject(url, request, KakaoToken.class);

    assert response != null;

    return response;
  }

  @Override
  public OAuthInfoResponse requestOauthInfo(String accessToken) {
    String url = apiUrl + "/v2/user/me";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    httpHeaders.set("Authorization", "Bearer " + accessToken);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("property_keys", "[\"kakao_account.profile\"]");

    HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

    return restTemplate.postForObject(url, request, KakaoInfoResponse.class);
  }

  @Override
  public KakaoToken reissueAccessToken(KakaoReissueParams params) {
    String url = authUrl + "/oauth/token";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = params.makeBody();
    body.add("grant_type", REFRESH_GRANT_TYPE);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

    KakaoToken response = restTemplate.postForObject(url, request, KakaoToken.class);

    assert response != null;

    return response;
  }
}
