package ru.red.lazaruscloud.dto.authDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto (
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 5, message = "min length 5")
    String username,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "min length 6")
    String password
){}
