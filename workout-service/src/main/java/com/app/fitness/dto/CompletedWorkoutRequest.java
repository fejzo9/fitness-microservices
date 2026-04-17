package com.app.fitness.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedWorkoutRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    private Long workoutPlanId;

    @NotNull(message = "Date must not be null")
    private LocalDate date;

    private Integer durationMin;
}
