package com.minitrello.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);

    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(RuntimeException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_ERROR");
        body.put("message", "Request validation failed");
        body.put("path", request.getRequestURI());
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }


    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase().toUpperCase().replace(" ", "_"))
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }


}
