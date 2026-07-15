package com.minitrello.event;

import lombok.Getter;

@Getter
public class TaskAssignedEvent {
  private final Long taskId;
  private final Long assigneeId;

  public TaskAssignedEvent(Long taskId, Long assigneeId) {
    this.taskId = taskId;
    this.assigneeId = assigneeId;
  }
}
