package com.example.TaskManagementSystem.dto;

public record TaskResponse(Long id, String title, String description, String status, Long userId) {
}
