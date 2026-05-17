package com.app.fitness.service;

import com.app.fitness.dto.ExerciseCategoryRequest;
import com.app.fitness.dto.ExerciseCategoryResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ExerciseCategoryMapper;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.fitness.workoutservice.model.ExerciseCategory;
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
class ExerciseCategoryServiceTest {

    @Mock
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Mock
    private ExerciseCategoryMapper exerciseCategoryMapper;

    @InjectMocks
    private ExerciseCategoryService exerciseCategoryService;

    @Test
    void findAll_shouldReturnMappedList() {
        ExerciseCategory category = ExerciseCategory.builder().id(1L).name("Legs").build();
        ExerciseCategoryResponse response = ExerciseCategoryResponse.builder().id(1L).name("Legs").build();
        when(exerciseCategoryRepository.findAll()).thenReturn(List.of(category));
        when(exerciseCategoryMapper.toResponse(category)).thenReturn(response);

        var result = exerciseCategoryService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Legs");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(exerciseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseCategoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_whenFound_shouldReturnResponse() {
        ExerciseCategory category = ExerciseCategory.builder().id(1L).name("Legs").build();
        ExerciseCategoryResponse response = ExerciseCategoryResponse.builder().id(1L).name("Legs").build();
        when(exerciseCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(exerciseCategoryMapper.toResponse(category)).thenReturn(response);

        var result = exerciseCategoryService.findById(1L);

        assertThat(result.getName()).isEqualTo("Legs");
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        ExerciseCategoryRequest request = ExerciseCategoryRequest.builder().name("Legs").build();
        when(exerciseCategoryRepository.existsByName("Legs")).thenReturn(true);

        assertThatThrownBy(() -> exerciseCategoryService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        ExerciseCategoryRequest request = ExerciseCategoryRequest.builder()
                .name("Legs")
                .description("Leg exercises")
                .build();
        ExerciseCategory entity = ExerciseCategory.builder().name("Legs").build();
        ExerciseCategory saved = ExerciseCategory.builder().id(1L).name("Legs").build();
        ExerciseCategoryResponse response = ExerciseCategoryResponse.builder().id(1L).name("Legs").build();

        when(exerciseCategoryRepository.existsByName("Legs")).thenReturn(false);
        when(exerciseCategoryMapper.toEntity(request)).thenReturn(entity);
        when(exerciseCategoryRepository.save(entity)).thenReturn(saved);
        when(exerciseCategoryMapper.toResponse(saved)).thenReturn(response);

        var result = exerciseCategoryService.create(request);

        assertThat(result.getName()).isEqualTo("Legs");
        verify(exerciseCategoryRepository).save(entity);
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        ExerciseCategoryRequest request = ExerciseCategoryRequest.builder().name("Legs").build();
        when(exerciseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseCategoryService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withDuplicateName_shouldThrow() {
        ExerciseCategory category = ExerciseCategory.builder().id(1L).name("Legs").build();
        ExerciseCategoryRequest request = ExerciseCategoryRequest.builder().name("Arms").build();
        when(exerciseCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(exerciseCategoryRepository.existsByName("Arms")).thenReturn(true);

        assertThatThrownBy(() -> exerciseCategoryService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(exerciseCategoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> exerciseCategoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(exerciseCategoryRepository.existsById(1L)).thenReturn(true);

        exerciseCategoryService.delete(1L);

        verify(exerciseCategoryRepository).deleteById(1L);
    }
}
