package com.app.fitness.service;

import com.app.fitness.dto.ExerciseCategoryType;
import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.app.fitness.dto.PageResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ExerciseMapper;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.ExerciseCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional(readOnly = true)
    public PageResponse<ExerciseResponse> findAll(Pageable pageable) {
        return PageResponse.of(exerciseRepository.findAll(pageable).map(exerciseMapper::toResponse));
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
        exercise.setCategories(resolveCategories(request.getCategories()));
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
        // Mutate the existing collection so Hibernate tracks join-table changes correctly.
        exercise.getCategories().clear();
        exercise.getCategories().addAll(resolveCategories(request.getCategories()));
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Transactional
    public void delete(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise not found with id: " + id);
        }
        exerciseRepository.deleteById(id);
    }

    private Set<ExerciseCategory> resolveCategories(List<ExerciseCategoryType> requestedTypes) {
        if (requestedTypes == null || requestedTypes.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> names = requestedTypes.stream()
                .map(t -> t.name().replace("_", " "))
                .collect(Collectors.toSet());
        List<ExerciseCategory> found = exerciseCategoryRepository.findByNameIn(List.copyOf(names));
        if (found.size() != names.size()) {
            Set<String> foundNames = found.stream().map(ExerciseCategory::getName).collect(Collectors.toSet());
            String missing = names.stream().filter(n -> !foundNames.contains(n)).findFirst().orElse("unknown");
            throw new ResourceNotFoundException("Exercise category not found with name: " + missing);
        }
        return new HashSet<>(found);
    }
}
