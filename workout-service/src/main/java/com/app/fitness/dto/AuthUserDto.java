package com.app.fitness.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AuthUserDto {
    private Long id;
    private String username;
    private String email;
    private String roleName;
    private LocalDateTime createdAt;
}