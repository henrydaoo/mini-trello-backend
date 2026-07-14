package com.minitrello.repository;

import com.minitrello.entity.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByListIdOrderByPositionAsc(Long listId);

  List<Task> findByListIdAndAssigneeIdOrderByPositionAsc(Long listId, Long assigneeId);

  Optional<Task> findTopByListIdOrderByPositionDesc(Long listId);
}
