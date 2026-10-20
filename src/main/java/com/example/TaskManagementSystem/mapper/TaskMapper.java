package com.example.TaskManagementSystem.mapper;

import com.example.TaskManagementSystem.dto.TaskResponse;
import com.example.TaskManagementSystem.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
                task.getUser() == null ? null : task.getUser().getId());
    }
}
