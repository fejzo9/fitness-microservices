package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutPlanRequest;
import com.app.fitness.dto.WorkoutPlanResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.WorkoutPlanService;
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
class WorkoutPlanControllerTest extends ControllerTestSupport {

    @Mock
    private WorkoutPlanService workoutPlanService;

    @InjectMocks
    private WorkoutPlanController workoutPlanController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(workoutPlanController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(workoutPlanService.findAll()).thenReturn(List.of(
                new WorkoutPlanResponse(1L, 3L, "Push Pull Legs", "Strength focus", true)));

        mockMvc.perform(get("/api/workout-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Push Pull Legs"));
    }

    @Test
    void getByUserId_shouldReturnUserPlans() throws Exception {
        when(workoutPlanService.findByUserId(3L)).thenReturn(List.of(
                new WorkoutPlanResponse(1L, 3L, "Push Pull Legs", "Strength focus", true)));

        mockMvc.perform(get("/api/workout-plans/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(3));
    }

    @Test
    void getById_whenPlanExists_shouldReturnPlan() throws Exception {
        when(workoutPlanService.findById(1L))
                .thenReturn(new WorkoutPlanResponse(1L, 3L, "Push Pull Legs", "Strength focus", true));

        mockMvc.perform(get("/api/workout-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(workoutPlanService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Workout plan not found with id: 99"));

        mockMvc.perform(get("/api/workout-plans/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest(3L, "Upper Lower", "Balanced", true);
        when(workoutPlanService.create(any(WorkoutPlanRequest.class)))
                .thenReturn(new WorkoutPlanResponse(2L, 3L, "Upper Lower", "Balanced", true));

        mockMvc.perform(post("/api/workout-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Upper Lower"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest(null, "", "Balanced", null);

        mockMvc.perform(post("/api/workout-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest(3L, "Push Pull Legs", "Duplicate", true);
        when(workoutPlanService.create(any(WorkoutPlanRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Workout plan already exists with name 'Push Pull Legs' for userId=3"));

        mockMvc.perform(post("/api/workout-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest(3L, "Upper Lower Plus", "Updated", false);
        when(workoutPlanService.update(eq(1L), any(WorkoutPlanRequest.class)))
                .thenReturn(new WorkoutPlanResponse(1L, 3L, "Upper Lower Plus", "Updated", false));

        mockMvc.perform(put("/api/workout-plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Upper Lower Plus"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest(3L, "Upper Lower Plus", "Updated", false);
        when(workoutPlanService.update(eq(99L), any(WorkoutPlanRequest.class)))
                .thenThrow(new ResourceNotFoundException("Workout plan not found with id: 99"));

        mockMvc.perform(put("/api/workout-plans/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/workout-plans/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Workout plan not found with id: 99"))
                .when(workoutPlanService).delete(99L);

        mockMvc.perform(delete("/api/workout-plans/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
