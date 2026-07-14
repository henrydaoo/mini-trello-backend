package com.minitrello.dto;

import com.minitrello.entity.Priority;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TaskResponse {
  private Long id;
  private Long listId;
  private String title;
  private String description;
  private Long assigneeId;
  private String assigneeUsername;
  private Priority priority;
  private LocalDate dueDate;
  private Integer position;
  private Instant createdAt;
  private Instant updatedAt;
}
