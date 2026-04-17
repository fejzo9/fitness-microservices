package com.app.fitness.service;

import com.app.fitness.dto.MealLogRequest;
import com.app.fitness.dto.MealLogResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.MealLogMapper;
import com.app.fitness.repository.MealLogRepository;
import com.fitness.nutritionservice.model.MealLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
class MealLogServiceTest {

    @Mock
    private MealLogRepository mealLogRepository;

    @Mock
    private MealLogMapper mealLogMapper;

    @InjectMocks
    private MealLogService mealLogService;

    @Test
    void findAll_shouldReturnMappedList() {
        MealLog log = MealLog.builder().id(1L).userId(3L).mealType("BREAKFAST").build();
        MealLogResponse response = new MealLogResponse(1L, 3L, LocalDate.of(2026, 4, 10), "BREAKFAST");
        when(mealLogRepository.findAll()).thenReturn(List.of(log));
        when(mealLogMapper.toResponse(log)).thenReturn(response);

        List<MealLogResponse> result = mealLogService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMealType()).isEqualTo("BREAKFAST");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(mealLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealLogService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 10), "BREAKFAST");
        when(mealLogRepository.existsByUserIdAndLogDateAndMealType(3L, LocalDate.of(2026, 4, 10), "BREAKFAST"))
                .thenReturn(true);

        assertThatThrownBy(() -> mealLogService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        MealLogRequest request = new MealLogRequest(3L, LocalDate.of(2026, 4, 11), "LUNCH");
        MealLog entity = MealLog.builder().userId(3L).mealType("LUNCH").build();
        MealLog saved = MealLog.builder().id(2L).userId(3L).mealType("LUNCH").build();
        MealLogResponse response = new MealLogResponse(2L, 3L, LocalDate.of(2026, 4, 11), "LUNCH");

        when(mealLogRepository.existsByUserIdAndLogDateAndMealType(any(), any(), any())).thenReturn(false);
        when(mealLogMapper.toEntity(request)).thenReturn(entity);
        when(mealLogRepository.save(entity)).thenReturn(saved);
        when(mealLogMapper.toResponse(saved)).thenReturn(response);

        MealLogResponse result = mealLogService.create(request);

        assertThat(result.getMealType()).isEqualTo("LUNCH");
        verify(mealLogRepository).save(entity);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(mealLogRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> mealLogService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldDeleteById() {
        when(mealLogRepository.existsById(1L)).thenReturn(true);

        mealLogService.delete(1L);

        verify(mealLogRepository).deleteById(1L);
    }
}
