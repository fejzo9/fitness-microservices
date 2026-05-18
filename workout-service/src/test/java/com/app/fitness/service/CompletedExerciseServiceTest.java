package com.app.fitness.service;

import com.app.fitness.dto.CompletedExerciseRequest;
import com.app.fitness.dto.CompletedExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.CompletedExerciseMapper;
import com.app.fitness.repository.CompletedExerciseRepository;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.fitness.workoutservice.model.CompletedExercise;
import com.fitness.workoutservice.model.CompletedWorkout;
import com.fitness.workoutservice.model.Exercise;
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
class CompletedExerciseServiceTest {

    @Mock
    private CompletedExerciseRepository completedExerciseRepository;

    @Mock
    private CompletedWorkoutRepository completedWorkoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private CompletedExerciseMapper completedExerciseMapper;

    @InjectMocks
    private CompletedExerciseService completedExerciseService;

    @Test
    void findAll_shouldReturnMappedList() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedExerciseResponse response = CompletedExerciseResponse.builder().id(1L).setsDone(4).repsDone(10).build();
        when(completedExerciseRepository.findAll()).thenReturn(List.of(ce));
        when(completedExerciseMapper.toResponse(ce)).thenReturn(response);

        var result = completedExerciseService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSetsDone()).isEqualTo(4);
    }

    @Test
    void findByExerciseId_shouldReturnMappedList() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedExerciseResponse response = CompletedExerciseResponse.builder().id(1L).setsDone(4).repsDone(10).build();
        when(completedExerciseRepository.findByExerciseId(1L)).thenReturn(List.of(ce));
        when(completedExerciseMapper.toResponse(ce)).thenReturn(response);

        var result = completedExerciseService.findByExerciseId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSetsDone()).isEqualTo(4);
    }

    @Test
    void findByUserId_shouldReturnMappedList() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedExerciseResponse response = CompletedExerciseResponse.builder().id(1L).setsDone(4).repsDone(10).build();
        when(completedExerciseRepository.findByCompletedWorkoutUserId(1L)).thenReturn(List.of(ce));
        when(completedExerciseMapper.toResponse(ce)).thenReturn(response);

        var result = completedExerciseService.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSetsDone()).isEqualTo(4);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(completedExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedExerciseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_whenFound_shouldReturnResponse() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedExerciseResponse response = CompletedExerciseResponse.builder().id(1L).setsDone(4).repsDone(10).build();
        when(completedExerciseRepository.findById(1L)).thenReturn(Optional.of(ce));
        when(completedExerciseMapper.toResponse(ce)).thenReturn(response);

        var result = completedExerciseService.findById(1L);

        assertThat(result.getSetsDone()).isEqualTo(4);
    }

    @Test
    void create_whenCompletedWorkoutNotFound_shouldThrow() {
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(99L)
                .exerciseId(1L)
                .build();
        when(completedWorkoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedExerciseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenExerciseNotFound_shouldThrow() {
        CompletedWorkout cw = CompletedWorkout.builder().id(1L).userId(1L).build();
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(1L)
                .exerciseId(99L)
                .build();
        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(cw));
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedExerciseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        CompletedWorkout cw = CompletedWorkout.builder().id(1L).userId(1L).build();
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(1L)
                .exerciseId(1L)
                .setsDone(4)
                .repsDone(10)
                .build();
        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(cw));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(completedExerciseRepository.existsByCompletedWorkoutAndExercise(cw, exercise)).thenReturn(true);

        assertThatThrownBy(() -> completedExerciseService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        CompletedWorkout cw = CompletedWorkout.builder().id(1L).userId(1L).build();
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(1L)
                .exerciseId(1L)
                .setsDone(4)
                .repsDone(10)
                .build();
        CompletedExercise entity = CompletedExercise.builder().setsDone(4).repsDone(10).build();
        CompletedExercise saved = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedExerciseResponse response = CompletedExerciseResponse.builder().id(1L).setsDone(4).repsDone(10).build();

        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(cw));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(completedExerciseRepository.existsByCompletedWorkoutAndExercise(cw, exercise)).thenReturn(false);
        when(completedExerciseMapper.toEntity(request)).thenReturn(entity);
        when(completedExerciseRepository.save(entity)).thenReturn(saved);
        when(completedExerciseMapper.toResponse(saved)).thenReturn(response);

        var result = completedExerciseService.create(request);

        assertThat(result.getSetsDone()).isEqualTo(4);
        assertThat(entity.getCompletedWorkout()).isEqualTo(cw);
        assertThat(entity.getExercise()).isEqualTo(exercise);
        verify(completedExerciseRepository).save(entity);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(1L)
                .exerciseId(1L)
                .build();
        when(completedExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedExerciseService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenCompletedWorkoutNotFound_shouldThrow() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(99L)
                .exerciseId(1L)
                .build();
        when(completedExerciseRepository.findById(1L)).thenReturn(Optional.of(ce));
        when(completedWorkoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedExerciseService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenExerciseNotFound_shouldThrow() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedWorkout cw = CompletedWorkout.builder().id(1L).userId(1L).build();
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(1L)
                .exerciseId(99L)
                .build();
        when(completedExerciseRepository.findById(1L)).thenReturn(Optional.of(ce));
        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(cw));
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedExerciseService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withValidRequest_shouldSaveAndReturn() {
        CompletedExercise ce = CompletedExercise.builder().id(1L).setsDone(4).repsDone(10).build();
        CompletedWorkout cw = CompletedWorkout.builder().id(1L).userId(1L).build();
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        CompletedExerciseRequest request = CompletedExerciseRequest.builder()
                .completedWorkoutId(1L)
                .exerciseId(1L)
                .setsDone(5)
                .repsDone(12)
                .build();
        CompletedExerciseResponse response = CompletedExerciseResponse.builder().id(1L).setsDone(5).repsDone(12).build();

        when(completedExerciseRepository.findById(1L)).thenReturn(Optional.of(ce));
        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(cw));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(completedExerciseRepository.save(ce)).thenReturn(ce);
        when(completedExerciseMapper.toResponse(ce)).thenReturn(response);

        var result = completedExerciseService.update(1L, request);

        assertThat(result.getSetsDone()).isEqualTo(5);
        assertThat(ce.getCompletedWorkout()).isEqualTo(cw);
        assertThat(ce.getExercise()).isEqualTo(exercise);
        verify(completedExerciseMapper).updateEntity(request, ce);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(completedExerciseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> completedExerciseService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(completedExerciseRepository.existsById(1L)).thenReturn(true);

        completedExerciseService.delete(1L);

        verify(completedExerciseRepository).deleteById(1L);
    }
}
