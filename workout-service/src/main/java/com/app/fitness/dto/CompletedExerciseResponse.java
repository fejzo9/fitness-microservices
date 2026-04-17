package com.app.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedExerciseResponse {

    private Long id;
    private Long completedWorkoutId;
    private Long exerciseId;
    private String exerciseName;
    private Integer setsDone;
    private Integer repsDone;
}
