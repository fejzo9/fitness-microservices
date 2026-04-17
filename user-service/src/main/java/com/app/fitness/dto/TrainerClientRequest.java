package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerClientRequest {

    @NotNull(message = "Trainer ID must not be null")
    private Long trainerId;

    @NotNull(message = "Client ID must not be null")
    private Long clientId;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotBlank(message = "Status must not be blank")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;
}
