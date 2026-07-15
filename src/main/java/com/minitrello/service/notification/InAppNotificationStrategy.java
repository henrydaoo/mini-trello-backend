package com.minitrello.service.notification;

import com.minitrello.entity.Notification;
import com.minitrello.entity.NotificationPreference;
import com.minitrello.entity.Task;
import com.minitrello.entity.User;
import com.minitrello.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppNotificationStrategy implements NotificationStrategy {
  public final NotificationRepository notificationRepository;

  @Override
  public void send(User user, Task task) {
    Notification notification =
        Notification.builder()
            .user(user)
            .task(task)
            .type(NotificationPreference.IN_APP)
            .message("You have been assigned to task \"" + task.getTitle() + "\"")
            .build();

    notificationRepository.save(notification);
  }
}
