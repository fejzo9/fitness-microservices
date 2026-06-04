package com.app.fitness.controller;

import com.app.fitness.dto.CompletedWorkoutRequest;
import com.app.fitness.dto.CompletedWorkoutResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.CompletedWorkoutService;
import com.app.fitness.exception.ServiceUnavailableException;
import java.time.LocalDate;
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
class CompletedWorkoutControllerTest extends ControllerTestSupport {

    @Mock
    private CompletedWorkoutService completedWorkoutService;

    @InjectMocks
    private CompletedWorkoutController completedWorkoutController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(completedWorkoutController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(completedWorkoutService.findAll()).thenReturn(List.of(
                new CompletedWorkoutResponse(1L, 3L, LocalDate.of(2026, 5, 10), 60)));

        mockMvc.perform(get("/api/completed-workouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].durationMin").value(60));
    }

    @Test
    void getByUserId_shouldReturnUserCompletedWorkouts() throws Exception {
        when(completedWorkoutService.findByUserId(3L)).thenReturn(List.of(
                new CompletedWorkoutResponse(1L, 3L, LocalDate.of(2026, 5, 10), 60)));

        mockMvc.perform(get("/api/completed-workouts/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(3));
    }

    @Test
    void getById_whenWorkoutExists_shouldReturnWorkout() throws Exception {
        when(completedWorkoutService.findById(1L))
                .thenReturn(new CompletedWorkoutResponse(1L, 3L, LocalDate.of(2026, 5, 10), 60));

        mockMvc.perform(get("/api/completed-workouts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(completedWorkoutService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Completed workout not found with id: 99"));

        mockMvc.perform(get("/api/completed-workouts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        CompletedWorkoutRequest request = new CompletedWorkoutRequest(3L, LocalDate.of(2026, 5, 10), 60);
        when(completedWorkoutService.create(any(CompletedWorkoutRequest.class)))
                .thenReturn(new CompletedWorkoutResponse(1L, 3L, LocalDate.of(2026, 5, 10), 60));

        mockMvc.perform(post("/api/completed-workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(3));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        CompletedWorkoutRequest request = new CompletedWorkoutRequest(null, null, 60);

        mockMvc.perform(post("/api/completed-workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        CompletedWorkoutRequest request = new CompletedWorkoutRequest(3L, LocalDate.of(2026, 5, 11), 75);
        when(completedWorkoutService.update(eq(1L), any(CompletedWorkoutRequest.class)))
                .thenReturn(new CompletedWorkoutResponse(1L, 3L, LocalDate.of(2026, 5, 11), 75));

        mockMvc.perform(put("/api/completed-workouts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMin").value(75));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        CompletedWorkoutRequest request = new CompletedWorkoutRequest(3L, LocalDate.of(2026, 5, 11), 75);
        when(completedWorkoutService.update(eq(99L), any(CompletedWorkoutRequest.class)))
                .thenThrow(new ResourceNotFoundException("Completed workout not found with id: 99"));

        mockMvc.perform(put("/api/completed-workouts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/completed-workouts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Completed workout not found with id: 99"))
                .when(completedWorkoutService).delete(99L);

        mockMvc.perform(delete("/api/completed-workouts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_whenAuthServiceUnavailable_shouldReturn503() throws Exception {
        CompletedWorkoutRequest request = new CompletedWorkoutRequest(3L, LocalDate.of(2026, 5, 10), 60);
        when(completedWorkoutService.create(any(CompletedWorkoutRequest.class)))
                .thenThrow(new ServiceUnavailableException("auth-service not available. Try again"));

        mockMvc.perform(post("/api/completed-workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("SERVICE_UNAVAILABLE"));
    }

    @Test
    void create_whenUserNotFoundInAuthService_shouldReturn404() throws Exception {
        CompletedWorkoutRequest request = new CompletedWorkoutRequest(99L, LocalDate.of(2026, 5, 10), 60);
        when(completedWorkoutService.create(any(CompletedWorkoutRequest.class)))
                .thenThrow(new ResourceNotFoundException("User with ID=99 doesn't exist"));

        mockMvc.perform(post("/api/completed-workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User with ID=99 doesn't exist"));
    }
}
