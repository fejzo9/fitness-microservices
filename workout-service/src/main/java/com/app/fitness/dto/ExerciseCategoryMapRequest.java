package com.app.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseCategoryMapRequest {

    @NotNull(message = "Exercise ID must not be null")
    private Long exerciseId;

    @NotNull(message = "Category ID must not be null")
    private Long categoryId;
}
