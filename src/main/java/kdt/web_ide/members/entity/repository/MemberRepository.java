package kdt.web_ide.members.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kdt.web_ide.members.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByKakaoId(Long kakaoId);

  Optional<Member> findByNickName(String nickName);

  @Query("""
    Select br.member
    From BoardUser br
    Where br.board.id=:boardId
    """)
  List<Member> findMemberListByBoardId(Long boardId);
}
