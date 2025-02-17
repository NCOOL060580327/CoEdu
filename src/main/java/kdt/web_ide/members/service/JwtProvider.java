package kdt.web_ide.members.service;

import java.security.Key;
import java.util.*;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
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
   * 토큰 생성
   *
   * @param expireTime
   * @return
   */
  private String createToken(RoleType role, Long memberId, Long expireTime) {
    Claims claims = Jwts.claims();
    claims.put("memberId", memberId);
    claims.put("role", RoleType.USER);

    Date now = new Date();
    return Jwts.builder()
        .claim(AUTHORIZATION_KEY, role)
        .setClaims(claims)
        .setExpiration(new Date(now.getTime() + expireTime))
        .setIssuedAt(now)
        .signWith(signatureAlgorithm, key)
        .compact();
  }

  /**
   * 액세스 토큰 생성
   *
   * @param memberId
   * @return
   */
  public String generateAccessToken(Long memberId) {
    return createToken(RoleType.USER, memberId, TOKEN_TIME);
  }

  /**
   * 리프레시 토큰 생성
   *
   * @param memberId
   * @return
   */
  public String generateRefreshToken(Long memberId) {
    return createToken(RoleType.USER, memberId, REFRESH_TOKEN_TIME);
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
   * @param memberId
   * @return
   */
  public UsernamePasswordAuthenticationToken createUserAuthentication(String memberId) {
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(memberId);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  public Long parseRefreshToken(String token) {
    if (validateToken(token)) {
      Claims claims = getUserInfoFromToken(token);
      return claims.get("memberId", Long.class);
    }
    throw new CustomException(ErrorCode.USER_NOT_FOUND);
  }

  /** 쿠키에서 refreshToken 추출 */
  public Optional<String> extractRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> "refreshToken".equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }
}
