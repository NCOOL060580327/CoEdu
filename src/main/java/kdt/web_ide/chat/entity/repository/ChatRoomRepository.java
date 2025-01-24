package kdt.web_ide.chat.entity.repository;

import kdt.web_ide.chat.entity.ChatRoom;
import kdt.web_ide.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        select cm.member from ChatRoomMember cm where cm.chatRoom.chatRoomId = :chatRoomId
    """)
    List<Member> findMemberListByChatRoomId(@Param("chatRoomId") Long chatRoomId);

}
