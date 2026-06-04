package com.app.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutWeeklyStatisticsResponse {
    private Long userId;
    private Integer totalPlannedExercises;
    private Integer totalCompletedExercises;
    private Double completionPercentage;
}
