package kdt.web_ide.chat.entity.repository;

import kdt.web_ide.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        SELECT c
        FROM ChatMessage c
        JOIN FETCH c.sender
        JOIN FETCH c.chatRoom
        WHERE c.chatRoom.chatRoomId = :chatRoomId
        ORDER BY c.sendTime DESC
    """)
    List<ChatMessage> findChatMessageListByChatRoom(@Param("chatRoomId") Long roomId);

}
