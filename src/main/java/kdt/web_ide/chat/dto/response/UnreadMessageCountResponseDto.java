package kdt.web_ide.chat.dto.response;

import lombok.Builder;

@Builder
public record UnreadMessageCountResponseDto(
        Long roomId,
        Integer notReadCount
) {
    public static UnreadMessageCountResponseDto of(Long roomId, Integer notReadCount) {
        return UnreadMessageCountResponseDto.builder()
                .roomId(roomId)
                .notReadCount(notReadCount)
                .build();
    }
}
