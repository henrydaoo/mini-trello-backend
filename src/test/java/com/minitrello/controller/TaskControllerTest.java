package com.minitrello.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minitrello.config.SecurityConfig;
import com.minitrello.dto.TaskAssigneeRequest;
import com.minitrello.dto.TaskCreateRequest;
import com.minitrello.dto.TaskResponse;
import com.minitrello.entity.Priority;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.TaskNotFoundException;
import com.minitrello.security.JwtService;
import com.minitrello.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
public class TaskControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private TaskService taskService;
  @MockBean private JwtService jwtService;
  @MockBean private UserDetailsService userDetailsService;

  private TaskResponse sampleTask(Long id) {
    return TaskResponse.builder()
        .id(id)
        .listId(100L)
        .title("Setup CI pipeline")
        .priority(Priority.HIGH)
        .position(0)
        .build();
  }

  @Test
  void getTasks_withoutAuthentication_returns403() throws Exception {
    mockMvc.perform((get("/api/lists/100/tasks"))).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "owner")
  void getTasks_authenticated_returns200WithList() throws Exception {
    when(taskService.getTasksForList(eq("owner"), eq(100L), eq(null)))
        .thenReturn(List.of(sampleTask(1L)));

    mockMvc
        .perform(get("/api/lists/100/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].title").value("Setup CI pipeline"));
  }

  @Test
  @WithMockUser(username = "owner")
  void createTask_validRequest_returns201() throws Exception {
    TaskCreateRequest request =
        new TaskCreateRequest("Setup CI pipeline", null, null, Priority.HIGH, null);

    when(taskService.createTask(eq("owner"), eq(100L), any(TaskCreateRequest.class)))
        .thenReturn(sampleTask(10L));

    mockMvc
        .perform(
            post("/api/lists/100/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Setup CI pipeline"));
  }

  @Test
  @WithMockUser(username = "owner")
  void createTask_blankTitle_returns400WithFieldErrors() throws Exception {
    TaskCreateRequest request = new TaskCreateRequest("", null, null, Priority.HIGH, null);
    mockMvc
        .perform(
            post("/api/lists/100/tasks")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.fieldErrors.title").exists());
  }

  @Test
  @WithMockUser(username = "owner")
  void getTask_notFound_returns404WithErrorBody() throws Exception {
    when(taskService.getTaskById("owner", 999L))
        .thenThrow(new TaskNotFoundException("Task not found with id 999"));

    mockMvc
        .perform(get("/api/tasks/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Task not found with id 999"))
        .andExpect(jsonPath("$.path").value("/api/tasks/999"));
  }

  @Test
  @WithMockUser(username = "outsider")
  void assignTask_userForbidden_returns403() throws Exception {
    when(taskService.assignTask(eq("outsider"), eq(500L), any(TaskAssigneeRequest.class)))
        .thenThrow(
            new ForbiddenOperationException(
                "Only the board owner or a board member can be assigned to a task"));

    mockMvc
        .perform(
            patch("/api/tasks/500/assignee")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new TaskAssigneeRequest(3L))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("FORBIDDEN"));
  }

  @Test
  @WithMockUser(username = "owner")
  void assignTask_valid_returns200() throws Exception {
    TaskResponse response = sampleTask(500L);
    when(taskService.assignTask(eq("owner"), eq(500L), any(TaskAssigneeRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            patch("/api/tasks/500/assignee")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(new TaskAssigneeRequest(2L))))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "owner")
  void deleteTask_returns204() throws Exception {
    mockMvc.perform(delete("/api/tasks/500")).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "owner")
  void getTask_invalidIdType_returns400() throws Exception {
    mockMvc
        .perform(get("/api/tasks/not-a-number"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
  }
}
