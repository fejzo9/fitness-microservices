package com.app.fitness.controller;

import com.app.fitness.dto.UserRequest;
import com.app.fitness.dto.UserResponse;
import com.app.fitness.exception.GlobalExceptionHandler;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getAll_shouldReturnListOfUsers() throws Exception {
        List<UserResponse> users = List.of(
                new UserResponse(1L, "admin1", "admin1@fitapp.com", "ADMIN", LocalDateTime.now()),
                new UserResponse(2L, "user1", "user1@fitapp.com", "USER", LocalDateTime.now()));
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin1"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.findById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        UserRequest request = new UserRequest("newuser", "newuser@test.com", "hash123", 3L);
        UserResponse response = new UserResponse(10L, "newuser", "newuser@test.com", "USER", LocalDateTime.now());
        when(userService.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roleName").value("USER"));
    }

    @Test
    void create_withInvalidEmail_shouldReturn400() throws Exception {
        UserRequest request = new UserRequest("newuser", "not-an-email", "hash123", 3L);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void create_withMissingFields_shouldReturn400() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        UserRequest request = new UserRequest("updateduser", "updated@test.com", "newhash", 3L);
        UserResponse response = new UserResponse(1L, "updateduser", "updated@test.com", "USER", LocalDateTime.now());
        when(userService.update(eq(1L), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
