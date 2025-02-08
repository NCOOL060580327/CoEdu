package kdt.web_ide.members.service;

import java.security.Key;
import java.util.*;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kdt.web_ide.members.dto.request.CustomUserInfoDto;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.entity.RefreshToken;
import kdt.web_ide.members.entity.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String AUTHORIZATION_KEY = "auth";

  private final CustomUserDetailService customUserDetailsService;
  public static final String BEARER_PREFIX = "Bearer ";

  private static final long REFRESH_TOKEN_TIME = 1000 * 60 * 60 * 24 * 7L; // 7일
  public static final Long TOKEN_TIME = 60 * 60 * 1000L; // 60분

  private static final Set<String> blacklistedTokens = new HashSet<>();

  @Value("${jwt.secret.key}")
  private String secretKey;

  @Value("${jwt.secret.refresh}")
  private String refreshSecretKey;

  private Key key;
  private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

  @PostConstruct
  public void init() {
    byte[] bytes = Base64.getDecoder().decode(secretKey);
    key = Keys.hmacShaKeyFor(bytes);
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /**
   * 엑세스 토큰 생성
   *
   * @param member
   * @param expireTime
   * @return
   */
  public String createToken(RoleType role, CustomUserInfoDto member, Long expireTime) {
    Claims claims = Jwts.claims();
    claims.put("memberId", member.getMemberId());
    claims.put("loginId", member.getLoginId());
    claims.put("role", member.getRoles());

    Date now = new Date();
    return Jwts.builder()
        .claim(AUTHORIZATION_KEY, role)
        .setSubject(member.getLoginId())
        .setClaims(claims)
        .setExpiration(new Date(now.getTime() + expireTime))
        .setIssuedAt(now)
        .signWith(SignatureAlgorithm.HS256, key)
        .compact();
  }

  /**
   * 토큰 재생성
   *
   * @param role
   * @param member
   * @param expireTime
   * @return
   */
  public String recreateAccessToken(RoleType role, CustomUserInfoDto member, Long expireTime) {
    Claims claims = Jwts.claims();
    claims.put("memberId", member.getMemberId());
    claims.put("loginId", member.getLoginId());
    claims.put("role", member.getRoles());

    Date now = new Date();

    return Jwts.builder()
        .claim(AUTHORIZATION_KEY, role)
        .setSubject(member.getLoginId())
        .setClaims(claims)
        .setExpiration(new Date(now.getTime() + expireTime))
        .setIssuedAt(now)
        .signWith(SignatureAlgorithm.HS256, key)
        .compact();
  }

  /**
   * 토큰으로 유저정보 가져오기
   *
   * @param token
   * @return
   */
  public Claims getUserInfoFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  /**
   * 토큰의 유효성,만료일자 확인
   *
   * @param jwtToken
   * @return
   */
  public boolean validateToken(String jwtToken) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
      return !claims.getBody().getExpiration().before(new Date());
    } catch (SecurityException e) {
      throw new JwtException("TOKEN_INVALID");
    } catch (MalformedJwtException e) {
      throw new JwtException("TOKEN_INVALID");
    } catch (ExpiredJwtException e) {
      throw new JwtException("TOKEN_EXPIRED");
    } catch (UnsupportedJwtException e) {
      throw new JwtException("TOKEN_INVALID");
    } catch (IllegalArgumentException e) {
      throw new JwtException("TOKEN_INVALID");
    }
  }

  /**
   * 유저 정보 저장
   *
   * @param loginId
   * @return
   */
  public UsernamePasswordAuthenticationToken createUserAuthentication(String loginId) {
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  /**
   * 로그인 시 토큰 생성
   *
   * @param member
   * @return
   */
  public TokenResponse createTokenByLogin(CustomUserInfoDto member) {
    String accessToken = createToken(RoleType.USER, member, TOKEN_TIME);
    String refreshToken = createToken(RoleType.USER, member, REFRESH_TOKEN_TIME);
    return new TokenResponse(accessToken, refreshToken, member.getLoginId());
  }

  /**
   * 토큰 무효화 (블랙리스트에 추가)
   *
   * @param token
   */
  public void invalidateToken(String token, String loginId) {
    if (validateToken(token)) {
      blacklistedTokens.add(token);
      log.info("Token invalidated: {}", token);
    } else {
      log.warn("Invalid token: {}", token);
    }
  }

  /**
   * 토큰이 블랙리스트에 있는지 확인
   *
   * @param token
   * @return
   */
  public boolean isTokenBlacklisted(String token) {
    return blacklistedTokens.contains(token);
  }

  /**
   * 토큰 만료시간 조회
   *
   * @param accessToken
   * @return
   */
  public Long getExpiration(String accessToken) {
    // 엑세스 토큰 만료시간
    Date expiration =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(accessToken)
            .getBody()
            .getExpiration();
    // 현재시간
    long now = new Date().getTime();
    return (expiration.getTime() - now);
  }

  /**
   * 리프레시 토큰의 유효성 및 만료 여부 확인
   *
   * @param refreshTokenObj 리프레시 토큰 객체
   * @return 새로 생성된 액세스 토큰 (유효하면), 만료 시 예외 발생
   */
  public String validateRefreshToken(RefreshToken refreshTokenObj) {
    // refresh 객체에서 refreshToken 추출
    String refreshToken = refreshTokenObj.getRefreshToken();
    if (refreshToken.startsWith("Bearer ")) {
      refreshToken = refreshToken.substring(7);
    }

    try {
      Jws<Claims> claims =
          Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(refreshToken);

      Date expiration = claims.getBody().getExpiration();
      Date now = new Date();

      // 만료 여부 확인
      if (expiration.before(now)) {
        log.warn("리프레시 토큰이 만료되었습니다.");
        return null;
      }

      // 로그인 ID, 역할 정보 추출
      Long memberId = claims.getBody().get("memberId", Long.class);
      String loginId = claims.getBody().get("loginId", String.class);
      List<RoleType> roles = claims.getBody().get("role", List.class);

      if (memberId == null || loginId == null || roles == null) {
        log.error("JWT에서 memberId, loginId 또는 roles 값을 찾을 수 없습니다!");
        return null;
      }
      CustomUserInfoDto userInfo = new CustomUserInfoDto(memberId, loginId, roles);
      return recreateAccessToken(RoleType.USER, userInfo, TOKEN_TIME);
    } catch (ExpiredJwtException e) {
      log.warn("JWT Expired: {}", e.getMessage());
      return null;
    } catch (JwtException e) {
      log.error("Invalid Token: {}", e.getMessage());
      return null;
    }
  }
}
