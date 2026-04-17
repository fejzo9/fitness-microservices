package com.app.fitness.service;

import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ExerciseMapper;
import com.app.fitness.repository.ExerciseRepository;
import com.fitness.workoutservice.model.Exercise;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional(readOnly = true)
    public List<ExerciseResponse> findAll() {
        return exerciseRepository.findAll().stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseResponse findById(Long id) {
        return exerciseRepository.findById(id)
                .map(exerciseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));
    }

    @Transactional
    public ExerciseResponse create(ExerciseRequest request) {
        if (exerciseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Exercise already exists with name: " + request.getName());
        }
        Exercise exercise = exerciseMapper.toEntity(request);
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseResponse update(Long id, ExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));
        if (!exercise.getName().equals(request.getName()) && exerciseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Exercise already exists with name: " + request.getName());
        }
        exerciseMapper.updateEntity(request, exercise);
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Transactional
    public void delete(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise not found with id: " + id);
        }
        exerciseRepository.deleteById(id);
    }
}
