package com.app.fitness.service;

import com.app.fitness.dto.WorkoutPlanRequest;
import com.app.fitness.dto.WorkoutPlanResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutPlanMapper;
import com.app.fitness.repository.WorkoutPlanRepository;
import com.fitness.workoutservice.model.WorkoutPlan;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanServiceTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private WorkoutPlanMapper workoutPlanMapper;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    @Test
    void findAll_shouldReturnMappedList() {
        WorkoutPlan plan = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutPlanResponse response = WorkoutPlanResponse.builder().id(1L).name("Plan A").build();
        when(workoutPlanRepository.findAll()).thenReturn(List.of(plan));
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        var result = workoutPlanService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Plan A");
    }

    @Test
    void findByUserId_shouldReturnMappedList() {
        WorkoutPlan plan = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutPlanResponse response = WorkoutPlanResponse.builder().id(1L).name("Plan A").build();
        when(workoutPlanRepository.findByUserId(1L)).thenReturn(List.of(plan));
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        var result = workoutPlanService.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Plan A");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(workoutPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_whenFound_shouldReturnResponse() {
        WorkoutPlan plan = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutPlanResponse response = WorkoutPlanResponse.builder().id(1L).name("Plan A").build();
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        var result = workoutPlanService.findById(1L);

        assertThat(result.getName()).isEqualTo("Plan A");
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        WorkoutPlanRequest request = WorkoutPlanRequest.builder()
                .userId(1L)
                .name("Plan A")
                .build();
        when(workoutPlanRepository.existsByUserIdAndName(1L, "Plan A")).thenReturn(true);

        assertThatThrownBy(() -> workoutPlanService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        WorkoutPlanRequest request = WorkoutPlanRequest.builder()
                .userId(1L)
                .name("Plan A")
                .description("My plan")
                .build();
        WorkoutPlan entity = WorkoutPlan.builder().userId(1L).name("Plan A").build();
        WorkoutPlan saved = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutPlanResponse response = WorkoutPlanResponse.builder().id(1L).name("Plan A").build();

        when(workoutPlanRepository.existsByUserIdAndName(1L, "Plan A")).thenReturn(false);
        when(workoutPlanMapper.toEntity(request)).thenReturn(entity);
        when(workoutPlanRepository.save(entity)).thenReturn(saved);
        when(workoutPlanMapper.toResponse(saved)).thenReturn(response);

        var result = workoutPlanService.create(request);

        assertThat(result.getName()).isEqualTo("Plan A");
        verify(workoutPlanRepository).save(entity);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        WorkoutPlanRequest request = WorkoutPlanRequest.builder().userId(1L).name("Plan A").build();
        when(workoutPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withValidRequest_shouldSaveAndReturn() {
        WorkoutPlan plan = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutPlanRequest request = WorkoutPlanRequest.builder()
                .userId(1L)
                .name("Plan B")
                .description("Updated")
                .build();
        WorkoutPlanResponse response = WorkoutPlanResponse.builder().id(1L).name("Plan B").build();

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(workoutPlanRepository.save(plan)).thenReturn(plan);
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        var result = workoutPlanService.update(1L, request);

        assertThat(result.getName()).isEqualTo("Plan B");
        verify(workoutPlanMapper).updateEntity(request, plan);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(workoutPlanRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> workoutPlanService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(workoutPlanRepository.existsById(1L)).thenReturn(true);

        workoutPlanService.delete(1L);

        verify(workoutPlanRepository).deleteById(1L);
    }
}
