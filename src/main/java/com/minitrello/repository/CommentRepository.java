package com.minitrello.repository;

import com.minitrello.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
