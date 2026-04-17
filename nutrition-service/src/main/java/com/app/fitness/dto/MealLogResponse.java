package com.app.fitness.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLogResponse {

    private Long id;
    private Long userId;
    private LocalDate logDate;
    private String mealType;
}
