package com.app.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseCategoryMapResponse {

    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Long categoryId;
    private String categoryName;
}
