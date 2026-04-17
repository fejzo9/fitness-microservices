package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealItemRequest {

    @NotNull(message = "Meal log ID must not be null")
    private Long mealLogId;

    @NotBlank(message = "Food name must not be blank")
    @Size(max = 200, message = "Food name must not exceed 200 characters")
    private String foodName;

    @PositiveOrZero(message = "Quantity must be zero or positive")
    private BigDecimal quantityG;

    @PositiveOrZero(message = "Calories must be zero or positive")
    private BigDecimal calories;

    @PositiveOrZero(message = "Protein must be zero or positive")
    private BigDecimal proteinG;

    @PositiveOrZero(message = "Carbs must be zero or positive")
    private BigDecimal carbsG;

    @PositiveOrZero(message = "Fats must be zero or positive")
    private BigDecimal fatsG;
}
