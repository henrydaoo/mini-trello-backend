package com.minitrello.service.notification;

import com.minitrello.entity.Task;
import com.minitrello.entity.User;
import com.minitrello.event.TaskAssignedEvent;
import com.minitrello.repository.TaskRepository;
import com.minitrello.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAssignedEventListener {
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final NotificationFactory notificationFactory;

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onTaskAssigned(TaskAssignedEvent event) {
    try {
      Task task = taskRepository.findById(event.getTaskId()).orElse(null);
      User assignee = userRepository.findById(event.getAssigneeId()).orElse(null);

      if (task == null || assignee == null) {
        log.warn(
            "Skipping assignment notification — task {} or user {} no longer exists",
            event.getTaskId(),
            event.getAssigneeId());
        return;
      }

      NotificationStrategy strategy =
          notificationFactory.getStrategy(assignee.getNotificationPreference());
      strategy.send(assignee, task);
    } catch (Exception e) {
      log.error(
          "Failed to send assignment notification for task {} to user {}",
          event.getTaskId(),
          event.getAssigneeId(),
          e);
    }
  }
}
