package com.minitrello.service;

import com.minitrello.dto.*;
import com.minitrello.entity.*;
import com.minitrello.event.TaskAssignedEvent;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.TaskListNotFoundException;
import com.minitrello.exception.TaskNotFoundException;
import com.minitrello.exception.UserNotFoundException;
import com.minitrello.repository.TaskListRepository;
import com.minitrello.repository.TaskRepository;
import com.minitrello.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
  private final TaskRepository taskRepository;
  private final TaskListRepository taskListRepository;
  private final UserRepository userRepository;
  private final BoardAccessService boardAccessService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public List<TaskResponse> getTasksForList(String username, Long listId, Long assigneeIdFilter) {
    TaskList list = getListOrThrow(listId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(list.getBoard(), currentUser);

    List<Task> tasks =
        assigneeIdFilter != null
            ? taskRepository.findByListIdAndAssigneeIdOrderByPositionAsc(listId, assigneeIdFilter)
            : taskRepository.findByListIdOrderByPositionAsc(listId);

    return tasks.stream().map(this::toResponse).toList();
  }

  public TaskResponse createTask(String username, Long listId, TaskCreateRequest request) {
    TaskList list = getListOrThrow(listId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(list.getBoard(), currentUser);

    User assignee = null;
    if (request.getAssigneeId() != null) {
      assignee = resolveAssignee(list.getBoard(), request.getAssigneeId());
    }

    int nextPosition =
        taskRepository
            .findTopByListIdOrderByPositionDesc(listId)
            .map(last -> last.getPosition() + 1)
            .orElse(0);

    Task task =
        Task.builder()
            .list(list)
            .title(request.getTitle())
            .description(request.getDescription())
            .assignee(assignee)
            .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
            .dueDate(request.getDueDate())
            .position(nextPosition)
            .build();

    return toResponse(taskRepository.save(task));
  }

  @Transactional(readOnly = true)
  public TaskResponse getTaskById(String username, Long taskId) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(task.getList().getBoard(), currentUser);
    return toResponse(task);
  }

  public TaskResponse updateTask(String username, Long taskId, TaskUpdateRequest request) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(task.getList().getBoard(), currentUser);

    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    if (request.getPriority() != null) {
      task.setPriority(request.getPriority());
    }
    task.setDueDate(request.getDueDate());

    return toResponse(taskRepository.save(task));
  }

  public TaskResponse assignTask(String username, Long taskId, TaskAssigneeRequest request) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(task.getList().getBoard(), currentUser);

    if (request.getAssigneeId() == null) {
      task.setAssignee(null);
    } else {
      User assignee = resolveAssignee(task.getList().getBoard(), request.getAssigneeId());
      task.setAssignee(assignee);
      Task savedTask = taskRepository.save(task);
      eventPublisher.publishEvent(new TaskAssignedEvent(savedTask.getId(), assignee.getId()));
      return toResponse(savedTask);
    }

    return toResponse(taskRepository.save(task));
  }

  public TaskResponse moveTask(String username, Long taskId, TaskMoveRequest request) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    Board board = task.getList().getBoard();
    boardAccessService.assertHasAccess(board, currentUser);

    TaskList targetList = getListOrThrow(request.getTargetListId());
    if (!targetList.getBoard().getId().equals(board.getId())) {
      throw new ForbiddenOperationException("Cannot move a task to a list on a different board");
    }

    task.setList(targetList);
    task.setPosition(request.getPosition());

    return toResponse(taskRepository.save(task));
  }

  public void deleteTask(String username, Long taskId) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(task.getList().getBoard(), currentUser);
    taskRepository.delete(task);
  }

  private User resolveAssignee(Board board, Long assigneeId) {
    User assignee =
        userRepository
            .findById(assigneeId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id " + assigneeId));

    boolean canBeAssigned =
        boardAccessService.isOwner(board, assignee) || boardAccessService.isMember(board, assignee);
    if (!canBeAssigned) {
      throw new ForbiddenOperationException(
          "Only the board owner or a board member can be assigned to a task");
    }
    return assignee;
  }

  private TaskList getListOrThrow(Long listId) {
    return taskListRepository
        .findById(listId)
        .orElseThrow(() -> new TaskListNotFoundException("List not found with id " + listId));
  }

  private Task getTaskOrThrow(Long taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + taskId));
  }

  private TaskResponse toResponse(Task task) {
    return TaskResponse.builder()
        .id(task.getId())
        .listId(task.getList().getId())
        .title(task.getTitle())
        .description(task.getDescription())
        .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
        .assigneeUsername(task.getAssignee() != null ? task.getAssignee().getUsername() : null)
        .priority(task.getPriority())
        .dueDate(task.getDueDate())
        .position(task.getPosition())
        .createdAt(task.getCreatedAt())
        .updatedAt(task.getUpdatedAt())
        .build();
  }
}
