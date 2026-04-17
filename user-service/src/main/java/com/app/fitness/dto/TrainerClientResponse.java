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
public class TrainerClientResponse {

    private Long id;
    private Long trainerId;
    private Long clientId;
    private LocalDate startDate;
    private String status;
}
