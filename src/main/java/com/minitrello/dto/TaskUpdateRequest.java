package com.minitrello.dto;

import com.minitrello.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {
  @NotBlank(message = "Title is required")
  @Size(max = 200, message = "Title must be at most 200 characters")
  private String title;

  private String description;

  private Priority priority;

  private LocalDate dueDate;
}
