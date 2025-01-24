package kdt.web_ide.chat.controller;

import kdt.web_ide.chat.dto.request.ChatMessageRequestDto;
import kdt.web_ide.chat.dto.response.GetChatMessageResponseDto;
import kdt.web_ide.chat.service.ChatService;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.members.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final MemberService memberService;

    // 메세지 전송 및 저장
    @MessageMapping("/chat/{roomId}")
    public ResponseEntity<Void> sendMessage(@DestinationVariable("roomId") Long roomId, @Payload ChatMessageRequestDto requestDto) {
        chatService.sendMessage(roomId, requestDto.senderId(), requestDto.content());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<Void> createChatRoom() {
        chatService.createChatRoom();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // 채팅방 채팅 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<List<GetChatMessageResponseDto>> getChatMessage(@PathVariable("roomId") Long roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = memberService.getMember(userDetails.getMember()).getMemberId();
        return ResponseEntity.status(HttpStatus.OK).body(chatService.getChatMessage(roomId, memberId));
    }

}
