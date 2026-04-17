package com.app.fitness.controller;

import com.app.fitness.dto.MealItemRequest;
import com.app.fitness.dto.MealItemResponse;
import com.app.fitness.service.MealItemService;
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
 * REST controller for managing meal items.
 *
 * <p>Base URL: /api/meal-items
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/meal-items         - Retrieve all meal items</li>
 *   <li>GET    /api/meal-items/{id}    - Retrieve a meal item by ID</li>
 *   <li>POST   /api/meal-items         - Create a new meal item (mealLogId required in body)</li>
 *   <li>PUT    /api/meal-items/{id}    - Update an existing meal item</li>
 *   <li>DELETE /api/meal-items/{id}    - Delete a meal item by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/meal-items")
@RequiredArgsConstructor
public class MealItemController {

    private final MealItemService mealItemService;

    @GetMapping
    public ResponseEntity<List<MealItemResponse>> getAll() {
        return ResponseEntity.ok(mealItemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mealItemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MealItemResponse> create(@Valid @RequestBody MealItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealItemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealItemResponse> update(@PathVariable Long id,
            @Valid @RequestBody MealItemRequest request) {
        return ResponseEntity.ok(mealItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
