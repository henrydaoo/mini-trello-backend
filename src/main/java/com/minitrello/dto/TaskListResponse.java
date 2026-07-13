package com.minitrello.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class TaskListResponse {
    private Long id;
    private Long boardId;
    private String name;
    private Integer position;
    private Instant createdAt;
}
