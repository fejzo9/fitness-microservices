package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutDayRequest;
import com.app.fitness.dto.WorkoutDayResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.WorkoutDayService;
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
class WorkoutDayControllerTest extends ControllerTestSupport {

    @Mock
    private WorkoutDayService workoutDayService;

    @InjectMocks
    private WorkoutDayController workoutDayController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(workoutDayController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(workoutDayService.findAll()).thenReturn(List.of(
                new WorkoutDayResponse(1L, 7L, "Day 1", 1)));

        mockMvc.perform(get("/api/workout-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dayName").value("Day 1"));
    }

    @Test
    void getByWorkoutPlanId_shouldReturnPlanDays() throws Exception {
        when(workoutDayService.findByWorkoutPlanId(7L)).thenReturn(List.of(
                new WorkoutDayResponse(1L, 7L, "Day 1", 1)));

        mockMvc.perform(get("/api/workout-days/plan/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workoutPlanId").value(7));
    }

    @Test
    void getById_whenDayExists_shouldReturnDay() throws Exception {
        when(workoutDayService.findById(1L)).thenReturn(new WorkoutDayResponse(1L, 7L, "Day 1", 1));

        mockMvc.perform(get("/api/workout-days/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderIndex").value(1));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(workoutDayService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Workout day not found with id: 99"));

        mockMvc.perform(get("/api/workout-days/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        WorkoutDayRequest request = new WorkoutDayRequest(7L, "Day 2", 2);
        when(workoutDayService.create(any(WorkoutDayRequest.class)))
                .thenReturn(new WorkoutDayResponse(2L, 7L, "Day 2", 2));

        mockMvc.perform(post("/api/workout-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayName").value("Day 2"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        WorkoutDayRequest request = new WorkoutDayRequest(null, "", null);

        mockMvc.perform(post("/api/workout-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        WorkoutDayRequest request = new WorkoutDayRequest(7L, "Day 2", 2);
        when(workoutDayService.update(eq(1L), any(WorkoutDayRequest.class)))
                .thenReturn(new WorkoutDayResponse(1L, 7L, "Day 2", 2));

        mockMvc.perform(put("/api/workout-days/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayName").value("Day 2"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        WorkoutDayRequest request = new WorkoutDayRequest(7L, "Day 2", 2);
        when(workoutDayService.update(eq(99L), any(WorkoutDayRequest.class)))
                .thenThrow(new ResourceNotFoundException("Workout day not found with id: 99"));

        mockMvc.perform(put("/api/workout-days/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/workout-days/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Workout day not found with id: 99"))
                .when(workoutDayService).delete(99L);

        mockMvc.perform(delete("/api/workout-days/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
