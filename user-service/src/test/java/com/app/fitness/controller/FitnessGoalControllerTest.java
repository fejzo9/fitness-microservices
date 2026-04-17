package com.app.fitness.controller;

import com.app.fitness.dto.FitnessGoalRequest;
import com.app.fitness.dto.FitnessGoalResponse;
import com.app.fitness.exception.GlobalExceptionHandler;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.FitnessGoalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FitnessGoalController.class)
@Import(GlobalExceptionHandler.class)
class FitnessGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FitnessGoalService fitnessGoalService;

    @Test
    void getAll_shouldReturnList() throws Exception {
        List<FitnessGoalResponse> goals = List.of(
                new FitnessGoalResponse(1L, 3L, "Lose Weight", new BigDecimal("75.00"), true, LocalDate.of(2026, 8, 1)));
        when(fitnessGoalService.findAll()).thenReturn(goals);

        mockMvc.perform(get("/api/fitness-goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].goalType").value("Lose Weight"));
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
        FitnessGoalResponse response = new FitnessGoalResponse(1L, 3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));
        when(fitnessGoalService.create(any())).thenReturn(response);

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
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Fitness goal not found with id: 99"))
                .when(fitnessGoalService).delete(99L);

        mockMvc.perform(delete("/api/fitness-goals/99"))
                .andExpect(status().isNotFound());
    }
}
