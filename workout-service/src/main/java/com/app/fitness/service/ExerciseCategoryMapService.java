package com.app.fitness.service;

import com.app.fitness.dto.ExerciseCategoryMapRequest;
import com.app.fitness.dto.ExerciseCategoryMapResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ExerciseCategoryMapMapper;
import com.app.fitness.repository.ExerciseCategoryMapRepository;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.ExerciseCategory;
import com.fitness.workoutservice.model.ExerciseCategoryMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseCategoryMapService {

    private final ExerciseCategoryMapRepository mapRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository categoryRepository;
    private final ExerciseCategoryMapMapper mapMapper;

    @Transactional(readOnly = true)
    public List<ExerciseCategoryMapResponse> findAll() {
        return mapRepository.findAll().stream()
                .map(mapMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseCategoryMapResponse findById(Long id) {
        return mapRepository.findById(id)
                .map(mapMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise-category mapping not found with id: " + id));
    }

    @Transactional
    public ExerciseCategoryMapResponse create(ExerciseCategoryMapRequest request) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        ExerciseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise category not found with id: " + request.getCategoryId()));
        if (mapRepository.existsByExerciseAndCategory(exercise, category)) {
            throw new DuplicateResourceException(
                    "Mapping already exists for exerciseId=" + request.getExerciseId()
                            + " and categoryId=" + request.getCategoryId());
        }
        ExerciseCategoryMap map = mapMapper.toEntity(request);
        map.setExercise(exercise);
        map.setCategory(category);
        return mapMapper.toResponse(mapRepository.save(map));
    }

    @Transactional
    public void delete(Long id) {
        if (!mapRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise-category mapping not found with id: " + id);
        }
        mapRepository.deleteById(id);
    }
}
