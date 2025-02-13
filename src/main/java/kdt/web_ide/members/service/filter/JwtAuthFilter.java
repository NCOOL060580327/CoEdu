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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.service.CustomUserDetailService;
import kdt.web_ide.members.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends GenericFilterBean {

  private final JwtProvider jwtProvider;
  private final CustomUserDetailService customUserDetailService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 헤더에서 JWT 토큰을 가져옴
    String token = jwtProvider.resolveToken(httpRequest);

    if (token != null) {
      if (jwtProvider.validateToken(token)) {

        Long memberId = jwtProvider.getUserInfoFromToken(token).get("memberId", Long.class);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(memberId.toString());

        if (userDetails != null) {
          Authentication authentication = jwtProvider.createUserAuthentication(memberId.toString());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
      } else {
        throw new CustomException(ErrorCode.INVALID_TOKEN);
      }
    }
    // 필터 체인을 계속 진행
    filterChain.doFilter(request, response);
  }
}
