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
public class WorkoutExerciseRequest {

    @NotNull(message = "Workout day ID must not be null")
    private Long workoutDayId;

    @NotNull(message = "Exercise ID must not be null")
    private Long exerciseId;

    private Integer sets;
    private Integer reps;
    private Integer restSec;
}
