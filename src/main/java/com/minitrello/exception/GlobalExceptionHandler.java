package com.minitrello.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // ---- 401 Unauthorized ----

  @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      RuntimeException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
  }

  // ---- 404 Not Found ----

  @ExceptionHandler({
    BoardNotFoundException.class,
    UserNotFoundException.class,
    BoardMemberNotFoundException.class,
    TaskListNotFoundException.class,
    TaskNotFoundException.class,
    NotificationNotFoundException.class,
    CommentNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(
      RuntimeException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  // ---- 403 Forbidden ----

  @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleForbidden(
      RuntimeException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  // ---- 409 Conflict ----

  @ExceptionHandler({MemberAlreadyExistsException.class, UserAlreadyExistsException.class})
  public ResponseEntity<ErrorResponse> handleConflict(
      RuntimeException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  // ---- 400 Bad Request ----

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }
    return buildResponse(HttpStatus.BAD_REQUEST, "Request validation failed", request, fieldErrors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMalformedJson(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or missing request body", request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String message = "Invalid value for parameter '" + ex.getName() + "'";
    return buildResponse(HttpStatus.BAD_REQUEST, message, request);
  }

  // ---- 500 Internal Server Error (fallback) ----

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
  }

  // ---- helpers ----

  private ResponseEntity<ErrorResponse> buildResponse(
      HttpStatus status, String message, HttpServletRequest request) {
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase().toUpperCase().replace(" ", "_"))
            .message(message)
            .path(request.getRequestURI())
            .build();
    return ResponseEntity.status(status).body(error);
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      Map<String, String> fieldErrors) {
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase().toUpperCase().replace(" ", "_"))
            .message(message)
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();
    return ResponseEntity.status(status).body(error);
  }
}
