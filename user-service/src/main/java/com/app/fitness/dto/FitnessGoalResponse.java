package com.app.fitness.dto;

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
public class FitnessGoalResponse {

    private Long id;
    private Long userId;
    private String goalType;
    private BigDecimal targetValue;
    private Boolean isActive;
    private LocalDate deadline;
}
