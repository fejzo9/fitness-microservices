package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseCategoryMapRequest;
import com.app.fitness.dto.ExerciseCategoryMapResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.ExerciseCategoryMapService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExerciseCategoryMapControllerTest extends ControllerTestSupport {

    @Mock
    private ExerciseCategoryMapService exerciseCategoryMapService;

    @InjectMocks
    private ExerciseCategoryMapController exerciseCategoryMapController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(exerciseCategoryMapController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(exerciseCategoryMapService.findAll()).thenReturn(List.of(
                new ExerciseCategoryMapResponse(1L, 2L, "Squat", 3L, "Strength")));

        mockMvc.perform(get("/api/exercise-category-maps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Strength"));
    }

    @Test
    void getByCategoryId_shouldReturnMappings() throws Exception {
        when(exerciseCategoryMapService.findByCategoryId(3L)).thenReturn(List.of(
                new ExerciseCategoryMapResponse(1L, 2L, "Squat", 3L, "Strength")));

        mockMvc.perform(get("/api/exercise-category-maps/category/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(3));
    }

    @Test
    void getByExerciseId_shouldReturnMappings() throws Exception {
        when(exerciseCategoryMapService.findByExerciseId(2L)).thenReturn(List.of(
                new ExerciseCategoryMapResponse(1L, 2L, "Squat", 3L, "Strength")));

        mockMvc.perform(get("/api/exercise-category-maps/exercise/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].exerciseId").value(2));
    }

    @Test
    void getById_whenMappingExists_shouldReturnMapping() throws Exception {
        when(exerciseCategoryMapService.findById(1L))
                .thenReturn(new ExerciseCategoryMapResponse(1L, 2L, "Squat", 3L, "Strength"));

        mockMvc.perform(get("/api/exercise-category-maps/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseName").value("Squat"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(exerciseCategoryMapService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Exercise-category mapping not found with id: 99"));

        mockMvc.perform(get("/api/exercise-category-maps/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        ExerciseCategoryMapRequest request = new ExerciseCategoryMapRequest(2L, 3L);
        when(exerciseCategoryMapService.create(any(ExerciseCategoryMapRequest.class)))
                .thenReturn(new ExerciseCategoryMapResponse(1L, 2L, "Squat", 3L, "Strength"));

        mockMvc.perform(post("/api/exercise-category-maps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Strength"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        ExerciseCategoryMapRequest request = new ExerciseCategoryMapRequest(null, null);

        mockMvc.perform(post("/api/exercise-category-maps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        ExerciseCategoryMapRequest request = new ExerciseCategoryMapRequest(2L, 3L);
        when(exerciseCategoryMapService.create(any(ExerciseCategoryMapRequest.class)))
                .thenThrow(new DuplicateResourceException("Mapping already exists for exerciseId=2 and categoryId=3"));

        mockMvc.perform(post("/api/exercise-category-maps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/exercise-category-maps/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Exercise-category mapping not found with id: 99"))
                .when(exerciseCategoryMapService).delete(99L);

        mockMvc.perform(delete("/api/exercise-category-maps/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
