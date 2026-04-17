package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.GlobalExceptionHandler;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(ExerciseController.class)
@Import(GlobalExceptionHandler.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExerciseService exerciseService;

    @Test
    void getAll_shouldReturnList() throws Exception {
        List<ExerciseResponse> exercises = List.of(
                new ExerciseResponse(1L, "Bench Press", "Chest exercise", "INTERMEDIATE"));
        when(exerciseService.findAll()).thenReturn(exercises);

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bench Press"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(exerciseService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Exercise not found with id: 99"));

        mockMvc.perform(get("/api/exercises/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        ExerciseRequest request = new ExerciseRequest("Squat", "Leg exercise", "BEGINNER");
        ExerciseResponse response = new ExerciseResponse(2L, "Squat", "Leg exercise", "BEGINNER");
        when(exerciseService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Squat"));
    }

    @Test
    void create_withBlankName_shouldReturn400() throws Exception {
        ExerciseRequest request = new ExerciseRequest("", "desc", "EASY");

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        ExerciseRequest request = new ExerciseRequest("Bench Press", null, null);
        when(exerciseService.create(any())).thenThrow(
                new DuplicateResourceException("Exercise already exists with name: Bench Press"));

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        ExerciseRequest request = new ExerciseRequest("Bench Press Updated", null, "ADVANCED");
        ExerciseResponse response = new ExerciseResponse(1L, "Bench Press Updated", null, "ADVANCED");
        when(exerciseService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bench Press Updated"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/exercises/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Exercise not found with id: 99")).when(exerciseService).delete(99L);

        mockMvc.perform(delete("/api/exercises/99"))
                .andExpect(status().isNotFound());
    }
}
