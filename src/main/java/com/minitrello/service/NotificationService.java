package com.minitrello.service;

import com.minitrello.dto.NotificationResponse;
import com.minitrello.entity.Notification;
import com.minitrello.entity.User;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.NotificationNotFoundException;
import com.minitrello.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final BoardAccessService boardAccessService;

  @Transactional(readOnly = true)
  public List<NotificationResponse> getNotificationsForCurrentUser(String username) {
    User currentUser = boardAccessService.getUserByUsername(username);
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
        .map(this::toResponse)
        .toList();
  }

  public NotificationResponse markAsRead(String username, Long notificationId) {
    User currentUser = boardAccessService.getUserByUsername(username);

    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () ->
                    new NotificationNotFoundException(
                        "Notification not found with id " + notificationId));

    if (!notification.getUser().getId().equals(currentUser.getId())) {
      throw new ForbiddenOperationException("You cannot access another user's notification");
    }

    notification.setRead(true);
    return toResponse(notificationRepository.save(notification));
  }

  private NotificationResponse toResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .taskId(notification.getTask().getId())
        .taskTitle(notification.getTask().getTitle())
        .type(notification.getType())
        .message(notification.getMessage())
        .isRead(notification.isRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
