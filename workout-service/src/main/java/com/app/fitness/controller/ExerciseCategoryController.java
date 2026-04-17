package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseCategoryRequest;
import com.app.fitness.dto.ExerciseCategoryResponse;
import com.app.fitness.service.ExerciseCategoryService;
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
 * REST controller for managing exercise categories.
 *
 * <p>Base URL: /api/exercise-categories
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/exercise-categories         - Retrieve all exercise categories</li>
 *   <li>GET    /api/exercise-categories/{id}    - Retrieve a category by ID</li>
 *   <li>POST   /api/exercise-categories         - Create a new category</li>
 *   <li>PUT    /api/exercise-categories/{id}    - Update an existing category</li>
 *   <li>DELETE /api/exercise-categories/{id}    - Delete a category by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/exercise-categories")
@RequiredArgsConstructor
public class ExerciseCategoryController {

    private final ExerciseCategoryService exerciseCategoryService;

    @GetMapping
    public ResponseEntity<List<ExerciseCategoryResponse>> getAll() {
        return ResponseEntity.ok(exerciseCategoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseCategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseCategoryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ExerciseCategoryResponse> create(@Valid @RequestBody ExerciseCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseCategoryResponse> update(@PathVariable Long id,
            @Valid @RequestBody ExerciseCategoryRequest request) {
        return ResponseEntity.ok(exerciseCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
