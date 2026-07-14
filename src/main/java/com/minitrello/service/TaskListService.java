package com.minitrello.service;

import com.minitrello.dto.TaskListPositionRequest;
import com.minitrello.dto.TaskListRequest;
import com.minitrello.dto.TaskListResponse;
import com.minitrello.entity.Board;
import com.minitrello.entity.TaskList;
import com.minitrello.entity.User;
import com.minitrello.exception.TaskListNotFoundException;
import com.minitrello.repository.TaskListRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskListService {
  private final TaskListRepository taskListRepository;
  private final BoardAccessService boardAccessService;

  @Transactional(readOnly = true)
  public List<TaskListResponse> getListsForBoard(String username, Long boardId) {
    Board board = boardAccessService.getBoardOrThrow(boardId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(board, currentUser);

    return taskListRepository.findByBoardIdOrderByPositionAsc(boardId).stream()
        .map(this::toResponse)
        .toList();
  }

  public TaskListResponse createList(String username, Long boardId, TaskListRequest request) {
    Board board = boardAccessService.getBoardOrThrow(boardId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(board, currentUser);

    int nextPosition =
        taskListRepository
            .findTopByBoardIdOrderByPositionDesc(boardId)
            .map(last -> last.getPosition() + 1)
            .orElse(0);

    TaskList list =
        TaskList.builder().board(board).name(request.getName()).position(nextPosition).build();

    return toResponse(taskListRepository.save(list));
  }

  public TaskListResponse updateList(String username, Long listId, TaskListRequest request) {
    TaskList list = getListOrThrow(listId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(list.getBoard(), currentUser);

    list.setName(request.getName());
    return toResponse(taskListRepository.save(list));
  }

  public TaskListResponse updatePosition(
      String username, Long listId, TaskListPositionRequest request) {
    TaskList list = getListOrThrow(listId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(list.getBoard(), currentUser);

    list.setPosition(request.getPosition());
    return toResponse(taskListRepository.save(list));
  }

  public void deleteList(String username, Long listId) {
    TaskList list = getListOrThrow(listId);
    User currentUser = boardAccessService.getUserByUsername(username);
    // Deleting a list removes every task inside it, so this is restricted to the board owner.
    boardAccessService.assertIsOwner(list.getBoard(), currentUser);

    taskListRepository.delete(list);
  }

  private TaskList getListOrThrow(Long listId) {
    return taskListRepository
        .findById(listId)
        .orElseThrow(() -> new TaskListNotFoundException("List not found with id " + listId));
  }

  private TaskListResponse toResponse(TaskList list) {
    return TaskListResponse.builder()
        .id(list.getId())
        .boardId(list.getBoard().getId())
        .name(list.getName())
        .position(list.getPosition())
        .createdAt(list.getCreatedAt())
        .build();
  }
}
