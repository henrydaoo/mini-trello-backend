package com.minitrello.exception;

import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponse {
  Instant timestamp;
  int status;
  String error;
  String message;
  String path;
  private Map<String, String> fieldErrors;
}
