package com.app.fitness.controller;

import com.app.fitness.dto.CompletedWorkoutRequest;
import com.app.fitness.dto.CompletedWorkoutResponse;
import com.app.fitness.service.CompletedWorkoutService;
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
 * REST controller for managing completed workout sessions.
 *
 * <p>Base URL: /api/completed-workouts
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/completed-workouts         - Retrieve all completed workouts</li>
 *   <li>GET    /api/completed-workouts/user/{userId} - Retrieve completed workouts by user ID</li>
 *   <li>GET    /api/completed-workouts/{id}    - Retrieve a completed workout by ID</li>
 *   <li>POST   /api/completed-workouts         - Log a new completed workout</li>
 *   <li>PUT    /api/completed-workouts/{id}    - Update a completed workout entry</li>
 *   <li>DELETE /api/completed-workouts/{id}    - Delete a completed workout entry</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/completed-workouts")
@RequiredArgsConstructor
public class CompletedWorkoutController {

    private final CompletedWorkoutService completedWorkoutService;

    @GetMapping
    public ResponseEntity<List<CompletedWorkoutResponse>> getAll() {
        return ResponseEntity.ok(completedWorkoutService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CompletedWorkoutResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(completedWorkoutService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompletedWorkoutResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(completedWorkoutService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CompletedWorkoutResponse> create(@Valid @RequestBody CompletedWorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(completedWorkoutService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompletedWorkoutResponse> update(@PathVariable Long id,
            @Valid @RequestBody CompletedWorkoutRequest request) {
        return ResponseEntity.ok(completedWorkoutService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        completedWorkoutService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
