package com.app.fitness.controller;

import com.app.fitness.dto.ExerciseCategoryMapRequest;
import com.app.fitness.dto.ExerciseCategoryMapResponse;
import com.app.fitness.service.ExerciseCategoryMapService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing exercise-to-category mappings.
 *
 * <p>Base URL: /api/exercise-category-maps
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/exercise-category-maps         - Retrieve all mappings</li>
 *   <li>GET    /api/exercise-category-maps/category/{categoryId} - Retrieve mappings by category ID</li>
 *   <li>GET    /api/exercise-category-maps/exercise/{exerciseId} - Retrieve mappings by exercise ID</li>
 *   <li>GET    /api/exercise-category-maps/{id}    - Retrieve a mapping by ID</li>
 *   <li>POST   /api/exercise-category-maps         - Create a new mapping</li>
 *   <li>DELETE /api/exercise-category-maps/{id}    - Delete a mapping by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/exercise-category-maps")
@RequiredArgsConstructor
public class ExerciseCategoryMapController {

    private final ExerciseCategoryMapService exerciseCategoryMapService;

    @GetMapping
    public ResponseEntity<List<ExerciseCategoryMapResponse>> getAll() {
        return ResponseEntity.ok(exerciseCategoryMapService.findAll());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExerciseCategoryMapResponse>> getByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(exerciseCategoryMapService.findByCategoryId(categoryId));
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<ExerciseCategoryMapResponse>> getByExerciseId(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(exerciseCategoryMapService.findByExerciseId(exerciseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseCategoryMapResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseCategoryMapService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ExerciseCategoryMapResponse> create(
            @Valid @RequestBody ExerciseCategoryMapRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseCategoryMapService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseCategoryMapService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
