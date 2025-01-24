package kdt.web_ide.boards.entity;

import kdt.web_ide.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardUserRepository extends JpaRepository<BoardUser,Long> {
    boolean existsByMember(Member member);

    List<BoardUser> findByMember(Member currentMember);

    Optional<BoardUser> findByMemberAndIsLeaderTrue(Member currentMember);

    Optional<BoardUser> findByMemberAndBoardAndIsLeaderTrue(Member member, Board board);

    List<BoardUser> findByBoard(Board board);

    Optional<BoardUser> findByMemberAndBoard(Member memberToRemove, Board board);
}
