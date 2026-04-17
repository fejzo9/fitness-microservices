package com.app.fitness.service;

import com.app.fitness.dto.CompletedWorkoutRequest;
import com.app.fitness.dto.CompletedWorkoutResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.CompletedWorkoutMapper;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.fitness.workoutservice.model.CompletedWorkout;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompletedWorkoutService {

    private final CompletedWorkoutRepository completedWorkoutRepository;
    private final CompletedWorkoutMapper completedWorkoutMapper;

    @Transactional(readOnly = true)
    public List<CompletedWorkoutResponse> findAll() {
        return completedWorkoutRepository.findAll().stream()
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
