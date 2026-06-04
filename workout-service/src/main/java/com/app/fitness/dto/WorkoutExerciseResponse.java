package com.app.fitness.dto;

import java.time.DayOfWeek;
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
public class WorkoutExerciseResponse {

    private Long id;
    private Long userId;
    private LocalDate scheduledDate;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private Boolean completed;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Integer restSec;
}
