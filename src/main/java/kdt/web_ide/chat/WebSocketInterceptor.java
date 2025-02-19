package kdt.web_ide.chat;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import kdt.web_ide.members.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketInterceptor implements ChannelInterceptor {

  private final JwtProvider jwtProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {

      String token = authHeader.substring(7);

      if (jwtProvider.validateToken(token)) {

        Claims claims = jwtProvider.getUserInfoFromToken(token);

        String memberId = claims.get("memberId", Long.class).toString();

        Authentication authentication = jwtProvider.createUserAuthentication(memberId);

        accessor.setUser(authentication);

      } else {
        throw new IllegalArgumentException("Invalid or expired token");
      }
    }
    return message;
  }
}
