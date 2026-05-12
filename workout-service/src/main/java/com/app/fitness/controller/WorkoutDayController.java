package com.app.fitness.controller;

import com.app.fitness.dto.WorkoutDayRequest;
import com.app.fitness.dto.WorkoutDayResponse;
import com.app.fitness.service.WorkoutDayService;
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
 * REST controller for managing workout days within a plan.
 *
 * <p>Base URL: /api/workout-days
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/workout-days         - Retrieve all workout days</li>
 *   <li>GET    /api/workout-days/plan/{workoutPlanId} - Retrieve workout days by workout plan ID</li>
 *   <li>GET    /api/workout-days/{id}    - Retrieve a workout day by ID</li>
 *   <li>POST   /api/workout-days         - Create a new workout day (workoutPlanId required in body)</li>
 *   <li>PUT    /api/workout-days/{id}    - Update an existing workout day</li>
 *   <li>DELETE /api/workout-days/{id}    - Delete a workout day by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/workout-days")
@RequiredArgsConstructor
public class WorkoutDayController {

    private final WorkoutDayService workoutDayService;

    @GetMapping
    public ResponseEntity<List<WorkoutDayResponse>> getAll() {
        return ResponseEntity.ok(workoutDayService.findAll());
    }

    @GetMapping("/plan/{workoutPlanId}")
    public ResponseEntity<List<WorkoutDayResponse>> getByWorkoutPlanId(@PathVariable Long workoutPlanId) {
        return ResponseEntity.ok(workoutDayService.findByWorkoutPlanId(workoutPlanId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutDayResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutDayService.findById(id));
    }

    @PostMapping
    public ResponseEntity<WorkoutDayResponse> create(@Valid @RequestBody WorkoutDayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutDayService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutDayResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkoutDayRequest request) {
        return ResponseEntity.ok(workoutDayService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutDayService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
