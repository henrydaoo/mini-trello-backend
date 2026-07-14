package com.minitrello.service;

import com.minitrello.dto.AuthResponse;
import com.minitrello.dto.LoginRequest;
import com.minitrello.dto.RegisterRequest;
import com.minitrello.dto.UserResponse;
import com.minitrello.entity.User;
import com.minitrello.exception.UserAlreadyExistsException;
import com.minitrello.repository.UserRepository;
import com.minitrello.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;

  @Transactional
  public UserResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new UserAlreadyExistsException("Username is already taken: " + request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("Email is already registered: " + request.getEmail());
    }

    User user =
        User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();

    User saved = userRepository.save(user);
    return UserResponse.fromEntity(saved);
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    User user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Authenticated user not found in database: " + request.getUsername()));

    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
    String token = jwtService.generateToken(userDetails);

    return AuthResponse.builder().token(token).user(UserResponse.fromEntity(user)).build();
  }
}
