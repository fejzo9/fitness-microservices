package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRequest {

    @NotBlank(message = "Exercise name must not be blank")
    @Size(max = 150, message = "Exercise name must not exceed 150 characters")
    private String name;

    private String description;

    @Size(max = 20, message = "Difficulty must not exceed 20 characters")
    private String difficulty;

    private List<ExerciseCategoryType> categories;
}
