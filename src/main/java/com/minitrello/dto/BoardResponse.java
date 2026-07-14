package com.minitrello.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoardResponse {
  private Long id;
  private String name;
  private String description;
  private Long ownerId;
  private String ownerUsername;
  private List<BoardMemberResponse> members;
  private Instant createdAt;
  private Instant updatedAt;
}
