package com.minitrello.dto;

import com.minitrello.entity.NotificationPreference;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {
  private Long id;
  private Long taskId;
  private String taskTitle;
  private NotificationPreference type;
  private String message;
  private boolean isRead;
  private Instant createdAt;
}
