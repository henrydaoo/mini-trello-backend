package com.minitrello.controller;

import com.minitrello.dto.TaskAssigneeRequest;
import com.minitrello.dto.TaskCreateRequest;
import com.minitrello.dto.TaskMoveRequest;
import com.minitrello.dto.TaskResponse;
import com.minitrello.dto.TaskUpdateRequest;
import com.minitrello.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task")
public class TaskController {

  private final TaskService taskService;

  @GetMapping("/api/lists/{listId}/tasks")
  public ResponseEntity<List<TaskResponse>> getTasks(
      Authentication authentication,
      @PathVariable Long listId,
      @RequestParam(required = false) Long assigneeId) {
    return ResponseEntity.ok(
        taskService.getTasksForList(authentication.getName(), listId, assigneeId));
  }

  @PostMapping("/api/lists/{listId}/tasks")
  public ResponseEntity<TaskResponse> createTask(
      Authentication authentication,
      @PathVariable Long listId,
      @Valid @RequestBody TaskCreateRequest request) {
    TaskResponse response = taskService.createTask(authentication.getName(), listId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/api/tasks/{taskId}")
  public ResponseEntity<TaskResponse> getTask(
      Authentication authentication, @PathVariable Long taskId) {
    return ResponseEntity.ok(taskService.getTaskById(authentication.getName(), taskId));
  }

  @PutMapping("/api/tasks/{taskId}")
  public ResponseEntity<TaskResponse> updateTask(
      Authentication authentication,
      @PathVariable Long taskId,
      @Valid @RequestBody TaskUpdateRequest request) {
    return ResponseEntity.ok(taskService.updateTask(authentication.getName(), taskId, request));
  }

  @PatchMapping("/api/tasks/{taskId}/assignee")
  public ResponseEntity<TaskResponse> assignTask(
      Authentication authentication,
      @PathVariable Long taskId,
      @Valid @RequestBody TaskAssigneeRequest request) {
    return ResponseEntity.ok(taskService.assignTask(authentication.getName(), taskId, request));
  }

  @PatchMapping("/api/tasks/{taskId}/move")
  public ResponseEntity<TaskResponse> moveTask(
      Authentication authentication,
      @PathVariable Long taskId,
      @Valid @RequestBody TaskMoveRequest request) {
    return ResponseEntity.ok(taskService.moveTask(authentication.getName(), taskId, request));
  }

  @DeleteMapping("/api/tasks/{taskId}")
  public ResponseEntity<Void> deleteTask(Authentication authentication, @PathVariable Long taskId) {
    taskService.deleteTask(authentication.getName(), taskId);
    return ResponseEntity.noContent().build();
  }
}
