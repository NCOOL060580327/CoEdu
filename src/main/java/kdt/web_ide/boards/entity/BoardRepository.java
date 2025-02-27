package kdt.web_ide.boards.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {
  @Query("SELECT b.id FROM Board b WHERE b.title = :title")
  Optional<Long> findIdByTitle(@Param("title") String title);
}
