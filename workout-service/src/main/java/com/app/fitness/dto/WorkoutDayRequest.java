package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDayRequest {

    @NotNull(message = "Workout plan ID must not be null")
    private Long workoutPlanId;

    @NotBlank(message = "Day name must not be blank")
    @Size(max = 50, message = "Day name must not exceed 50 characters")
    private String dayName;

    @NotNull(message = "Order index must not be null")
    private Integer orderIndex;
}
