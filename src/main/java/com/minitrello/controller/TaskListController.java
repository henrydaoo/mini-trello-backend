package com.minitrello.controller;

import com.minitrello.dto.TaskListPositionRequest;
import com.minitrello.dto.TaskListRequest;
import com.minitrello.dto.TaskListResponse;
import com.minitrello.service.TaskListService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TaskListController {

  private final TaskListService taskListService;

  @GetMapping("/api/boards/{boardId}/lists")
  public ResponseEntity<List<TaskListResponse>> getLists(
      Authentication authentication, @PathVariable Long boardId) {
    return ResponseEntity.ok(taskListService.getListsForBoard(authentication.getName(), boardId));
  }

  @PostMapping("/api/boards/{boardId}/lists")
  public ResponseEntity<TaskListResponse> createList(
      Authentication authentication,
      @PathVariable Long boardId,
      @Valid @RequestBody TaskListRequest request) {
    TaskListResponse response =
        taskListService.createList(authentication.getName(), boardId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/api/lists/{listId}")
  public ResponseEntity<TaskListResponse> updateList(
      Authentication authentication,
      @PathVariable Long listId,
      @Valid @RequestBody TaskListRequest request) {
    return ResponseEntity.ok(taskListService.updateList(authentication.getName(), listId, request));
  }

  @PatchMapping("/api/lists/{listId}/position")
  public ResponseEntity<TaskListResponse> updatePosition(
      Authentication authentication,
      @PathVariable Long listId,
      @Valid @RequestBody TaskListPositionRequest request) {
    return ResponseEntity.ok(
        taskListService.updatePosition(authentication.getName(), listId, request));
  }

  @DeleteMapping("/api/lists/{listId}")
  public ResponseEntity<Void> deleteList(Authentication authentication, @PathVariable Long listId) {
    taskListService.deleteList(authentication.getName(), listId);
    return ResponseEntity.noContent().build();
  }
}
