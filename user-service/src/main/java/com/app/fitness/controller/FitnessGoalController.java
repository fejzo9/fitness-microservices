package com.app.fitness.controller;

import com.app.fitness.dto.FitnessGoalRequest;
import com.app.fitness.dto.FitnessGoalResponse;
import com.app.fitness.service.FitnessGoalService;
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
 * REST controller for managing fitness goals.
 *
 * <p>Base URL: /api/fitness-goals
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/fitness-goals         - Retrieve all fitness goals</li>
 *   <li>GET    /api/fitness-goals/{id}    - Retrieve a fitness goal by ID</li>
 *   <li>POST   /api/fitness-goals         - Create a new fitness goal</li>
 *   <li>PUT    /api/fitness-goals/{id}    - Update an existing fitness goal</li>
 *   <li>DELETE /api/fitness-goals/{id}    - Delete a fitness goal by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/fitness-goals")
@RequiredArgsConstructor
public class FitnessGoalController {

    private final FitnessGoalService fitnessGoalService;

    @GetMapping
    public ResponseEntity<List<FitnessGoalResponse>> getAll() {
        return ResponseEntity.ok(fitnessGoalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FitnessGoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fitnessGoalService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FitnessGoalResponse> create(@Valid @RequestBody FitnessGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fitnessGoalService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FitnessGoalResponse> update(@PathVariable Long id,
            @Valid @RequestBody FitnessGoalRequest request) {
        return ResponseEntity.ok(fitnessGoalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fitnessGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FitnessGoalResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(fitnessGoalService.findByUserId(userId));
    }
    
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<FitnessGoalResponse> getActiveByUserId(@PathVariable Long userId) {
        FitnessGoalResponse goal = fitnessGoalService.findActiveByUserId(userId);
        if (goal == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(goal);
    }
}
