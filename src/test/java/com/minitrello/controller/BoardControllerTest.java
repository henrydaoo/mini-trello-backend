package com.minitrello.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minitrello.config.SecurityConfig;
import com.minitrello.dto.AddMemberRequest;
import com.minitrello.dto.BoardRequest;
import com.minitrello.dto.BoardResponse;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.MemberAlreadyExistsException;
import com.minitrello.security.JwtService;
import com.minitrello.service.BoardService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
public class BoardControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private BoardService boardService;
  @MockBean private JwtService jwtService;
  @MockBean private UserDetailsService userDetailsService;

  private BoardResponse sampleBoard(Long id) {
    return BoardResponse.builder()
        .id(id)
        .name("Board A")
        .description("desc")
        .ownerId(1L)
        .ownerUsername("owner")
        .members(List.of())
        .build();
  }

  @Test
  @WithMockUser(username = "owner")
  void getBoards_returns200WithBoardsOfCurrentUser() throws Exception {
    when(boardService.getBoardsForCurrentUser("owner")).thenReturn(List.of(sampleBoard(10L)));

    mockMvc
        .perform(get("/api/boards"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].name").value("Board A"));
  }

  @Test
  @WithMockUser(username = "owner")
  void createBoard_validRequest_returns201() throws Exception {
    when(boardService.createBoard(eq("owner"), any(BoardRequest.class)))
        .thenReturn(sampleBoard(10L));

    mockMvc
        .perform(
            post("/api/boards")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new BoardRequest("Board A", "desc"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(10));
  }

  @Test
  @WithMockUser(username = "owner")
  void createBoard_blankName_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/boards")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new BoardRequest("", "desc"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.name").exists());
  }

  @Test
  @WithMockUser(username = "not-owner")
  void updateBoard_nonOwner_returns403() throws Exception {
    when(boardService.updateBoard(eq("not-owner"), eq(10L), any(BoardRequest.class)))
        .thenThrow(new ForbiddenOperationException("Only the board owner can perform this action"));

    mockMvc
        .perform(
            put("/api/boards/10")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new BoardRequest("New name", "desc"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("FORBIDDEN"));
  }

  @Test
  @WithMockUser(username = "owner")
  void deleteBoard_returns204() throws Exception {
    mockMvc.perform(delete("/api/boards/10")).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "owner")
  void addMember_alreadyMember_returns409() throws Exception {
    when(boardService.addMember(eq("owner"), eq(10L), any(AddMemberRequest.class)))
        .thenThrow(new MemberAlreadyExistsException("This user is already a member of the board"));

    mockMvc
        .perform(
            post("/api/boards/10/members")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new AddMemberRequest("jane@example.com"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("CONFLICT"));
  }

  @Test
  @WithMockUser(username = "owner")
  void addMember_invalidEmail_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/boards/10/members")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new AddMemberRequest("not-an-email"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.email").exists());
  }
}
