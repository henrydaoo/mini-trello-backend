package com.minitrello.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequest {
  @NotBlank(message = "Board name is required")
  @Size(max = 100, message = "Board name must be at most 100 characters")
  private String name;

  @Size(max = 2000, message = "Description must be at most 2000 characters")
  private String description;
}
