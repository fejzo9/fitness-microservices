package com.app.fitness.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WorkoutNotificationMessage implements Serializable {
    private Long userId;
    private Integer completedExercises;
    private Integer uncompletedExercises;
    private String date;
}
