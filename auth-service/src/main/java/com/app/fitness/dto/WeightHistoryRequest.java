package com.app.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightHistoryRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Integer weight;
    @NotNull
    private LocalDate entryDate;
}
