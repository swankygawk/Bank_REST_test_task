package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 5, max = 50, message = "Username must be 5-50 characters long")
    String username,

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 255, message = "Password must be 8-255 characters long")
    String password
) {}
