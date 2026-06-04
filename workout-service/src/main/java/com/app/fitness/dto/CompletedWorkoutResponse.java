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
public class CompletedWorkoutResponse {

    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer durationMin;
}
