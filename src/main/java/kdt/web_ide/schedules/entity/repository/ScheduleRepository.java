package kdt.web_ide.schedules.entity.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kdt.web_ide.boards.entity.Board;
import kdt.web_ide.schedules.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  List<Schedule> findByBoard(Board board);

  @Query(
      "SELECT s FROM Schedule s WHERE FUNCTION('DATE', s.startAt) = :date AND s.board.id = :boardId")
  List<Schedule> findByStartAtDateAndBoardId(
      @Param("date") LocalDate date, @Param("boardId") Long boardId);
}
