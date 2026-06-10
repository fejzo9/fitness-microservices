package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.app.fitness.dto.WorkoutWeeklyStatisticsResponse;
import com.app.fitness.service.WorkoutExerciseService;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing workout exercises.
 *
 * <p>Base URL: /api/workout-exercises
 */
@RestController
@RequestMapping("/api/workout-exercises")
@RequiredArgsConstructor
public class WorkoutExerciseController {

    private final WorkoutExerciseService workoutExerciseService;

    @GetMapping
    public ResponseEntity<List<WorkoutExerciseResponse>> getAll() {
        return ResponseEntity.ok(workoutExerciseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutExerciseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutExerciseService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkoutExerciseResponse>> getByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean nextWeek) {
        return ResponseEntity.ok(workoutExerciseService.findByUserId(userId, nextWeek));
    }

    @GetMapping("/user/{userId}/day/{day}")
    public ResponseEntity<List<WorkoutExerciseResponse>> getByUserIdAndDay(
            @PathVariable Long userId,
            @PathVariable DayOfWeek day) {
        return ResponseEntity.ok(workoutExerciseService.findByUserIdAndDay(userId, day));
    }

    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<WorkoutExerciseResponse>> getCompletedByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(workoutExerciseService.findCompletedByUserId(userId));
    }

    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<WorkoutWeeklyStatisticsResponse> getWeeklyStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(workoutExerciseService.getWeeklyStatistics(userId));
    }

    @PostMapping
    public ResponseEntity<WorkoutExerciseResponse> create(@Valid @RequestBody WorkoutExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutExerciseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutExerciseResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkoutExerciseRequest request) {
        return ResponseEntity.ok(workoutExerciseService.update(id, request));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<WorkoutExerciseResponse> markCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(workoutExerciseService.markCompleted(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutExerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
