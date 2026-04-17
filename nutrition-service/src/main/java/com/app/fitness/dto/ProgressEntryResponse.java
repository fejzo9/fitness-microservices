package com.app.fitness.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEntryResponse {

    private Long id;
    private Long userId;
    private LocalDate entryDate;
    private BigDecimal weightKg;
    private BigDecimal bodyFatPct;
    private String notes;
}
