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
public class WorkoutPlanRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotBlank(message = "Plan name must not be blank")
    @Size(max = 150, message = "Plan name must not exceed 150 characters")
    private String name;

    private String description;

    @NotNull(message = "isActive must not be null")
    private Boolean isActive;
}
