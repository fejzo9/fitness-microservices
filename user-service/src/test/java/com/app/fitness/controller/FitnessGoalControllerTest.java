package com.app.fitness.controller;

import com.app.fitness.dto.FitnessGoalRequest;
import com.app.fitness.dto.FitnessGoalResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.FitnessGoalService;
import java.math.BigDecimal;
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
class FitnessGoalControllerTest extends ControllerTestSupport {

    @Mock
    private FitnessGoalService fitnessGoalService;

    @InjectMocks
    private FitnessGoalController fitnessGoalController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(fitnessGoalController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(fitnessGoalService.findAll()).thenReturn(List.of(
                new FitnessGoalResponse(1L, 3L, "Lose Weight", new BigDecimal("75.00"), true,
                        LocalDate.of(2026, 8, 1))));

        mockMvc.perform(get("/api/fitness-goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].goalType").value("Lose Weight"));
    }

    @Test
    void getById_whenGoalExists_shouldReturnGoal() throws Exception {
        when(fitnessGoalService.findById(1L)).thenReturn(
                new FitnessGoalResponse(1L, 3L, "Lose Weight", new BigDecimal("75.00"), true,
                        LocalDate.of(2026, 8, 1)));

        mockMvc.perform(get("/api/fitness-goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalType").value("Lose Weight"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(fitnessGoalService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Fitness goal not found with id: 99"));

        mockMvc.perform(get("/api/fitness-goals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));
        when(fitnessGoalService.create(any(FitnessGoalRequest.class))).thenReturn(
                new FitnessGoalResponse(1L, 3L, "Lose Weight", new BigDecimal("75.00"), true,
                        LocalDate.of(2026, 8, 1)));

        mockMvc.perform(post("/api/fitness-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalType").value("Lose Weight"));
    }

    @Test
    void create_withMissingUserId_shouldReturn400() throws Exception {
        FitnessGoalRequest request = new FitnessGoalRequest(null, "Lose Weight", null, true, null);

        mockMvc.perform(post("/api/fitness-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));
        when(fitnessGoalService.create(any(FitnessGoalRequest.class)))
                .thenThrow(new DuplicateResourceException("Fitness goal already exists for userId=3, goalType=Lose Weight"));

        mockMvc.perform(post("/api/fitness-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Gain Muscle", new BigDecimal("82.00"), true,
                LocalDate.of(2026, 10, 1));
        when(fitnessGoalService.update(eq(1L), any(FitnessGoalRequest.class))).thenReturn(
                new FitnessGoalResponse(1L, 3L, "Gain Muscle", new BigDecimal("82.00"), true,
                        LocalDate.of(2026, 10, 1)));

        mockMvc.perform(put("/api/fitness-goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalType").value("Gain Muscle"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Gain Muscle", new BigDecimal("82.00"), true,
                LocalDate.of(2026, 10, 1));
        when(fitnessGoalService.update(eq(99L), any(FitnessGoalRequest.class)))
                .thenThrow(new ResourceNotFoundException("Fitness goal not found with id: 99"));

        mockMvc.perform(put("/api/fitness-goals/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/fitness-goals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Fitness goal not found with id: 99"))
                .when(fitnessGoalService).delete(99L);

        mockMvc.perform(delete("/api/fitness-goals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
