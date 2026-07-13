package com.minitrello.repository;

import com.minitrello.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    List<TaskList> findByBoardIdOrderByPositionAsc(Long boardId);

    Optional<TaskList> findTopByBoardIdOrderByPositionDesc(Long boardId);
}
