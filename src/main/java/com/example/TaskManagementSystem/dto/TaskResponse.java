package com.example.TaskManagementSystem.dto;

import com.example.TaskManagementSystem.entity.User;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private Long user_id;

    public TaskResponse(Long id, String title, String description, String status, Long user_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.user_id = user_id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public Long getUser_id() {
        return user_id;
    }
}
