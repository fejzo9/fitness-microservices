package com.app.fitness.service;

import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ExerciseMapper;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.ExerciseCategory;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void findAll_shouldReturnMappedList() {
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        ExerciseResponse response = ExerciseResponse.builder().id(1L).name("Bench Press").build();
        Page<Exercise> page = new org.springframework.data.domain.PageImpl<>(List.of(exercise));
        when(exerciseRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        when(exerciseMapper.toResponse(exercise)).thenReturn(response);

        var result = exerciseService.findAll(org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getName()).isEqualTo("Bench Press");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_whenFound_shouldReturnResponse() {
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        ExerciseResponse response = ExerciseResponse.builder().id(1L).name("Bench Press").build();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseMapper.toResponse(exercise)).thenReturn(response);

        var result = exerciseService.findById(1L);

        assertThat(result.getName()).isEqualTo("Bench Press");
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        when(exerciseRepository.existsByName("Bench Press")).thenReturn(true);

        assertThatThrownBy(() -> exerciseService.create(ExerciseRequest.builder().name("Bench Press").build()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Squat")
                .description("Leg exercise")
                .difficulty("BEGINNER")
                .build();
        Exercise entity = Exercise.builder().name("Squat").build();
        Exercise saved = Exercise.builder().id(2L).name("Squat").build();
        ExerciseResponse response = ExerciseResponse.builder().id(2L).name("Squat").build();

        when(exerciseRepository.existsByName("Squat")).thenReturn(false);
        when(exerciseMapper.toEntity(request)).thenReturn(entity);
        when(exerciseRepository.save(entity)).thenReturn(saved);
        when(exerciseMapper.toResponse(saved)).thenReturn(response);

        ExerciseResponse result = exerciseService.create(request);

        assertThat(result.getName()).isEqualTo("Squat");
        verify(exerciseRepository).save(entity);
    }

    @Test
    void create_withCategories_shouldResolveAndMap() {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Squat")
                .categories(List.of(com.app.fitness.dto.ExerciseCategoryType.Legs))
                .build();
        Exercise entity = Exercise.builder().name("Squat").build();
        Exercise saved = Exercise.builder().id(2L).name("Squat").categories(Set.of()).build();
        ExerciseCategory category = ExerciseCategory.builder().id(1L).name("Legs").build();
        ExerciseResponse response = ExerciseResponse.builder().id(2L).name("Squat").build();

        when(exerciseRepository.existsByName("Squat")).thenReturn(false);
        when(exerciseMapper.toEntity(request)).thenReturn(entity);
        when(exerciseCategoryRepository.findByNameIn(any())).thenReturn(List.of(category));
        when(exerciseRepository.save(entity)).thenReturn(saved);
        when(exerciseMapper.toResponse(saved)).thenReturn(response);

        exerciseService.create(request);

        assertThat(entity.getCategories()).containsExactly(category);
        verify(exerciseCategoryRepository).findByNameIn(List.of("Legs"));
    }

    @Test
    void create_withMissingCategory_shouldThrow() {
        ExerciseRequest request = ExerciseRequest.builder()
                .name("Squat")
                .categories(List.of(com.app.fitness.dto.ExerciseCategoryType.Legs))
                .build();
        Exercise entity = Exercise.builder().name("Squat").build();

        when(exerciseRepository.existsByName("Squat")).thenReturn(false);
        when(exerciseMapper.toEntity(request)).thenReturn(entity);
        when(exerciseCategoryRepository.findByNameIn(any())).thenReturn(List.of());

        assertThatThrownBy(() -> exerciseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise category not found");
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.update(99L, ExerciseRequest.builder().name("Squat").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withDuplicateName_shouldThrow() {
        Exercise exercise = Exercise.builder().id(1L).name("Bench Press").build();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.existsByName("Squat")).thenReturn(true);

        assertThatThrownBy(() -> exerciseService.update(1L, ExerciseRequest.builder().name("Squat").build()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(exerciseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> exerciseService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(exerciseRepository.existsById(1L)).thenReturn(true);

        exerciseService.delete(1L);

        verify(exerciseRepository).deleteById(1L);
    }
}
