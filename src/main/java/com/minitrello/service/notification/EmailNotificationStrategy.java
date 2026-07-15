package com.minitrello.service.notification;

import com.minitrello.entity.Task;
import com.minitrello.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationStrategy implements NotificationStrategy {

  @Override
  public void send(User user, Task task) {
    log.info(
        "[EMAIL] To: {} <{}> — You have been assigned to task \"{}\" (list: {})",
        user.getUsername(),
        user.getEmail(),
        task.getTitle(),
        task.getList().getName());
  }
}
