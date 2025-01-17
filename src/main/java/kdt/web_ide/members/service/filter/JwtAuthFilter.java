package kdt.web_ide.members.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 헤더에서 JWT 토큰을 가져옴
        String token = jwtProvider.resolveToken(httpRequest);

        // 토큰이 유효한지 확인
        if (token != null && jwtProvider.validateToken(token)) {
            // 토큰이 유효하면 토큰으로부터 유저 정보를 가져옴
            Claims userInfo = jwtProvider.getUserInfoFromToken(token);

            Authentication authentication = jwtProvider.createUserAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        // 필터 체인을 계속 진행
        filterChain.doFilter(request, response);
    }

}

