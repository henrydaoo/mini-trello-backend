package com.minitrello.service.notification;

import com.minitrello.entity.Task;
import com.minitrello.entity.User;

public interface NotificationStrategy {
  void send(User user, Task task);
}
