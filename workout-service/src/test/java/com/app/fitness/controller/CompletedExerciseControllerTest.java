package com.app.fitness.controller;

import com.app.fitness.dto.CompletedExerciseRequest;
import com.app.fitness.dto.CompletedExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.CompletedExerciseService;
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
class CompletedExerciseControllerTest extends ControllerTestSupport {

    @Mock
    private CompletedExerciseService completedExerciseService;

    @InjectMocks
    private CompletedExerciseController completedExerciseController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(completedExerciseController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(completedExerciseService.findAll()).thenReturn(List.of(
                new CompletedExerciseResponse(1L, 4L, 2L, "Squat", 4, 8)));

        mockMvc.perform(get("/api/completed-exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].exerciseName").value("Squat"));
    }

    @Test
    void getByExerciseId_shouldReturnExerciseHistory() throws Exception {
        when(completedExerciseService.findByExerciseId(2L)).thenReturn(List.of(
                new CompletedExerciseResponse(1L, 4L, 2L, "Squat", 4, 8)));

        mockMvc.perform(get("/api/completed-exercises/exercise/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].exerciseId").value(2));
    }

    @Test
    void getByUserId_shouldReturnUserHistory() throws Exception {
        when(completedExerciseService.findByUserId(3L)).thenReturn(List.of(
                new CompletedExerciseResponse(1L, 4L, 2L, "Squat", 4, 8)));

        mockMvc.perform(get("/api/completed-exercises/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completedWorkoutId").value(4));
    }

    @Test
    void getById_whenEntryExists_shouldReturnEntry() throws Exception {
        when(completedExerciseService.findById(1L))
                .thenReturn(new CompletedExerciseResponse(1L, 4L, 2L, "Squat", 4, 8));

        mockMvc.perform(get("/api/completed-exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setsDone").value(4));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(completedExerciseService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Completed exercise not found with id: 99"));

        mockMvc.perform(get("/api/completed-exercises/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        CompletedExerciseRequest request = new CompletedExerciseRequest(4L, 2L, 4, 8);
        when(completedExerciseService.create(any(CompletedExerciseRequest.class)))
                .thenReturn(new CompletedExerciseResponse(1L, 4L, 2L, "Squat", 4, 8));

        mockMvc.perform(post("/api/completed-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName").value("Squat"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        CompletedExerciseRequest request = new CompletedExerciseRequest(null, null, 4, 8);

        mockMvc.perform(post("/api/completed-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        CompletedExerciseRequest request = new CompletedExerciseRequest(4L, 2L, 4, 8);
        when(completedExerciseService.create(any(CompletedExerciseRequest.class)))
                .thenThrow(new DuplicateResourceException("Exercise already logged for this completed workout"));

        mockMvc.perform(post("/api/completed-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        CompletedExerciseRequest request = new CompletedExerciseRequest(4L, 2L, 5, 8);
        when(completedExerciseService.update(eq(1L), any(CompletedExerciseRequest.class)))
                .thenReturn(new CompletedExerciseResponse(1L, 4L, 2L, "Squat", 5, 8));

        mockMvc.perform(put("/api/completed-exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setsDone").value(5));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        CompletedExerciseRequest request = new CompletedExerciseRequest(4L, 2L, 5, 8);
        when(completedExerciseService.update(eq(99L), any(CompletedExerciseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Completed exercise not found with id: 99"));

        mockMvc.perform(put("/api/completed-exercises/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/completed-exercises/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Completed exercise not found with id: 99"))
                .when(completedExerciseService).delete(99L);

        mockMvc.perform(delete("/api/completed-exercises/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
