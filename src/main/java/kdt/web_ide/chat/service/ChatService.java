package kdt.web_ide.chat.service;

import kdt.web_ide.chat.dto.response.GetChatMessageResponseDto;
import kdt.web_ide.chat.dto.response.UnreadMessageCountResponseDto;
import kdt.web_ide.chat.entity.ChatMessage;
import kdt.web_ide.chat.entity.ChatRoom;
import kdt.web_ide.chat.entity.ChatRoomMember;
import kdt.web_ide.chat.entity.repository.ChatMessageRepository;
import kdt.web_ide.chat.entity.repository.ChatRoomMemberRepository;
import kdt.web_ide.chat.entity.repository.ChatRoomRepository;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final MemberRepository memberRepository;

    // 메세지 전송
    @Transactional
    public void sendMessage(Long chatRoomId, Long senderId, String messageText) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = saveMessage(chatRoom, sender, messageText);

        simpMessagingTemplate.convertAndSend("/room/chat/" + chatRoomId, GetChatMessageResponseDto.fromChatMessage(message));

        chatRoomMemberRepository.incrementNotReadCount(chatRoomId, senderId);

        notifyUnreadMessageCount(chatRoomId, senderId);
    }

    @Transactional
    public List<GetChatMessageResponseDto> getChatMessage(Long roomId, Long memberId) {

        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessageListByChatRoom(roomId);

        List<GetChatMessageResponseDto> responseDtoList = chatMessageList.stream()
                .map(GetChatMessageResponseDto::fromChatMessage).toList();

        chatRoomMemberRepository.resetNotReadCount(roomId, memberId);

        return responseDtoList;
    }

    private ChatMessage saveMessage(ChatRoom chatRoom, Member sender, String messageText) {

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageText(messageText)
                .sendTime(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(message);
    }

    private void notifyUnreadMessageCount(Long roomId, Long senderId) {

        List<ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findAllByChatRoom_ChatRoomId(roomId);

        for (ChatRoomMember chatRoomMember : chatRoomMemberList) {

            Long memberId = chatRoomMember.getMember().getMemberId();

            if (!memberId.equals(senderId)) {

                int notReadCount = chatRoomMember.getNotReadCount();

                simpMessagingTemplate.convertAndSend("/room/unread/" + memberId, UnreadMessageCountResponseDto.of(roomId, notReadCount));
            }
        }
    }

}
