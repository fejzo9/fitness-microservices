package com.app.fitness.service;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutExerciseMapper;
import com.app.fitness.repository.ExerciseRepository;
import com.app.fitness.repository.WorkoutDayRepository;
import com.app.fitness.repository.WorkoutExerciseRepository;
import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutExercise;
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
class WorkoutExerciseServiceTest {

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutExerciseMapper workoutExerciseMapper;

    @InjectMocks
    private WorkoutExerciseService workoutExerciseService;

    @Test
    void findAll_shouldReturnMappedList() {
        WorkoutExercise we = WorkoutExercise.builder().id(1L).sets(4).reps(10).build();
        WorkoutExerciseResponse response = WorkoutExerciseResponse.builder().id(1L).sets(4).reps(10).build();
        when(workoutExerciseRepository.findAll()).thenReturn(List.of(we));
        when(workoutExerciseMapper.toResponse(we)).thenReturn(response);

        var result = workoutExerciseService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSets()).isEqualTo(4);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(workoutExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutExerciseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_whenFound_shouldReturnResponse() {
        WorkoutExercise we = WorkoutExercise.builder().id(1L).sets(4).reps(10).build();
        WorkoutExerciseResponse response = WorkoutExerciseResponse.builder().id(1L).sets(4).reps(10).build();
        when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(we));
        when(workoutExerciseMapper.toResponse(we)).thenReturn(response);

        var result = workoutExerciseService.findById(1L);

        assertThat(result.getSets()).isEqualTo(4);
    }

    @Test
    void create_whenWorkoutDayNotFound_shouldThrow() {
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(99L)
                .exerciseId(1L)
                .build();
        when(workoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutExerciseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenExerciseNotFound_shouldThrow() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(1L)
                .exerciseId(99L)
                .build();
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutExerciseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(1L)
                .exerciseId(1L)
                .sets(4)
                .reps(10)
                .build();
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(workoutExerciseRepository.existsByWorkoutDayAndExercise(day, exercise)).thenReturn(true);

        assertThatThrownBy(() -> workoutExerciseService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(1L)
                .exerciseId(1L)
                .sets(4)
                .reps(10)
                .restSec(90)
                .build();
        WorkoutExercise entity = WorkoutExercise.builder().sets(4).reps(10).build();
        WorkoutExercise saved = WorkoutExercise.builder().id(1L).sets(4).reps(10).build();
        WorkoutExerciseResponse response = WorkoutExerciseResponse.builder().id(1L).sets(4).reps(10).build();

        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(workoutExerciseRepository.existsByWorkoutDayAndExercise(day, exercise)).thenReturn(false);
        when(workoutExerciseMapper.toEntity(request)).thenReturn(entity);
        when(workoutExerciseRepository.save(entity)).thenReturn(saved);
        when(workoutExerciseMapper.toResponse(saved)).thenReturn(response);

        var result = workoutExerciseService.create(request);

        assertThat(result.getSets()).isEqualTo(4);
        assertThat(entity.getWorkoutDay()).isEqualTo(day);
        assertThat(entity.getExercise()).isEqualTo(exercise);
        verify(workoutExerciseRepository).save(entity);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(1L)
                .exerciseId(1L)
                .build();
        when(workoutExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutExerciseService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenWorkoutDayNotFound_shouldThrow() {
        WorkoutExercise we = WorkoutExercise.builder().id(1L).sets(4).reps(10).build();
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(99L)
                .exerciseId(1L)
                .build();
        when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(we));
        when(workoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutExerciseService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenExerciseNotFound_shouldThrow() {
        WorkoutExercise we = WorkoutExercise.builder().id(1L).sets(4).reps(10).build();
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(1L)
                .exerciseId(99L)
                .build();
        when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(we));
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutExerciseService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withValidRequest_shouldSaveAndReturn() {
        WorkoutExercise we = WorkoutExercise.builder().id(1L).sets(4).reps(10).build();
        WorkoutDay day = WorkoutDay.builder().id(1L).dayName("Monday").build();
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        WorkoutExerciseRequest request = WorkoutExerciseRequest.builder()
                .workoutDayId(1L)
                .exerciseId(1L)
                .sets(5)
                .reps(12)
                .restSec(120)
                .build();
        WorkoutExerciseResponse response = WorkoutExerciseResponse.builder().id(1L).sets(5).reps(12).build();

        when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(we));
        when(workoutDayRepository.findById(1L)).thenReturn(Optional.of(day));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(workoutExerciseRepository.save(we)).thenReturn(we);
        when(workoutExerciseMapper.toResponse(we)).thenReturn(response);

        var result = workoutExerciseService.update(1L, request);

        assertThat(result.getSets()).isEqualTo(5);
        assertThat(we.getWorkoutDay()).isEqualTo(day);
        assertThat(we.getExercise()).isEqualTo(exercise);
        verify(workoutExerciseMapper).updateEntity(request, we);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(workoutExerciseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> workoutExerciseService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(workoutExerciseRepository.existsById(1L)).thenReturn(true);

        workoutExerciseService.delete(1L);

        verify(workoutExerciseRepository).deleteById(1L);
    }
}
