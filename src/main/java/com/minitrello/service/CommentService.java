package com.minitrello.service;

import com.minitrello.dto.CommentRequest;
import com.minitrello.dto.CommentResponse;
import com.minitrello.entity.Comment;
import com.minitrello.entity.Task;
import com.minitrello.entity.User;
import com.minitrello.exception.CommentNotFoundException;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.TaskNotFoundException;
import com.minitrello.repository.CommentRepository;
import com.minitrello.repository.TaskRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
  private final CommentRepository commentRepository;
  private final TaskRepository taskRepository;
  private final BoardAccessService boardAccessService;

  @Transactional(readOnly = true)
  public List<CommentResponse> getCommentsForTask(String username, Long taskId) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(task.getList().getBoard(), currentUser);

    return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
        .map(this::toResponse)
        .toList();
  }

  public CommentResponse addComment(String username, Long taskId, CommentRequest request) {
    Task task = getTaskOrThrow(taskId);
    User currentUser = boardAccessService.getUserByUsername(username);
    boardAccessService.assertHasAccess(task.getList().getBoard(), currentUser);

    Comment comment =
        Comment.builder().task(task).user(currentUser).content(request.getContent()).build();

    return toResponse(commentRepository.save(comment));
  }

  public void deleteComment(String username, Long commentId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(
                () -> new CommentNotFoundException("Comment not found with id " + commentId));
    User currentUser = boardAccessService.getUserByUsername(username);

    boardAccessService.assertHasAccess(comment.getTask().getList().getBoard(), currentUser);
    if (!comment.getUser().getId().equals(currentUser.getId())) {
      throw new ForbiddenOperationException("Only the comment author can delete this comment");
    }

    commentRepository.delete(comment);
  }

  private Task getTaskOrThrow(Long taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with id " + taskId));
  }

  private CommentResponse toResponse(Comment comment) {
    return CommentResponse.builder()
        .id(comment.getId())
        .taskId(comment.getTask().getId())
        .userId(comment.getUser().getId())
        .username(comment.getUser().getUsername())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt())
        .build();
  }
}
