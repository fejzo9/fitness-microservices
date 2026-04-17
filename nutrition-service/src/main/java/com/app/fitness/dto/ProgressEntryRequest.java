package com.app.fitness.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class ProgressEntryRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Entry date must not be null")
    private LocalDate entryDate;

    @PositiveOrZero(message = "Weight must be zero or positive")
    private BigDecimal weightKg;

    @PositiveOrZero(message = "Body fat percentage must be zero or positive")
    private BigDecimal bodyFatPct;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}
