package com.minitrello.controller;

import com.minitrello.dto.CommentRequest;
import com.minitrello.dto.CommentResponse;
import com.minitrello.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @GetMapping("/api/tasks/{taskId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(
      Authentication authentication, @PathVariable Long taskId) {
    return ResponseEntity.ok(commentService.getCommentsForTask(authentication.getName(), taskId));
  }

  @PostMapping("/api/tasks/{taskId}/comments")
  public ResponseEntity<CommentResponse> addComment(
      Authentication authentication,
      @PathVariable Long taskId,
      @Valid @RequestBody CommentRequest request) {
    CommentResponse response = commentService.addComment(authentication.getName(), taskId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/api/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
      Authentication authentication, @PathVariable Long commentId) {
    commentService.deleteComment(authentication.getName(), commentId);
    return ResponseEntity.noContent().build();
  }
}
