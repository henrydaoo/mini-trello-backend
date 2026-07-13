package com.minitrello.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

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
}
