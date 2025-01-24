package kdt.web_ide.post.entity.repository;

import kdt.web_ide.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

