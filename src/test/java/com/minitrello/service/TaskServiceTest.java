package com.minitrello.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minitrello.dto.TaskAssigneeRequest;
import com.minitrello.dto.TaskCreateRequest;
import com.minitrello.dto.TaskMoveRequest;
import com.minitrello.dto.TaskResponse;
import com.minitrello.entity.Board;
import com.minitrello.entity.NotificationPreference;
import com.minitrello.entity.Priority;
import com.minitrello.entity.Task;
import com.minitrello.entity.TaskList;
import com.minitrello.entity.User;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.TaskNotFoundException;
import com.minitrello.repository.TaskListRepository;
import com.minitrello.repository.TaskRepository;
import com.minitrello.repository.UserRepository;
import com.minitrello.service.notification.NotificationFactory;
import com.minitrello.service.notification.NotificationStrategy;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskListRepository taskListRepository;
  @Mock private UserRepository userRepository;
  @Mock private BoardAccessService boardAccessService;
  @Mock private NotificationFactory notificationFactory;
  @Mock private NotificationStrategy notificationStrategy;

  @InjectMocks private TaskService taskService;

  private User owner;
  private User assignee;
  private Board board;
  private TaskList list;

  @BeforeEach
  void setUp() {
    owner =
        User.builder()
            .id(1L)
            .username("owner")
            .email("owner@test.com")
            .passwordHash("hash")
            .build();
    assignee =
        User.builder()
            .id(2L)
            .username("bob")
            .email("bob@test.com")
            .passwordHash("hash")
            .notificationPreference(NotificationPreference.IN_APP)
            .build();
    board = Board.builder().id(10L).name("Board A").owner(owner).build();
    list = TaskList.builder().id(100L).board(board).name("To Do").position(0).build();
  }

  @Test
  void createTask_withoutAssignee_computesNextPositionAsZeroWhenListEmpty() {
    when(taskListRepository.findById(100L)).thenReturn(Optional.of(list));
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(taskRepository.findTopByListIdOrderByPositionDesc(100L)).thenReturn(Optional.empty());
    when(taskRepository.save(any(Task.class)))
        .thenAnswer(
            inv -> {
              Task t = inv.getArgument(0);
              t.setId(1000L);
              return t;
            });

    TaskCreateRequest request =
        new TaskCreateRequest("Setup CI", "desc", null, Priority.HIGH, null);

    TaskResponse response = taskService.createTask("owner", 100L, request);

    assertThat(response.getTitle()).isEqualTo("Setup CI");
    assertThat(response.getPosition()).isZero();
    assertThat(response.getAssigneeId()).isNull();
    verify(boardAccessService).assertHasAccess(board, owner);
  }

  @Test
  void createTask_withAssigneeNotOnBoard_throwsForbidden() {
    when(taskListRepository.findById(100L)).thenReturn(Optional.of(list));
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
    when(boardAccessService.isOwner(board, assignee)).thenReturn(false);
    when(boardAccessService.isMember(board, assignee)).thenReturn(false);

    TaskCreateRequest request = new TaskCreateRequest("Setup CI", "desc", 2L, Priority.HIGH, null);

    assertThrows(
        ForbiddenOperationException.class, () -> taskService.createTask("owner", 100L, request));

    verify(taskRepository, never()).save(any());
  }



  @Test
  void assignTask_withNullAssigneeId_unassignsAndSkipsNotification() {
    Task task =
        Task.builder()
            .id(500L)
            .list(list)
            .title("Fix bug")
            .assignee(assignee)
            .priority(Priority.MEDIUM)
            .position(0)
            .build();

    when(taskRepository.findById(500L)).thenReturn(Optional.of(task));
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    TaskResponse response = taskService.assignTask("owner", 500L, new TaskAssigneeRequest(null));

    assertThat(response.getAssigneeId()).isNull();
    verify(notificationFactory, never()).getStrategy(any());
  }

  @Test
  void assignTask_toUserNotOnBoard_throwsForbidden_andNeverNotifies() {
    Task task =
        Task.builder()
            .id(500L)
            .list(list)
            .title("Fix bug")
            .priority(Priority.MEDIUM)
            .position(0)
            .build();
    User outsider =
        User.builder().id(3L).username("outsider").email("o@test.com").passwordHash("h").build();

    when(taskRepository.findById(500L)).thenReturn(Optional.of(task));
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(userRepository.findById(3L)).thenReturn(Optional.of(outsider));
    when(boardAccessService.isOwner(board, outsider)).thenReturn(false);
    when(boardAccessService.isMember(board, outsider)).thenReturn(false);

    assertThrows(
        ForbiddenOperationException.class,
        () -> taskService.assignTask("owner", 500L, new TaskAssigneeRequest(3L)));

    verify(notificationFactory, never()).getStrategy(any());
    verify(taskRepository, never()).save(any());
  }

  @Test
  void moveTask_toListOnDifferentBoard_throwsForbidden() {
    Task task =
        Task.builder()
            .id(500L)
            .list(list)
            .title("Fix bug")
            .priority(Priority.MEDIUM)
            .position(0)
            .build();
    Board otherBoard = Board.builder().id(20L).name("Board B").owner(owner).build();
    TaskList otherList =
        TaskList.builder().id(200L).board(otherBoard).name("Done").position(0).build();

    when(taskRepository.findById(500L)).thenReturn(Optional.of(task));
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(taskListRepository.findById(200L)).thenReturn(Optional.of(otherList));

    assertThrows(
        ForbiddenOperationException.class,
        () -> taskService.moveTask("owner", 500L, new TaskMoveRequest(200L, 0)));

    verify(taskRepository, never()).save(any());
  }

  @Test
  void moveTask_toListOnSameBoard_succeeds() {
    Task task =
        Task.builder()
            .id(500L)
            .list(list)
            .title("Fix bug")
            .priority(Priority.MEDIUM)
            .position(0)
            .build();
    TaskList targetList =
        TaskList.builder().id(101L).board(board).name("In Progress").position(1).build();

    when(taskRepository.findById(500L)).thenReturn(Optional.of(task));
    when(boardAccessService.getUserByUsername("owner")).thenReturn(owner);
    when(taskListRepository.findById(101L)).thenReturn(Optional.of(targetList));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    TaskResponse response = taskService.moveTask("owner", 500L, new TaskMoveRequest(101L, 2));

    assertThat(response.getListId()).isEqualTo(101L);
    assertThat(response.getPosition()).isEqualTo(2);
  }

  @Test
  void deleteTask_whenNoAccess_throwsForbidden_andNeverDeletes() {
    Task task =
        Task.builder()
            .id(500L)
            .list(list)
            .title("Fix bug")
            .priority(Priority.MEDIUM)
            .position(0)
            .build();

    when(taskRepository.findById(500L)).thenReturn(Optional.of(task));
    when(boardAccessService.getUserByUsername("stranger")).thenReturn(owner);
    org.mockito.Mockito.doThrow(new ForbiddenOperationException("no access"))
        .when(boardAccessService)
        .assertHasAccess(board, owner);

    assertThrows(ForbiddenOperationException.class, () -> taskService.deleteTask("stranger", 500L));

    verify(taskRepository, never()).delete(any());
  }

  @Test
  void getTaskById_notFound_throwsTaskNotFoundException() {
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById("owner", 999L));
  }
}
