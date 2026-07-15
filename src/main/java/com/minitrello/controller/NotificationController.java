package com.minitrello.controller;

import com.minitrello.dto.NotificationResponse;
import com.minitrello.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification")
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getNotifications(
      Authentication authentication) {
    return ResponseEntity.ok(
        notificationService.getNotificationsForCurrentUser(authentication.getName()));
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<NotificationResponse> markAsRead(
      Authentication authentication, @PathVariable Long id) {
    return ResponseEntity.ok(notificationService.markAsRead(authentication.getName(), id));
  }
}
