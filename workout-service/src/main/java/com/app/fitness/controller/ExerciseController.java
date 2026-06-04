package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.app.fitness.dto.PageResponse;
import com.app.fitness.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing exercises.
 *
 * <p>Base URL: /api/exercises
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/exercises         - Retrieve all exercises</li>
 *   <li>GET    /api/exercises/{id}    - Retrieve an exercise by ID</li>
 *   <li>POST   /api/exercises         - Create a new exercise</li>
 *   <li>PUT    /api/exercises/{id}    - Update an existing exercise</li>
 *   <li>DELETE /api/exercises/{id}    - Delete an exercise by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<PageResponse<ExerciseResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        if (categoryId != null) {
            return ResponseEntity.ok(exerciseService.findByCategory(categoryId, pageable));
        }
        return ResponseEntity.ok(exerciseService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ExerciseResponse>> search(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(exerciseService.searchByName(name, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> create(@Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> update(@PathVariable Long id,
            @Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
