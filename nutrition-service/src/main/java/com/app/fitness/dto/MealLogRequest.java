package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLogRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Log date must not be null")
    private LocalDate logDate;

    @NotBlank(message = "Meal type must not be blank")
    @Size(max = 50, message = "Meal type must not exceed 50 characters")
    private String mealType;
}
