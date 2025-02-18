package kdt.web_ide.schedules.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kdt.web_ide.boards.entity.Board;
import kdt.web_ide.schedules.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  List<Schedule> findByBoard(Board board);
}
