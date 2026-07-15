package com.minitrello.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
  @NotBlank(message = "Content is required")
  @Size(max = 2000, message = "Content must be at most 2000 characters")
  private String content;
}
