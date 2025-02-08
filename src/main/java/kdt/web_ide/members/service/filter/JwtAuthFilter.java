package kdt.web_ide.members.service.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import kdt.web_ide.members.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends GenericFilterBean {

  private final JwtProvider jwtProvider;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 헤더에서 JWT 토큰을 가져옴
    String token = jwtProvider.resolveToken(httpRequest);

    if (token != null && jwtProvider.validateToken(token)) {
      // JWT 토큰에서 loginId 추철
      Claims claims = jwtProvider.getUserInfoFromToken(token);
      String loginId = claims.get("loginId", String.class);

      if (loginId != null) {
        Authentication authentication = jwtProvider.createUserAuthentication(loginId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    // 필터 체인을 계속 진행
    filterChain.doFilter(request, response);
  }
}
