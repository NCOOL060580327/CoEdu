package kdt.web_ide.members.service.errorHandler;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import kdt.web_ide.members.dto.response.SecurityExceptionDto;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final SecurityExceptionDto exceptionDto =
      new SecurityExceptionDto(
          HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());

    try (OutputStream os = response.getOutputStream()) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.writeValue(os, exceptionDto);
      os.flush();
    }
  }
}
