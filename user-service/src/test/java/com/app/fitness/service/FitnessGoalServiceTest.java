package com.app.fitness.service;

import com.app.fitness.dto.FitnessGoalRequest;
import com.app.fitness.dto.FitnessGoalResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.FitnessGoalMapper;
import com.app.fitness.repository.FitnessGoalRepository;
import com.fitness.userservice.model.FitnessGoal;
import java.math.BigDecimal;
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
class FitnessGoalServiceTest {

    @Mock
    private FitnessGoalRepository fitnessGoalRepository;

    @Mock
    private FitnessGoalMapper fitnessGoalMapper;

    @InjectMocks
    private FitnessGoalService fitnessGoalService;

    @Test
    void findAll_shouldReturnMappedList() {
        FitnessGoal goal = FitnessGoal.builder().id(1L).userId(3L).goalType("Lose Weight").build();
        FitnessGoalResponse response = new FitnessGoalResponse(1L, 3L, "Lose Weight", null, true, null);
        when(fitnessGoalRepository.findAll()).thenReturn(List.of(goal));
        when(fitnessGoalMapper.toResponse(goal)).thenReturn(response);

        List<FitnessGoalResponse> result = fitnessGoalService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGoalType()).isEqualTo("Lose Weight");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(fitnessGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fitnessGoalService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenDuplicate_shouldThrowDuplicateResourceException() {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Lose Weight", null, true, LocalDate.of(2026, 8, 1));
        when(fitnessGoalRepository.existsByUserIdAndGoalTypeAndDeadline(3L, "Lose Weight", LocalDate.of(2026, 8, 1)))
                .thenReturn(true);

        assertThatThrownBy(() -> fitnessGoalService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));
        FitnessGoal entity = FitnessGoal.builder().userId(3L).goalType("Lose Weight").build();
        FitnessGoal saved = FitnessGoal.builder().id(1L).userId(3L).goalType("Lose Weight").build();
        FitnessGoalResponse response = new FitnessGoalResponse(1L, 3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));

        when(fitnessGoalRepository.existsByUserIdAndGoalTypeAndDeadline(any(), any(), any())).thenReturn(false);
        when(fitnessGoalMapper.toEntity(request)).thenReturn(entity);
        when(fitnessGoalRepository.save(entity)).thenReturn(saved);
        when(fitnessGoalMapper.toResponse(saved)).thenReturn(response);

        FitnessGoalResponse result = fitnessGoalService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(fitnessGoalRepository).save(entity);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(fitnessGoalRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> fitnessGoalService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(fitnessGoalRepository.existsById(1L)).thenReturn(true);

        fitnessGoalService.delete(1L);

        verify(fitnessGoalRepository).deleteById(1L);
    }

    @Test
    void findByUserId_shouldReturnMappedList() {
        FitnessGoal goal1 = FitnessGoal.builder().id(1L).userId(3L).goalType("Lose Weight").isActive(true).build();
        FitnessGoal goal2 = FitnessGoal.builder().id(2L).userId(3L).goalType("Build Muscle").isActive(false).build();
        FitnessGoalResponse response1 = new FitnessGoalResponse(1L, 3L, "Lose Weight", null, true, null);
        FitnessGoalResponse response2 = new FitnessGoalResponse(2L, 3L, "Build Muscle", null, false, null);
        
        when(fitnessGoalRepository.findByUserId(3L)).thenReturn(List.of(goal1, goal2));
        when(fitnessGoalMapper.toResponse(goal1)).thenReturn(response1);
        when(fitnessGoalMapper.toResponse(goal2)).thenReturn(response2);

        List<FitnessGoalResponse> result = fitnessGoalService.findByUserId(3L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getGoalType()).isEqualTo("Lose Weight");
        assertThat(result.get(1).getGoalType()).isEqualTo("Build Muscle");
    }

    @Test
    void findActiveByUserId_whenNoActiveGoals_shouldReturnNull() {
        when(fitnessGoalRepository.findActiveGoalsByUserId(3L)).thenReturn(List.of());

        FitnessGoalResponse result = fitnessGoalService.findActiveByUserId(3L);

        assertThat(result).isNull();
    }

    @Test
    void findActiveByUserId_whenActiveGoalsExist_shouldReturnMostRecent() {
        FitnessGoal goal1 = FitnessGoal.builder().id(1L).userId(3L).goalType("Lose Weight").isActive(true).build();
        FitnessGoal goal2 = FitnessGoal.builder().id(2L).userId(3L).goalType("Build Muscle").isActive(true).build();
        FitnessGoalResponse response = new FitnessGoalResponse(2L, 3L, "Build Muscle", null, true, null);
        
        when(fitnessGoalRepository.findActiveGoalsByUserId(3L)).thenReturn(List.of(goal2, goal1));

        FitnessGoalResponse result = fitnessGoalService.findActiveByUserId(3L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getGoalType()).isEqualTo("Build Muscle");
    }

    @Test
    void create_whenActiveGoal_shouldDeactivateExistingActiveGoals() {
        FitnessGoalRequest request = new FitnessGoalRequest(3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));
        FitnessGoal existingActiveGoal = FitnessGoal.builder().id(1L).userId(3L).goalType("Build Muscle").isActive(true).build();
        FitnessGoal entity = FitnessGoal.builder().userId(3L).goalType("Lose Weight").isActive(true).build();
        FitnessGoal saved = FitnessGoal.builder().id(2L).userId(3L).goalType("Lose Weight").isActive(true).build();
        FitnessGoalResponse response = new FitnessGoalResponse(2L, 3L, "Lose Weight", new BigDecimal("75.00"), true,
                LocalDate.of(2026, 8, 1));

        when(fitnessGoalRepository.existsByUserIdAndGoalTypeAndDeadline(any(), any(), any())).thenReturn(false);
        when(fitnessGoalRepository.findActiveGoalsByUserId(3L)).thenReturn(List.of(existingActiveGoal));
        when(fitnessGoalMapper.toEntity(request)).thenReturn(entity);
        when(fitnessGoalRepository.save(any(FitnessGoal.class))).thenReturn(saved);
        when(fitnessGoalMapper.toResponse(saved)).thenReturn(response);

        FitnessGoalResponse result = fitnessGoalService.create(request);

        assertThat(result.getId()).isEqualTo(2L);
        verify(fitnessGoalRepository).save(existingActiveGoal);
        verify(existingActiveGoal).setIsActive(false);
    }
}
