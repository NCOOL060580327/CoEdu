package kdt.web_ide.post.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kdt.web_ide.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findByBoardId(Long boardId);
}
