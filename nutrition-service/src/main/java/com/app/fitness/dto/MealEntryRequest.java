package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Entry date must not be null")
    private LocalDate entryDate;

    @NotNull(message = "Meal time must not be null")
    private LocalTime mealTime;

    @NotBlank(message = "Meal name must not be blank")
    @Size(max = 200, message = "Meal name must not exceed 200 characters")
    private String mealName;

    @PositiveOrZero(message = "Calories must be zero or positive")
    private BigDecimal calories;

    @PositiveOrZero(message = "Protein must be zero or positive")
    private BigDecimal proteinG;

    @PositiveOrZero(message = "Carbs must be zero or positive")
    private BigDecimal carbsG;

    @PositiveOrZero(message = "Fats must be zero or positive")
    private BigDecimal fatsG;
}
