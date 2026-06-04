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
import java.time.LocalDate;
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

    private WorkoutExerciseResponse toResponseWithDay(WorkoutExercise entity) {
        WorkoutExerciseResponse response = workoutExerciseMapper.toResponse(entity);
        if (entity.getScheduledDate() != null) {
            response.setDayOfWeek(entity.getScheduledDate().getDayOfWeek());
        }
        return response;
    }

    private LocalDate[] currentWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        return new LocalDate[]{monday, sunday};
    }

    private LocalDate[] nextWeekRange() {
        LocalDate[] current = currentWeekRange();
        return new LocalDate[]{current[0].plusWeeks(1), current[1].plusWeeks(1)};
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findAll() {
        LocalDate[] range = currentWeekRange();
        return workoutExerciseRepository.findAll().stream()
                .filter(e -> e.getScheduledDate() != null
                        && !e.getScheduledDate().isBefore(range[0])
                        && !e.getScheduledDate().isAfter(range[1]))
                .map(this::toResponseWithDay)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findByUserId(Long userId, boolean nextWeek) {
        LocalDate[] range = nextWeek ? nextWeekRange() : currentWeekRange();
        return workoutExerciseRepository.findByUserIdAndScheduledDateBetween(userId, range[0], range[1]).stream()
                .map(this::toResponseWithDay)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findByUserIdAndDay(Long userId, DayOfWeek day) {
        LocalDate[] range = currentWeekRange();
        // Nađi datum u tekućoj sedmici koji odgovara traženom danu
        LocalDate targetDate = range[0];
        while (!targetDate.getDayOfWeek().equals(day)) {
            targetDate = targetDate.plusDays(1);
        }
        return workoutExerciseRepository.findByUserIdAndScheduledDate(userId, targetDate).stream()
                .map(this::toResponseWithDay)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutExerciseResponse findById(Long id) {
        return workoutExerciseRepository.findById(id)
                .map(this::toResponseWithDay)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found with id: " + id));
    }

    @Transactional
    public WorkoutExerciseResponse create(WorkoutExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + request.getExerciseId()));
        if (workoutExerciseRepository.existsByUserIdAndScheduledDateAndExercise(
                request.getUserId(), request.getScheduledDate(), exercise)) {
            throw new DuplicateResourceException(
                    "Exercise already assigned to this user on this date");
        }
        WorkoutExercise workoutExercise = workoutExerciseMapper.toEntity(request);
        workoutExercise.setExercise(exercise);
        return toResponseWithDay(workoutExerciseRepository.save(workoutExercise));
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
        return toResponseWithDay(workoutExerciseRepository.save(workoutExercise));
    }

    @Transactional(readOnly = true)
    public WorkoutWeeklyStatisticsResponse getWeeklyStatistics(Long userId) {
        LocalDate[] range = currentWeekRange();
        List<WorkoutExercise> exercises = workoutExerciseRepository
                .findByUserIdAndScheduledDateBetween(userId, range[0], range[1]);
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
    public WorkoutExerciseResponse markCompleted(Long id) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found with id: " + id));
        workoutExercise.setCompleted(true);
        return toResponseWithDay(workoutExerciseRepository.save(workoutExercise));
    }

    @Transactional
    public void delete(Long id) {
        if (!workoutExerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workout exercise not found with id: " + id);
        }
        workoutExerciseRepository.deleteById(id);
    }
}
