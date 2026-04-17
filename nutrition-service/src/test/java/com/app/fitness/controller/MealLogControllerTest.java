package com.app.fitness.controller;

import com.app.fitness.dto.MealLogRequest;
import com.app.fitness.dto.MealLogResponse;
import com.app.fitness.exception.GlobalExceptionHandler;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.MealLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealLogController.class)
@Import(GlobalExceptionHandler.class)
class MealLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MealLogService mealLogService;

    @Test
    void getAll_shouldReturnList() throws Exception {
        List<MealLogResponse> logs = List.of(
                new MealLogResponse(1L, 3L, LocalDate.of(2026, 4, 10), "BREAKFAST"));
        when(mealLogService.findAll()).thenReturn(logs);

        mockMvc.perform(get("/api/meal-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mealType").value("BREAKFAST"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(mealLogService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Meal log not found with id: 99"));

        mockMvc.perform(get("/api/meal-logs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 11), "LUNCH");
        MealLogResponse response = new MealLogResponse(2L, 3L, LocalDate.of(2026, 4, 11), "LUNCH");
        when(mealLogService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/meal-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mealType").value("LUNCH"));
    }

    @Test
    void create_withMissingMealType_shouldReturn400() throws Exception {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 11), "");

        mockMvc.perform(post("/api/meal-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_withNullUserId_shouldReturn400() throws Exception {
        MealLogRequest request = new MealLogRequest(null, LocalDate.of(2026, 4, 11), "LUNCH");

        mockMvc.perform(post("/api/meal-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Meal log not found with id: 99"))
                .when(mealLogService).delete(99L);

        mockMvc.perform(delete("/api/meal-logs/99"))
                .andExpect(status().isNotFound());
    }
}
