package com.app.fitness.dto;

import jakarta.validation.constraints.Email;
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
public class UserRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    private String password;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    private Integer age;

    private Integer height;

    private Integer weight;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @NotNull(message = "Role ID must not be null")
    private Long roleId;
}
