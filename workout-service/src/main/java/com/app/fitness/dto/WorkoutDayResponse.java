package com.app.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDayResponse {

    private Long id;
    private Long workoutPlanId;
    private String dayName;
    private Integer orderIndex;
}
