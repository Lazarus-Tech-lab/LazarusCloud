package ru.red.lazaruscloud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 5, message = "min length 5")
        String username,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "min length 8")
        String password,
        @NotBlank(message = "Username cannot be blank")
        @Email(message = "Invalid email format")
        String email) {}
