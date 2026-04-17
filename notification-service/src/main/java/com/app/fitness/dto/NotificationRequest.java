package com.app.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotBlank(message = "Message must not be blank")
    private String message;

    @NotBlank(message = "Type must not be blank")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    @NotNull(message = "isRead must not be null")
    private Boolean isRead;
}
