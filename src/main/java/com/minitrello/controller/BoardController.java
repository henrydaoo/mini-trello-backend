package com.minitrello.controller;

import com.minitrello.dto.AddMemberRequest;
import com.minitrello.dto.BoardRequest;
import com.minitrello.dto.BoardResponse;
import com.minitrello.service.BoardService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
  private final BoardService boardService;

  @GetMapping
  public ResponseEntity<List<BoardResponse>> getBoards(Authentication authentication) {
    return ResponseEntity.ok(boardService.getBoardsForCurrentUser(authentication.getName()));
  }

  @PostMapping
  public ResponseEntity<BoardResponse> createBoard(
      Authentication authentication, @Valid @RequestBody BoardRequest request) {
    BoardResponse response = boardService.createBoard(authentication.getName(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BoardResponse> getBoard(
      Authentication authentication, @PathVariable Long id) {
    return ResponseEntity.ok(boardService.getBoardById(authentication.getName(), id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BoardResponse> updateBoard(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody BoardRequest request) {
    return ResponseEntity.ok(boardService.updateBoard(authentication.getName(), id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBoard(Authentication authentication, @PathVariable Long id) {
    boardService.deleteBoard(authentication.getName(), id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/members")
  public ResponseEntity<BoardResponse> addMember(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody AddMemberRequest request) {
    BoardResponse response = boardService.addMember(authentication.getName(), id, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}/members/{userId}")
  public ResponseEntity<Void> removeMember(
      Authentication authentication, @PathVariable Long id, @PathVariable Long userId) {
    boardService.removeMember(authentication.getName(), id, userId);
    return ResponseEntity.noContent().build();
  }
}
