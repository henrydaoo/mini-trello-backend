package com.minitrello.repository;

import com.minitrello.entity.TaskList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {

  List<TaskList> findByBoardIdOrderByPositionAsc(Long boardId);

  Optional<TaskList> findTopByBoardIdOrderByPositionDesc(Long boardId);
}
