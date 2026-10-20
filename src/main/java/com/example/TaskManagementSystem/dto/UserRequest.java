package com.example.TaskManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Pattern(regexp = "\\+?[0-9]{7,15}", message = "must contain 7 to 15 digits with an optional leading +") String phone,
        @NotBlank @Size(min = 8, max = 64) String password) {
}
