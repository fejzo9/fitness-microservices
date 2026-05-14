package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseCategoryRequest;
import com.app.fitness.dto.ExerciseCategoryResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.ExerciseCategoryService;
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
class ExerciseCategoryControllerTest extends ControllerTestSupport {

    @Mock
    private ExerciseCategoryService exerciseCategoryService;

    @InjectMocks
    private ExerciseCategoryController exerciseCategoryController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(exerciseCategoryController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(exerciseCategoryService.findAll()).thenReturn(List.of(
                new ExerciseCategoryResponse(1L, "Strength", "Heavy compound work")));

        mockMvc.perform(get("/api/exercise-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Strength"));
    }

    @Test
    void getById_whenCategoryExists_shouldReturnCategory() throws Exception {
        when(exerciseCategoryService.findById(1L))
                .thenReturn(new ExerciseCategoryResponse(1L, "Strength", "Heavy compound work"));

        mockMvc.perform(get("/api/exercise-categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Heavy compound work"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(exerciseCategoryService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Exercise category not found with id: 99"));

        mockMvc.perform(get("/api/exercise-categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        ExerciseCategoryRequest request = new ExerciseCategoryRequest("Mobility", "Mobility work");
        when(exerciseCategoryService.create(any(ExerciseCategoryRequest.class)))
                .thenReturn(new ExerciseCategoryResponse(2L, "Mobility", "Mobility work"));

        mockMvc.perform(post("/api/exercise-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mobility"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        ExerciseCategoryRequest request = new ExerciseCategoryRequest("", "Mobility work");

        mockMvc.perform(post("/api/exercise-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        ExerciseCategoryRequest request = new ExerciseCategoryRequest("Strength", "Duplicate");
        when(exerciseCategoryService.create(any(ExerciseCategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Exercise category already exists with name: Strength"));

        mockMvc.perform(post("/api/exercise-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        ExerciseCategoryRequest request = new ExerciseCategoryRequest("Strength+", "Updated");
        when(exerciseCategoryService.update(eq(1L), any(ExerciseCategoryRequest.class)))
                .thenReturn(new ExerciseCategoryResponse(1L, "Strength+", "Updated"));

        mockMvc.perform(put("/api/exercise-categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Strength+"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        ExerciseCategoryRequest request = new ExerciseCategoryRequest("Strength+", "Updated");
        when(exerciseCategoryService.update(eq(99L), any(ExerciseCategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exercise category not found with id: 99"));

        mockMvc.perform(put("/api/exercise-categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/exercise-categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Exercise category not found with id: 99"))
                .when(exerciseCategoryService).delete(99L);

        mockMvc.perform(delete("/api/exercise-categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
