package com.app.fitness.service;

import com.app.fitness.client.AuthServiceClient;
import com.app.fitness.dto.AuthUserDto;
import com.app.fitness.dto.CompletedWorkoutRequest;
import com.app.fitness.dto.CompletedWorkoutResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.exception.ServiceUnavailableException;
import com.app.fitness.mapper.CompletedWorkoutMapper;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.fitness.workoutservice.model.CompletedWorkout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletedWorkoutServiceTest {

    @Mock
    private CompletedWorkoutRepository completedWorkoutRepository;

    @Mock
    private CompletedWorkoutMapper completedWorkoutMapper;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private CompletedWorkoutService completedWorkoutService;

    private CompletedWorkoutRequest request;
    private CompletedWorkout completedWorkout;
    private CompletedWorkoutResponse response;
    private AuthUserDto authUserDto;

    @BeforeEach
    void setUp() {
        request = CompletedWorkoutRequest.builder()
                .userId(1L)
                .workoutPlanId(10L)
                .date(LocalDate.of(2026, 5, 14))
                .durationMin(60)
                .build();

        completedWorkout = new CompletedWorkout();

        response = CompletedWorkoutResponse.builder()
                .id(1L)
                .userId(1L)
                .workoutPlanId(10L)
                .date(LocalDate.of(2026, 5, 14))
                .durationMin(60)
                .build();

        authUserDto = new AuthUserDto();
        authUserDto.setId(1L);
        authUserDto.setUsername("testuser");
        authUserDto.setEmail("test@example.com");
    }

    // -----------------------------------------------------------------------
    // create() — sinhrona komunikacija s auth-service
    // -----------------------------------------------------------------------

    @Test
    void create_shouldSaveAndReturnResponse_whenUserExistsInAuthService() {
        when(authServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(authUserDto));
        when(completedWorkoutMapper.toEntity(request)).thenReturn(completedWorkout);
        when(completedWorkoutRepository.save(completedWorkout)).thenReturn(completedWorkout);
        when(completedWorkoutMapper.toResponse(completedWorkout)).thenReturn(response);

        CompletedWorkoutResponse result = completedWorkoutService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getDurationMin()).isEqualTo(60);
        // Provjeri da je auth-service pozvan tačno jednom s ispravnim userId
        verify(authServiceClient, times(1)).getUserById(1L);
        verify(completedWorkoutRepository).save(completedWorkout);
    }

    @Test
    void create_shouldThrowServiceUnavailableException_whenAuthServiceReturns503() {
        // Simulira aktivirani fallback — auth-service nije dostupan
        when(authServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

        assertThatThrownBy(() -> completedWorkoutService.create(request))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("auth-service not available");

        // Workout se nikad ne smije snimiti ako auth-service nije dostupan
        verify(completedWorkoutRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenAuthServiceReturns404() {
        // Korisnik ne postoji u auth-service
        when(authServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        assertThatThrownBy(() -> completedWorkoutService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with ID=1 doesn't exist");

        verify(completedWorkoutRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenAuthResponseBodyIsNull() {
        // auth-service vraća 200 ali bez tijela — tretiramo kao nepostojeći korisnik
        when(authServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> completedWorkoutService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with ID=1 doesn't exist");

        verify(completedWorkoutRepository, never()).save(any());
    }

    @Test
    void create_shouldNotCallRepository_beforeAuthServiceValidation() {
        // Provjeri redoslijed: auth-service se mora pozvati PRIJE snimanja
        when(authServiceClient.getUserById(1L))
                .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

        try {
            completedWorkoutService.create(request);
        } catch (ServiceUnavailableException ignored) {}

        verify(authServiceClient).getUserById(1L);
        verifyNoInteractions(completedWorkoutRepository);
    }

    // -----------------------------------------------------------------------
    // findAll()
    // -----------------------------------------------------------------------

    @Test
    void findAll_shouldReturnListOfResponses() {
        when(completedWorkoutRepository.findAll()).thenReturn(List.of(completedWorkout));
        when(completedWorkoutMapper.toResponse(completedWorkout)).thenReturn(response);

        List<CompletedWorkoutResponse> result = completedWorkoutService.findAll();

        assertThat(result).hasSize(1);
        verify(completedWorkoutRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoWorkoutsExist() {
        when(completedWorkoutRepository.findAll()).thenReturn(List.of());

        List<CompletedWorkoutResponse> result = completedWorkoutService.findAll();

        assertThat(result).isEmpty();
        verify(completedWorkoutRepository).findAll();
    }

    // -----------------------------------------------------------------------
    // findByUserId()
    // -----------------------------------------------------------------------

    @Test
    void findByUserId_shouldReturnWorkoutsForGivenUser() {
        when(completedWorkoutRepository.findByUserId(1L)).thenReturn(List.of(completedWorkout));
        when(completedWorkoutMapper.toResponse(completedWorkout)).thenReturn(response);

        List<CompletedWorkoutResponse> result = completedWorkoutService.findByUserId(1L);

        assertThat(result).hasSize(1);
        verify(completedWorkoutRepository).findByUserId(1L);
    }

    @Test
    void findByUserId_shouldReturnEmptyList_whenUserHasNoWorkouts() {
        when(completedWorkoutRepository.findByUserId(99L)).thenReturn(List.of());

        List<CompletedWorkoutResponse> result = completedWorkoutService.findByUserId(99L);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // findById()
    // -----------------------------------------------------------------------

    @Test
    void findById_shouldReturnResponse_whenWorkoutExists() {
        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(completedWorkout));
        when(completedWorkoutMapper.toResponse(completedWorkout)).thenReturn(response);

        CompletedWorkoutResponse result = completedWorkoutService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenWorkoutNotFound() {
        when(completedWorkoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedWorkoutService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Completed workout not found with id: 99");
    }

    // -----------------------------------------------------------------------
    // update()
    // -----------------------------------------------------------------------

    @Test
    void update_shouldUpdateAndReturnResponse_whenWorkoutExists() {
        when(completedWorkoutRepository.findById(1L)).thenReturn(Optional.of(completedWorkout));
        when(completedWorkoutRepository.save(completedWorkout)).thenReturn(completedWorkout);
        when(completedWorkoutMapper.toResponse(completedWorkout)).thenReturn(response);

        CompletedWorkoutResponse result = completedWorkoutService.update(1L, request);

        assertThat(result).isNotNull();
        verify(completedWorkoutMapper).updateEntity(request, completedWorkout);
        verify(completedWorkoutRepository).save(completedWorkout);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenWorkoutNotFound() {
        when(completedWorkoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completedWorkoutService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Completed workout not found with id: 99");

        verify(completedWorkoutRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // delete()
    // -----------------------------------------------------------------------

    @Test
    void delete_shouldDeleteWorkout_whenWorkoutExists() {
        when(completedWorkoutRepository.existsById(1L)).thenReturn(true);

        completedWorkoutService.delete(1L);

        verify(completedWorkoutRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenWorkoutNotFound() {
        when(completedWorkoutRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> completedWorkoutService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Completed workout not found with id: 99");

        verify(completedWorkoutRepository, never()).deleteById(any());
    }
}