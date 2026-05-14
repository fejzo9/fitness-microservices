package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.WorkoutExerciseService;
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
class WorkoutExerciseControllerTest extends ControllerTestSupport {

    @Mock
    private WorkoutExerciseService workoutExerciseService;

    @InjectMocks
    private WorkoutExerciseController workoutExerciseController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(workoutExerciseController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(workoutExerciseService.findAll()).thenReturn(List.of(
                new WorkoutExerciseResponse(1L, 7L, 2L, "Squat", 4, 8, 120)));

        mockMvc.perform(get("/api/workout-exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].exerciseName").value("Squat"));
    }

    @Test
    void getById_whenEntryExists_shouldReturnEntry() throws Exception {
        when(workoutExerciseService.findById(1L))
                .thenReturn(new WorkoutExerciseResponse(1L, 7L, 2L, "Squat", 4, 8, 120));

        mockMvc.perform(get("/api/workout-exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restSec").value(120));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(workoutExerciseService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Workout exercise not found with id: 99"));

        mockMvc.perform(get("/api/workout-exercises/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        WorkoutExerciseRequest request = new WorkoutExerciseRequest(7L, 2L, 4, 8, 120);
        when(workoutExerciseService.create(any(WorkoutExerciseRequest.class)))
                .thenReturn(new WorkoutExerciseResponse(1L, 7L, 2L, "Squat", 4, 8, 120));

        mockMvc.perform(post("/api/workout-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseId").value(2));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        WorkoutExerciseRequest request = new WorkoutExerciseRequest(null, null, 4, 8, 120);

        mockMvc.perform(post("/api/workout-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        WorkoutExerciseRequest request = new WorkoutExerciseRequest(7L, 2L, 4, 8, 120);
        when(workoutExerciseService.create(any(WorkoutExerciseRequest.class)))
                .thenThrow(new DuplicateResourceException("Exercise already assigned to this workout day"));

        mockMvc.perform(post("/api/workout-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        WorkoutExerciseRequest request = new WorkoutExerciseRequest(7L, 2L, 5, 6, 90);
        when(workoutExerciseService.update(eq(1L), any(WorkoutExerciseRequest.class)))
                .thenReturn(new WorkoutExerciseResponse(1L, 7L, 2L, "Squat", 5, 6, 90));

        mockMvc.perform(put("/api/workout-exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").value(5));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        WorkoutExerciseRequest request = new WorkoutExerciseRequest(7L, 2L, 5, 6, 90);
        when(workoutExerciseService.update(eq(99L), any(WorkoutExerciseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Workout exercise not found with id: 99"));

        mockMvc.perform(put("/api/workout-exercises/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/workout-exercises/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Workout exercise not found with id: 99"))
                .when(workoutExerciseService).delete(99L);

        mockMvc.perform(delete("/api/workout-exercises/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
