package kdt.web_ide.chat.entity.repository;

import kdt.web_ide.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findAllByChatRoom_ChatRoomId(Long chatRoomId);

    @Modifying
    @Query("""
        UPDATE ChatRoomMember crm
        SET crm.notReadCount = crm.notReadCount + 1
        WHERE crm.chatRoom.chatRoomId = :chatRoomId
        AND crm.member.memberId != :senderId
    """)
    void incrementNotReadCount(@Param("chatRoomId") Long chatRoomId, @Param("senderId") Long senderId);

    @Modifying
    @Query("""
           UPDATE ChatRoomMember crm
           SET crm.notReadCount = 0
           WHERE crm.chatRoom.chatRoomId = :chatRoomId
           AND crm.member.memberId = :memberId
    """)
    void resetNotReadCount(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

}
