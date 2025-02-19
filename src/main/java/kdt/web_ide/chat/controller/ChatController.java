package kdt.web_ide.chat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import kdt.web_ide.chat.dto.request.ChatMessageRequestDto;
import kdt.web_ide.chat.dto.response.GetChatMessageResponseDto;
import kdt.web_ide.chat.service.ChatService;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.members.service.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;
  private final MemberService memberService;

  // 메세지 전송 및 저장
  @MessageMapping("/chat/{roomId}")
  public ResponseEntity<Void> sendMessage(
      @DestinationVariable("roomId") Long roomId, @Payload ChatMessageRequestDto requestDto) {
    chatService.sendMessage(roomId, requestDto);
    return ResponseEntity.status(HttpStatus.OK).body(null);
  }

  // 채팅방 채팅 조회
  @GetMapping("/{roomId}")
  public ResponseEntity<List<GetChatMessageResponseDto>> getChatMessage(
      @PathVariable("roomId") Long roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = memberService.getMember(userDetails.getMember()).getMemberId();
    return ResponseEntity.status(HttpStatus.OK).body(chatService.getChatMessage(roomId, memberId));
  }

  @PostMapping("/{roomId}/images")
  public ResponseEntity<Void> sendImages(
      @PathVariable("roomId") Long roomId,
      @RequestPart("imageFile") List<MultipartFile> imageFiles,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getMember().getMemberId();
    chatService.sendImage(memberId, roomId, imageFiles);
    return ResponseEntity.status(HttpStatus.OK).body(null);
  }
}
