package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutPlanRequest;
import com.app.fitness.dto.WorkoutPlanResponse;
import com.app.fitness.service.WorkoutPlanService;
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
 * REST controller for managing workout plans.
 *
 * <p>Base URL: /api/workout-plans
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/workout-plans         - Retrieve all workout plans</li>
 *   <li>GET    /api/workout-plans/{id}    - Retrieve a workout plan by ID</li>
 *   <li>POST   /api/workout-plans         - Create a new workout plan</li>
 *   <li>PUT    /api/workout-plans/{id}    - Update an existing workout plan</li>
 *   <li>DELETE /api/workout-plans/{id}    - Delete a workout plan by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @GetMapping
    public ResponseEntity<List<WorkoutPlanResponse>> getAll() {
        return ResponseEntity.ok(workoutPlanService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutPlanService.findById(id));
    }

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> create(@Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutPlanService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(workoutPlanService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
