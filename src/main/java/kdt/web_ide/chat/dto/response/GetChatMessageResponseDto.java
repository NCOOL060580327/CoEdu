package kdt.web_ide.chat.dto.response;

import java.time.format.DateTimeFormatter;

import kdt.web_ide.chat.entity.ChatMessage;
import lombok.Builder;

@Builder
public record GetChatMessageResponseDto(
    Long senderId,
    String messageText,
    String memberProfileImageUrl,
    String memberNickname,
    String sendTime) {
  public static GetChatMessageResponseDto fromChatMessage(ChatMessage chatMessage) {
    return GetChatMessageResponseDto.builder()
        .senderId(chatMessage.getSender().getMemberId())
        .messageText(chatMessage.getMessageText())
        .memberProfileImageUrl(chatMessage.getSender().getProfileImage())
        .memberNickname(chatMessage.getSender().getNickName())
        .sendTime(
            chatMessage.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .build();
  }
}
