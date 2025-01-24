package kdt.web_ide.members.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import kdt.web_ide.members.dto.request.CustomUserInfoDto;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.entity.RefreshToken;
import kdt.web_ide.members.entity.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    public void init(){
        byte[] bytes = Base64.getDecoder()
                .decode(secretKey);
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
        return BEARER_PREFIX +
                Jwts.builder()
                        .claim(AUTHORIZATION_KEY,role)
                        .setSubject(member.getLoginId())
                        .setClaims(claims)
                        .setExpiration(new Date(now.getTime() + expireTime))
                        .setIssuedAt(now)
                        .signWith(SignatureAlgorithm.HS256,key)
                        .compact();
    }

    /**
     * 토큰 재생성
     * @param username
     * @param roles
     * @return
     */
    public String recreateAccessToken(String username, Object roles){
        Claims claims = Jwts.claims().setSubject(username); // JWT payload 에 저장되는 정보단위
        claims.put("roles", roles); // 정보는 key / value 쌍으로 저장
        Date now = new Date();

        //Access Token
        String accessToken = Jwts.builder()
                .setClaims(claims) // 정보 저장
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + TOKEN_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return accessToken;
    }

    /**
     * 토큰으로 유저정보 가져오기
     * @param token
     * @return
     */
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }


    /**
     * 토큰의 유효성,만료일자 확인
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
     * @param loginId
     * @return
     */

    public UsernamePasswordAuthenticationToken createUserAuthentication(String loginId) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * 로그인 시 토큰 생성
     * @param member
     * @return
     */
    public TokenResponse createTokenByLogin(CustomUserInfoDto member) {
        String accessToken = createToken(RoleType.USER,member,TOKEN_TIME);
        String refreshToken = createToken(RoleType.USER,member,REFRESH_TOKEN_TIME);
        return new TokenResponse(accessToken, refreshToken,member.getLoginId());
    }


    /**
     * 토큰 무효화 (블랙리스트에 추가)
     * @param token
     */
    public void invalidateToken(String token,String loginId) {
        if (validateToken(token)) {
            blacklistedTokens.add(token);
            log.info("Token invalidated: {}", token);
        } else {
            log.warn("Invalid token: {}", token);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token
     * @return
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * 토큰 만료시간 조회
     * @param accessToken
     * @return
     */

    public Long getExpiration(String accessToken){
        //엑세스 토큰 만료시간
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody()
                .getExpiration();
        //현재시간
        long now = new Date().getTime();
        return (expiration.getTime()-now);
    }


    /**
     * 리프레시 토큰의 유효성,만료일자 확인
     * @param refreshTokenObj
     * @return
     */
    public String validateRefreshToken(RefreshToken refreshTokenObj){

        // refresh 객체에서 refreshToken 추출
        String refreshToken = refreshTokenObj.getRefreshToken();

        try {
            // 검증
            Jws<Claims> claims = Jwts.parser().setSigningKey(refreshSecretKey).parseClaimsJws(refreshToken);

            //refresh 토큰의 만료시간이 지나지 않았을 경우, 새로운 access 토큰을 생성
            if (!claims.getBody().getExpiration().before(new Date())) {
                return recreateAccessToken(claims.getBody().get("sub").toString(), claims.getBody().get("roles"));
            }
        }catch (Exception e) {
            //refresh 토큰이 만료되었을 경우, 로그인이 필요.
            return null;

        }

        return null;
    }



}
