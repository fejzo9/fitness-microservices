package com.app.fitness.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // Import JsonProperty
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {

    private Long id;
    private String name;
    private String description;
    private String difficulty;
    @JsonProperty("categories")
    private List<ExerciseCategoryResponse> categories;
}
