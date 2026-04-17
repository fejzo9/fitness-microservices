package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.app.fitness.service.WorkoutExerciseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing exercises assigned to a workout day.
 *
 * <p>Base URL: /api/workout-exercises
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/workout-exercises         - Retrieve all workout exercises</li>
 *   <li>GET    /api/workout-exercises/{id}    - Retrieve a workout exercise by ID</li>
 *   <li>POST   /api/workout-exercises         - Assign an exercise to a workout day</li>
 *   <li>PUT    /api/workout-exercises/{id}    - Update an existing workout exercise</li>
 *   <li>DELETE /api/workout-exercises/{id}    - Remove an exercise from a workout day</li>
 * </ul>
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

    @PostMapping
    public ResponseEntity<WorkoutExerciseResponse> create(@Valid @RequestBody WorkoutExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutExerciseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutExerciseResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkoutExerciseRequest request) {
        return ResponseEntity.ok(workoutExerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutExerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
