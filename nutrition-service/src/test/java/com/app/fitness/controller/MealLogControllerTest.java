package com.app.fitness.controller;

import com.app.fitness.dto.MealLogRequest;
import com.app.fitness.dto.MealLogResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.MealLogService;
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
class MealLogControllerTest extends ControllerTestSupport {

    @Mock
    private MealLogService mealLogService;

    @InjectMocks
    private MealLogController mealLogController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(mealLogController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(mealLogService.findAll()).thenReturn(List.of(
                new MealLogResponse(1L, 3L, LocalDate.of(2026, 4, 10), "BREAKFAST")));

        mockMvc.perform(get("/api/meal-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mealType").value("BREAKFAST"));
    }

    @Test
    void getById_whenLogExists_shouldReturnLog() throws Exception {
        when(mealLogService.findById(1L))
                .thenReturn(new MealLogResponse(1L, 3L, LocalDate.of(2026, 4, 10), "BREAKFAST"));

        mockMvc.perform(get("/api/meal-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mealType").value("BREAKFAST"));
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
        when(mealLogService.create(any(MealLogRequest.class)))
                .thenReturn(new MealLogResponse(2L, 3L, LocalDate.of(2026, 4, 11), "LUNCH"));

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
    void create_whenDuplicate_shouldReturn409() throws Exception {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 10), "BREAKFAST");
        when(mealLogService.create(any(MealLogRequest.class)))
                .thenThrow(new DuplicateResourceException("Meal log already exists for userId=3, date=2026-04-10, mealType=BREAKFAST"));

        mockMvc.perform(post("/api/meal-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 11), "DINNER");
        when(mealLogService.update(eq(1L), any(MealLogRequest.class)))
                .thenReturn(new MealLogResponse(1L, 3L, LocalDate.of(2026, 4, 11), "DINNER"));

        mockMvc.perform(put("/api/meal-logs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealType").value("DINNER"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 11), "DINNER");
        when(mealLogService.update(eq(99L), any(MealLogRequest.class)))
                .thenThrow(new ResourceNotFoundException("Meal log not found with id: 99"));

        mockMvc.perform(put("/api/meal-logs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/meal-logs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Meal log not found with id: 99"))
                .when(mealLogService).delete(99L);

        mockMvc.perform(delete("/api/meal-logs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
