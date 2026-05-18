package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.app.fitness.dto.PageResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.ExerciseService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class ExerciseControllerTest extends ControllerTestSupport {

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private ExerciseController exerciseController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(exerciseController);
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        List<ExerciseResponse> exercises = List.of(
                ExerciseResponse.builder()
                        .id(1L)
                        .name("Bench Press")
                        .description("Chest exercise")
                        .difficulty("INTERMEDIATE")
                        .build());
        Pageable pageable = PageRequest.of(0, 10);
        when(exerciseService.findAll(any(Pageable.class)))
                .thenReturn(PageResponse.of(new PageImpl<>(exercises, pageable, 1)));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Bench Press"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getById_whenExerciseExists_shouldReturnExercise() throws Exception {
        when(exerciseService.findById(1L))
                .thenReturn(ExerciseResponse.builder()
                        .id(1L)
                        .name("Bench Press")
                        .description("Chest exercise")
                        .difficulty("INTERMEDIATE")
                        .build());

        mockMvc.perform(get("/api/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bench Press"));
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
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Squat")
                .description("Leg exercise")
                .difficulty("BEGINNER")
                .build();
        when(exerciseService.create(any(ExerciseRequest.class)))
                .thenReturn(ExerciseResponse.builder()
                        .id(2L)
                        .name("Squat")
                        .description("Leg exercise")
                        .difficulty("BEGINNER")
                        .build());

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Squat"));
    }

    @Test
    void create_withBlankName_shouldReturn400() throws Exception {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("")
                .description("desc")
                .difficulty("EASY")
                .build();

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Bench Press")
                .build();
        when(exerciseService.create(any(ExerciseRequest.class)))
                .thenThrow(new DuplicateResourceException("Exercise already exists with name: Bench Press"));

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Bench Press Updated")
                .difficulty("ADVANCED")
                .build();
        when(exerciseService.update(eq(1L), any(ExerciseRequest.class)))
                .thenReturn(ExerciseResponse.builder()
                        .id(1L)
                        .name("Bench Press Updated")
                        .difficulty("ADVANCED")
                        .build());

        mockMvc.perform(put("/api/exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bench Press Updated"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Bench Press Updated")
                .difficulty("ADVANCED")
                .build();
        when(exerciseService.update(eq(99L), any(ExerciseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exercise not found with id: 99"));

        mockMvc.perform(put("/api/exercises/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
