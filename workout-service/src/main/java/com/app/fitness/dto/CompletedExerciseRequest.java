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
public class CompletedExerciseRequest {

    @NotNull(message = "Completed workout ID must not be null")
    private Long completedWorkoutId;

    @NotNull(message = "Exercise ID must not be null")
    private Long exerciseId;

    private Integer setsDone;
    private Integer repsDone;
}
