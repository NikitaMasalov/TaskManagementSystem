package com.example.TaskManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank @Size(max = 50) String title,
        @NotBlank @Size(max = 255) String description,
        @NotBlank @Size(max = 255) String status,
        @Positive Long userId) {
}
