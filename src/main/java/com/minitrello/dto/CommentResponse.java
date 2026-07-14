package com.minitrello.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {
  private Long id;
  private Long taskId;
  private Long userId;
  private String username;
  private String content;
  private Instant createdAt;
}
