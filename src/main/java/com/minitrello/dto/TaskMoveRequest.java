package com.minitrello.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskMoveRequest {
    @NotNull(message = "Target list id is required")
    private Long targetListId;

    @NotNull(message = "Position is required")
    @PositiveOrZero(message = "Position must be zero or a positive number")
    private Integer position;
}
