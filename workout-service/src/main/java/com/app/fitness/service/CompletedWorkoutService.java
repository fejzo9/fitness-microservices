package com.app.fitness.service;

import com.app.fitness.client.AuthServiceClient;
import com.app.fitness.dto.AuthUserDto;
import com.app.fitness.dto.CompletedWorkoutRequest;
import com.app.fitness.dto.CompletedWorkoutResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.CompletedWorkoutMapper;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.app.fitness.model.CompletedWorkout;
import com.app.fitness.exception.ServiceUnavailableException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompletedWorkoutService {

    private final CompletedWorkoutRepository completedWorkoutRepository;
    private final CompletedWorkoutMapper completedWorkoutMapper;
    private final AuthServiceClient authServiceClient;
    @Transactional(readOnly = true)
    public List<CompletedWorkoutResponse> findAll() {
        return completedWorkoutRepository.findAll().stream()
                .map(completedWorkoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompletedWorkoutResponse> findByUserId(Long userId) {
        return completedWorkoutRepository.findByUserId(userId).stream()
                .map(completedWorkoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompletedWorkoutResponse findById(Long id) {
        return completedWorkoutRepository.findById(id)
                .map(completedWorkoutMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Completed workout not found with id: " + id));
    }

    @Transactional
    public CompletedWorkoutResponse create(CompletedWorkoutRequest request) {
        ResponseEntity<AuthUserDto> authResponse = authServiceClient.getUserById(request.getUserId());

        if (authResponse.getStatusCode().value() == 503) {
            throw new ServiceUnavailableException("auth-service not available. Try again");
        }
        if (authResponse.getStatusCode().value() == 404 || authResponse.getBody() == null) {
            throw new ResourceNotFoundException("User with ID=" + request.getUserId() + " doesn't exist");
        }
        CompletedWorkout completedWorkout = completedWorkoutMapper.toEntity(request);
        return completedWorkoutMapper.toResponse(completedWorkoutRepository.save(completedWorkout));
    }

    @Transactional
    public CompletedWorkoutResponse update(Long id, CompletedWorkoutRequest request) {
        CompletedWorkout completedWorkout = completedWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Completed workout not found with id: " + id));
        completedWorkoutMapper.updateEntity(request, completedWorkout);
        return completedWorkoutMapper.toResponse(completedWorkoutRepository.save(completedWorkout));
    }

    @Transactional
    public void delete(Long id) {
        if (!completedWorkoutRepository.existsById(id)) {
            throw new ResourceNotFoundException("Completed workout not found with id: " + id);
        }
        completedWorkoutRepository.deleteById(id);
    }
}
