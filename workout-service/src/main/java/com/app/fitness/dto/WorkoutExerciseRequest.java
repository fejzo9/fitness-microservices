package com.app.fitness.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
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

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Day of week must not be null")
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private Boolean completed;

    @NotNull(message = "Exercise ID must not be null")
    private Long exerciseId;

    private Integer sets;
    private Integer reps;
    private Integer restSec;
}
