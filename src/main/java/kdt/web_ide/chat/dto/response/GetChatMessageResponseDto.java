package kdt.web_ide.chat.dto.response;

import kdt.web_ide.chat.entity.ChatMessage;
import lombok.Builder;

import java.time.format.DateTimeFormatter;

@Builder
public record GetChatMessageResponseDto(String messageText,
                                        String sendTime,
                                        Long memberId,
                                        String name,
                                        String profileImage
) {
    public static GetChatMessageResponseDto fromChatMessage(ChatMessage chatMessage) {
        return GetChatMessageResponseDto.builder()
                .messageText(chatMessage.getMessageText())
                .sendTime(chatMessage.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .memberId(chatMessage.getSender().getMemberId())
                .name(chatMessage.getSender().getNickName())
                .profileImage(chatMessage.getSender().getProfileImage())
                .build();
    }
}
