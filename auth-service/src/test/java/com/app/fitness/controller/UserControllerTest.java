package com.app.fitness.controller;

import com.app.fitness.dto.UserRequest;
import com.app.fitness.dto.UserResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest extends ControllerTestSupport {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 14, 10, 30);

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(userController);
    }

    @Test
    void getAll_shouldReturnListOfUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                UserResponse.builder().id(1L).username("admin1").email("admin1@fitapp.com").roleName("ADMIN").createdAt(CREATED_AT).build(),
                UserResponse.builder().id(2L).username("user1").email("user1@fitapp.com").roleName("USER").createdAt(CREATED_AT).build()));

        mockMvc.perform(get("/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin1"));
    }

    @Test
    void getById_whenUserExists_shouldReturnUser() throws Exception {
        when(userService.findById(1L))
                .thenReturn(UserResponse.builder().id(1L).username("admin1").email("admin1@fitapp.com").roleName("ADMIN").createdAt(CREATED_AT).build());

        mockMvc.perform(get("/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    void getById_whenUserDoesNotExist_shouldReturn404() throws Exception {
        when(userService.findById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/auth/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturnCreatedUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("hash123")
                .roleId(3L)
                .build();
        when(userService.create(any(UserRequest.class)))
                .thenReturn(UserResponse.builder().id(10L).username("newuser").email("newuser@test.com").roleName("USER").createdAt(CREATED_AT).build());

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roleName").value("USER"));
    }

    @Test
    void create_withInvalidEmail_shouldReturn400() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("newuser")
                .email("not-an-email")
                .password("hash123")
                .roleId(3L)
                .build();

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void create_whenUsernameAlreadyExists_shouldReturn409() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("admin1")
                .email("admin1@fitapp.com")
                .password("hash123")
                .roleId(1L)
                .build();
        when(userService.create(any(UserRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists: admin1"));

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturnUpdatedUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("updateduser")
                .email("updated@test.com")
                .password("newhash")
                .roleId(3L)
                .build();
        when(userService.update(eq(1L), any(UserRequest.class)))
                .thenReturn(UserResponse.builder().id(1L).username("updateduser").email("updated@test.com").roleName("USER").createdAt(CREATED_AT).build());

        mockMvc.perform(put("/auth/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    void update_whenUserDoesNotExist_shouldReturn404() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("updateduser")
                .email("updated@test.com")
                .password("newhash")
                .roleId(3L)
                .build();
        when(userService.update(eq(99L), any(UserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(put("/auth/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenUserExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/auth/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenUserDoesNotExist_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: 99")).when(userService).delete(99L);

        mockMvc.perform(delete("/auth/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
