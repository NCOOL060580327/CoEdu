package kdt.web_ide.members.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kdt.web_ide.members.dto.request.CustomUserInfoDto;
import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.entity.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.hibernate.query.spi.QueryInterpretationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";

    public static final String BEARER_PREFIX = "Bearer ";
    private static final long REFRESH_TOKEN_TIME = 1000 * 60 * 60 * 24 * 7L;// 7일


    // 토큰 만료 시간
    public final Long TOKEN_TIME = 60 * 60 * 1000L; //60분

    private Key key;

    private final UserDetailsService userDetailsService;



    // 로그 설정
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;

    public JwtUtil() {
    }

    /**
     * 헤더에서 토큰 가져오기
     * @param request
     * @return
     */
    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(BEARER_PREFIX))
            return bearerToken.substring(7);
        return null;
    }

    /**
     * 토큰 생성
     * @param member
     * @return
     */
    public String createAccessToken(CustomUserInfoDto member) {
        return createToken(member,TOKEN_TIME);
    }

    /**
     * Access Token 생성
     * @param member
     * @param expireTime
     * @return
     */
    public String createToken(CustomUserInfoDto member, Long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("memberId", member.getMemberId());
        claims.put("email", member.getEmail());
        claims.put("role", member.getRole());

        // 토큰 생성 시간
        Date now = new Date();


        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(member.getEmail()) // 주제 설정
                        .setClaims(claims) // 클레임 추가
                        .setExpiration(new Date(now.getTime() + expireTime)) // 만료 시간
                        .setIssuedAt(now) // 발급 시간
                        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256) // 서명
                        .compact();
    }

    // Jwt Cookie에 저장
    public void addJwtToCookie(String token, HttpServletResponse res) {
        try{
            token = URLEncoder.encode(token,"utf-8").replaceAll("\\+","%20");
            Cookie cookie = new Cookie(AUTHORIZATION_HEADER,token);
            cookie.setPath("/");
            res.addCookie(cookie);
        } catch (UnsupportedEncodingException e){
            logger.error(e.getMessage());
        }
    }

    /**
     * 로그인 시 토큰 발행
     * @param member
     * @return
     */
    public TokenResponse createTokenByLogin(CustomUserInfoDto member){
        String accessToken = createToken(member,TOKEN_TIME);
        String refreshToken = createToken(member,REFRESH_TOKEN_TIME);
        return new TokenResponse(accessToken,refreshToken);
    }

    // 리프레쉬 토큰 함게 발행

    /**
     * 토큰으로 유저정보 가져오기
     * @param token
     * @return
     */
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Jwt 검증
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch(SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            log.info("유효하지 않은 만료된 JWT 토큰입니다.");
        } catch(IllegalArgumentException e) {
            log.info("잘못된 토큰 입니다.");
        }
        return false;
    }

    /**
     * 일반 유저 인증 객체 생성
     * @param email
     * @return
     */
    public Authentication createUserAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
