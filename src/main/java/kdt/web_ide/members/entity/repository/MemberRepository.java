package kdt.web_ide.members.entity.repository;

import kdt.web_ide.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {


    Optional<Member> findByNickName(String nickName);


    Optional<Member> findByLoginId(String userId);
}
