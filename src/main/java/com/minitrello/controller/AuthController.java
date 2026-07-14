package com.minitrello.controller;

import com.minitrello.dto.AuthResponse;
import com.minitrello.dto.LoginRequest;
import com.minitrello.dto.RegisterRequest;
import com.minitrello.dto.UserResponse;
import com.minitrello.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

              @PostMapping("/register")
              public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
                UserResponse response = authService.register(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
              }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }
}
