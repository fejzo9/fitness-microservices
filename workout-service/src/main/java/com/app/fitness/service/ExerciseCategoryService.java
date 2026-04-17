package com.app.fitness.service;

import com.app.fitness.dto.ExerciseCategoryRequest;
import com.app.fitness.dto.ExerciseCategoryResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ExerciseCategoryMapper;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.fitness.workoutservice.model.ExerciseCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseCategoryService {

    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final ExerciseCategoryMapper exerciseCategoryMapper;

    @Transactional(readOnly = true)
    public List<ExerciseCategoryResponse> findAll() {
        return exerciseCategoryRepository.findAll().stream()
                .map(exerciseCategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseCategoryResponse findById(Long id) {
        return exerciseCategoryRepository.findById(id)
                .map(exerciseCategoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise category not found with id: " + id));
    }

    @Transactional
    public ExerciseCategoryResponse create(ExerciseCategoryRequest request) {
        if (exerciseCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Exercise category already exists with name: " + request.getName());
        }
        ExerciseCategory category = exerciseCategoryMapper.toEntity(request);
        return exerciseCategoryMapper.toResponse(exerciseCategoryRepository.save(category));
    }

    @Transactional
    public ExerciseCategoryResponse update(Long id, ExerciseCategoryRequest request) {
        ExerciseCategory category = exerciseCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise category not found with id: " + id));
        if (!category.getName().equals(request.getName())
                && exerciseCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Exercise category already exists with name: " + request.getName());
        }
        exerciseCategoryMapper.updateEntity(request, category);
        return exerciseCategoryMapper.toResponse(exerciseCategoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!exerciseCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise category not found with id: " + id);
        }
        exerciseCategoryRepository.deleteById(id);
    }
}
