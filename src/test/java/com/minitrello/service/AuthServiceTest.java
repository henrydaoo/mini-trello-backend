package com.minitrello.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.minitrello.dto.AuthResponse;
import com.minitrello.dto.LoginRequest;
import com.minitrello.dto.RegisterRequest;
import com.minitrello.dto.UserResponse;
import com.minitrello.entity.User;
import com.minitrello.exception.UserAlreadyExistsException;
import com.minitrello.repository.UserRepository;
import com.minitrello.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtService jwtService;
  @Mock private UserDetailsService userDetailsService;

  @InjectMocks private AuthService authService;

  private RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    registerRequest = new RegisterRequest("bob", "bob@test.com", "password123");
  }

  @Test
  void register_usernameTaken_throwsUserAlreadyExistsException() {
    when(userRepository.existsByUsername("bob")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));

    verify(userRepository, never()).save(any());
  }

  @Test
  void register_emailTaken_throwsUserAlreadyExistsException() {
    when(userRepository.existsByUsername("bob")).thenReturn(false);
    when(userRepository.existsByEmail("bob@test.com")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));

    verify(userRepository, never()).save(any());
  }

  @Test
  void register_validRequest_encodesPasswordAndSavesUser() {
    when(userRepository.existsByUsername("bob")).thenReturn(false);
    when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            inv -> {
              User u = inv.getArgument(0);
              u.setId(1L);
              return u;
            });

    UserResponse response = authService.register(registerRequest);

    assertThat(response.getUsername()).isEqualTo("bob");
    assertThat(response.getEmail()).isEqualTo("bob@test.com");
  }

  @Test
  void login_validCredentials_returnsTokenAndUser() {
    User user =
        User.builder()
            .id(1L)
            .username("bob")
            .email("bob@test.com")
            .passwordHash("encoded-hash")
            .build();
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.withUsername("bob")
            .password("encoded-hash")
            .authorities("ROLE_USER")
            .build();

    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
    when(userDetailsService.loadUserByUsername("bob")).thenReturn(userDetails);
    when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

    AuthResponse response = authService.login(new LoginRequest("bob", "password123"));

    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getUser().getUsername()).isEqualTo("bob");
  }

  @Test
  void login_badCredentials_propagatesException_andNeverGeneratesToken() {
    doThrow(new BadCredentialsException("Bad credentials"))
        .when(authenticationManager)
        .authenticate(any());

    assertThrows(
        BadCredentialsException.class,
        () -> authService.login(new LoginRequest("bob", "wrong-password")));

    verify(jwtService, never()).generateToken(any(UserDetails.class));
  }
}
