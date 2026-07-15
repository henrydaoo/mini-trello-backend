package com.minitrello.service.notification;

import com.minitrello.entity.NotificationPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationFactory {
  private final EmailNotificationStrategy emailNotificationStrategy;
  private final InAppNotificationStrategy inAppNotificationStrategy;

  public NotificationStrategy getStrategy(NotificationPreference preference) {
    return switch (preference) {
      case EMAIL -> emailNotificationStrategy;
      case IN_APP -> inAppNotificationStrategy;
    };
  }
}
