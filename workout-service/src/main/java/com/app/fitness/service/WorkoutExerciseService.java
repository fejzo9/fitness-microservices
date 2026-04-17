package com.app.fitness.service;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutExerciseMapper;
import com.app.fitness.repository.ExerciseRepository;
import com.app.fitness.repository.WorkoutDayRepository;
import com.app.fitness.repository.WorkoutExerciseRepository;
import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutExercise;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutExerciseService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutExerciseMapper workoutExerciseMapper;

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findAll() {
        return workoutExerciseRepository.findAll().stream()
                .map(workoutExerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutExerciseResponse findById(Long id) {
        return workoutExerciseRepository.findById(id)
                .map(workoutExerciseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found with id: " + id));
    }

    @Transactional
    public WorkoutExerciseResponse create(WorkoutExerciseRequest request) {
        WorkoutDay day = workoutDayRepository.findById(request.getWorkoutDayId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout day not found with id: " + request.getWorkoutDayId()));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        if (workoutExerciseRepository.existsByWorkoutDayAndExercise(day, exercise)) {
            throw new DuplicateResourceException(
                    "Exercise already assigned to this workout day");
        }
        WorkoutExercise workoutExercise = workoutExerciseMapper.toEntity(request);
        workoutExercise.setWorkoutDay(day);
        workoutExercise.setExercise(exercise);
        return workoutExerciseMapper.toResponse(workoutExerciseRepository.save(workoutExercise));
    }

    @Transactional
    public WorkoutExerciseResponse update(Long id, WorkoutExerciseRequest request) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found with id: " + id));
        WorkoutDay day = workoutDayRepository.findById(request.getWorkoutDayId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout day not found with id: " + request.getWorkoutDayId()));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        workoutExerciseMapper.updateEntity(request, workoutExercise);
        workoutExercise.setWorkoutDay(day);
        workoutExercise.setExercise(exercise);
        return workoutExerciseMapper.toResponse(workoutExerciseRepository.save(workoutExercise));
    }

    @Transactional
    public void delete(Long id) {
        if (!workoutExerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workout exercise not found with id: " + id);
        }
        workoutExerciseRepository.deleteById(id);
    }
}
