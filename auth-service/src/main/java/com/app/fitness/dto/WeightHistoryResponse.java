package com.app.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightHistoryResponse {
    private Long id;
    private Long userId;
    private Integer weight;
    private LocalDate entryDate;
}
