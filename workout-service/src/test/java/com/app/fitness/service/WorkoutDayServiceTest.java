package com.app.fitness.service;

import com.app.fitness.dto.WorkoutDayRequest;
import com.app.fitness.dto.WorkoutDayResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutDayMapper;
import com.app.fitness.repository.WorkoutDayRepository;
import com.app.fitness.repository.WorkoutPlanRepository;
import com.fitness.workoutservice.model.WorkoutDay;
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
class WorkoutDayServiceTest {

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private WorkoutDayMapper workoutDayMapper;

    @InjectMocks
    private WorkoutDayService workoutDayService;

    @Test
    void findAll_shouldReturnMappedList() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutDayResponse response = WorkoutDayResponse.builder().id(1L).dayName("Monday").build();
        when(workoutDayRepository.findAll()).thenReturn(List.of(day));
        when(workoutDayMapper.toResponse(day)).thenReturn(response);

        var result = workoutDayService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDayName()).isEqualTo("Monday");
    }

    @Test
    void findByWorkoutPlanId_shouldReturnMappedList() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutDayResponse response = WorkoutDayResponse.builder().id(1L).dayName("Monday").build();
        when(workoutDayRepository.findByWorkoutPlanId(1L)).thenReturn(List.of(day));
        when(workoutDayMapper.toResponse(day)).thenReturn(response);

        var result = workoutDayService.findByWorkoutPlanId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDayName()).isEqualTo("Monday");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(workoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutDayService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_whenFound_shouldReturnResponse() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutDayResponse response = WorkoutDayResponse.builder().id(1L).dayName("Monday").build();
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(workoutDayMapper.toResponse(day)).thenReturn(response);

        var result = workoutDayService.findById(1L);

        assertThat(result.getDayName()).isEqualTo("Monday");
    }

    @Test
    void create_whenWorkoutPlanNotFound_shouldThrow() {
        WorkoutDayRequest request = WorkoutDayRequest.builder()
                .workoutPlanId(99L)
                .dayName("Monday")
                .build();
        when(workoutPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutDayService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        WorkoutPlan plan = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutDayRequest request = WorkoutDayRequest.builder()
                .workoutPlanId(1L)
                .dayName("Monday")
                .orderIndex(1)
                .build();
        WorkoutDay entity = WorkoutDay.builder().dayName("Monday").build();
        WorkoutDay saved = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutDayResponse response = WorkoutDayResponse.builder().id(1L).dayName("Monday").build();

        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(workoutDayMapper.toEntity(request)).thenReturn(entity);
        when(workoutDayRepository.save(entity)).thenReturn(saved);
        when(workoutDayMapper.toResponse(saved)).thenReturn(response);

        var result = workoutDayService.create(request);

        assertThat(result.getDayName()).isEqualTo("Monday");
        assertThat(entity.getWorkoutPlan()).isEqualTo(plan);
        verify(workoutDayRepository).save(entity);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        WorkoutDayRequest request = WorkoutDayRequest.builder()
                .workoutPlanId(1L)
                .dayName("Monday")
                .build();
        when(workoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutDayService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenWorkoutPlanNotFound_shouldThrow() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutDayRequest request = WorkoutDayRequest.builder()
                .workoutPlanId(99L)
                .dayName("Tuesday")
                .build();
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(workoutPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutDayService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withValidRequest_shouldSaveAndReturn() {
        WorkoutPlan plan = WorkoutPlan.builder().id(1L).userId(1L).name("Plan A").build();
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutDayRequest request = WorkoutDayRequest.builder()
                .workoutPlanId(1L)
                .dayName("Tuesday")
                .orderIndex(2)
                .build();
        WorkoutDayResponse response = WorkoutDayResponse.builder().id(1L).dayName("Tuesday").build();

        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(workoutDayRepository.save(day)).thenReturn(day);
        when(workoutDayMapper.toResponse(day)).thenReturn(response);

        var result = workoutDayService.update(1L, request);

        assertThat(result.getDayName()).isEqualTo("Tuesday");
        assertThat(day.getWorkoutPlan()).isEqualTo(plan);
        verify(workoutDayMapper).updateEntity(request, day);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(workoutDayRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> workoutDayService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(workoutDayRepository.existsById(1L)).thenReturn(true);

        workoutDayService.delete(1L);

        verify(workoutDayRepository).deleteById(1L);
    }
}
