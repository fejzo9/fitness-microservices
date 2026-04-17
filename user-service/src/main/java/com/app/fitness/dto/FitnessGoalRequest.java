package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitnessGoalRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotBlank(message = "Goal type must not be blank")
    @Size(max = 100, message = "Goal type must not exceed 100 characters")
    private String goalType;

    private BigDecimal targetValue;

    @NotNull(message = "isActive must not be null")
    private Boolean isActive;

    private LocalDate deadline;
}
