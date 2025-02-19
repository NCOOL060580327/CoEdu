package kdt.web_ide.members.dto.response;

public record LoginResponseWithToken(LoginResponseDto responseDto, String refreshToken) {}
