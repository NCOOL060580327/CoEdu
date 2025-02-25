package kdt.web_ide.notification.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kdt.web_ide.members.entity.Member;
import kdt.web_ide.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByMemberOrderByCreatedAtDesc(Member member);

  List<Notification> findByMemberAndIsReadFalse(Member member);

  void deleteByMember(Member member);
}
