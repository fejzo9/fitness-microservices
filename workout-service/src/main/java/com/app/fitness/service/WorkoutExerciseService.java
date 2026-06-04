package com.app.fitness.service;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.app.fitness.dto.WorkoutWeeklyStatisticsResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutExerciseMapper;
import com.app.fitness.repository.ExerciseRepository;
import com.app.fitness.repository.WorkoutExerciseRepository;
import com.app.fitness.model.Exercise;
import com.app.fitness.model.WorkoutExercise;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutExerciseService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutExerciseMapper workoutExerciseMapper;

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findAll() {
        return workoutExerciseRepository.findAll().stream()
                .map(workoutExerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findByUserId(Long userId) {
        return workoutExerciseRepository.findByUserId(userId).stream()
                .map(workoutExerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findByUserIdAndDay(Long userId, DayOfWeek day) {
        return workoutExerciseRepository.findByUserIdAndDayOfWeek(userId, day).stream()
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
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        if (workoutExerciseRepository.existsByUserIdAndDayOfWeekAndExercise(
                request.getUserId(), request.getDayOfWeek(), exercise)) {
            throw new DuplicateResourceException(
                    "Exercise already assigned to this user on this day");
        }
        WorkoutExercise workoutExercise = workoutExerciseMapper.toEntity(request);
        workoutExercise.setExercise(exercise);
        return workoutExerciseMapper.toResponse(workoutExerciseRepository.save(workoutExercise));
    }

    @Transactional
    public WorkoutExerciseResponse update(Long id, WorkoutExerciseRequest request) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found with id: " + id));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        workoutExerciseMapper.updateEntity(request, workoutExercise);
        workoutExercise.setExercise(exercise);
        return workoutExerciseMapper.toResponse(workoutExerciseRepository.save(workoutExercise));
    }

    @Transactional(readOnly = true)
    public WorkoutWeeklyStatisticsResponse getWeeklyStatistics(Long userId) {
        List<WorkoutExercise> exercises = workoutExerciseRepository.findByUserId(userId);
        int total = exercises.size();
        long completed = exercises.stream()
                .filter(ex -> Boolean.TRUE.equals(ex.getCompleted()))
                .count();
        double percentage = total > 0 ? (double) completed / total * 100 : 0.0;
        return WorkoutWeeklyStatisticsResponse.builder()
                .userId(userId)
                .totalPlannedExercises(total)
                .totalCompletedExercises((int) completed)
                .completionPercentage(percentage)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        if (!workoutExerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workout exercise not found with id: " + id);
        }
        workoutExerciseRepository.deleteById(id);
    }
}
