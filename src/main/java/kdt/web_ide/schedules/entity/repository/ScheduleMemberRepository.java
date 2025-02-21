package kdt.web_ide.schedules.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kdt.web_ide.members.entity.Member;
import kdt.web_ide.schedules.entity.Schedule;
import kdt.web_ide.schedules.entity.ScheduleMember;

public interface ScheduleMemberRepository extends JpaRepository<ScheduleMember, Long> {
  List<ScheduleMember> findBySchedule(Schedule schedule);

  void deleteBySchedule(Schedule schedule);

  ScheduleMember findByScheduleAndMember(Schedule schedule, Member member);
}
