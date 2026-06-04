package com.app.fitness.service;

import com.app.fitness.dto.CompletedExerciseRequest;
import com.app.fitness.dto.CompletedExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.CompletedExerciseMapper;
import com.app.fitness.repository.CompletedExerciseRepository;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.app.fitness.model.CompletedExercise;
import com.app.fitness.model.CompletedWorkout;
import com.app.fitness.model.Exercise;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompletedExerciseService {

    private final CompletedExerciseRepository completedExerciseRepository;
    private final CompletedWorkoutRepository completedWorkoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final CompletedExerciseMapper completedExerciseMapper;

    @Transactional(readOnly = true)
    public List<CompletedExerciseResponse> findAll() {
        return completedExerciseRepository.findAll().stream()
                .map(completedExerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompletedExerciseResponse> findByExerciseId(Long exerciseId) {
        return completedExerciseRepository.findByExerciseId(exerciseId).stream()
                .map(completedExerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompletedExerciseResponse> findByUserId(Long userId) {
        return completedExerciseRepository.findByCompletedWorkoutUserId(userId).stream()
                .map(completedExerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompletedExerciseResponse findById(Long id) {
        return completedExerciseRepository.findById(id)
                .map(completedExerciseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Completed exercise not found with id: " + id));
    }

    @Transactional
    public CompletedExerciseResponse create(CompletedExerciseRequest request) {
        CompletedWorkout completedWorkout = completedWorkoutRepository.findById(request.getCompletedWorkoutId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Completed workout not found with id: " + request.getCompletedWorkoutId()));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        if (completedExerciseRepository.existsByCompletedWorkoutAndExercise(completedWorkout, exercise)) {
            throw new DuplicateResourceException(
                    "Exercise already logged for this completed workout");
        }
        CompletedExercise completedExercise = completedExerciseMapper.toEntity(request);
        completedExercise.setCompletedWorkout(completedWorkout);
        completedExercise.setExercise(exercise);
        return completedExerciseMapper.toResponse(completedExerciseRepository.save(completedExercise));
    }

    @Transactional
    public CompletedExerciseResponse update(Long id, CompletedExerciseRequest request) {
        CompletedExercise completedExercise = completedExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Completed exercise not found with id: " + id));
        CompletedWorkout completedWorkout = completedWorkoutRepository.findById(request.getCompletedWorkoutId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Completed workout not found with id: " + request.getCompletedWorkoutId()));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        completedExerciseMapper.updateEntity(request, completedExercise);
        completedExercise.setCompletedWorkout(completedWorkout);
        completedExercise.setExercise(exercise);
        return completedExerciseMapper.toResponse(completedExerciseRepository.save(completedExercise));
    }

    @Transactional
    public void delete(Long id) {
        if (!completedExerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Completed exercise not found with id: " + id);
        }
        completedExerciseRepository.deleteById(id);
    }
}
