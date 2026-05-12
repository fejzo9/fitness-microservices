package com.app.fitness.controller;

import com.app.fitness.dto.CompletedExerciseRequest;
import com.app.fitness.dto.CompletedExerciseResponse;
import com.app.fitness.service.CompletedExerciseService;
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
 * REST controller for logging exercises within a completed workout and tracking exercise progress.
 *
 * <p>Base URL: /api/completed-exercises
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/completed-exercises         - Retrieve all completed exercises</li>
 *   <li>GET    /api/completed-exercises/exercise/{exerciseId} - Retrieve completed exercises by exercise ID (progress tracking)</li>
 *   <li>GET    /api/completed-exercises/user/{userId} - Retrieve completed exercises by user ID</li>
 *   <li>GET    /api/completed-exercises/{id}    - Retrieve a completed exercise by ID</li>
 *   <li>POST   /api/completed-exercises         - Log a new completed exercise</li>
 *   <li>PUT    /api/completed-exercises/{id}    - Update a completed exercise entry</li>
 *   <li>DELETE /api/completed-exercises/{id}    - Delete a completed exercise entry</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/completed-exercises")
@RequiredArgsConstructor
public class CompletedExerciseController {

    private final CompletedExerciseService completedExerciseService;

    @GetMapping
    public ResponseEntity<List<CompletedExerciseResponse>> getAll() {
        return ResponseEntity.ok(completedExerciseService.findAll());
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<CompletedExerciseResponse>> getByExerciseId(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(completedExerciseService.findByExerciseId(exerciseId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CompletedExerciseResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(completedExerciseService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompletedExerciseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(completedExerciseService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CompletedExerciseResponse> create(@Valid @RequestBody CompletedExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(completedExerciseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompletedExerciseResponse> update(@PathVariable Long id,
            @Valid @RequestBody CompletedExerciseRequest request) {
        return ResponseEntity.ok(completedExerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        completedExerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
