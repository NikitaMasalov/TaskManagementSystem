package com.example.TaskManagementSystem.mapper;

import com.example.TaskManagementSystem.dto.TaskRequest;
import com.example.TaskManagementSystem.dto.TaskResponse;
import com.example.TaskManagementSystem.entity.Task;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class TaskMapper {

    public Task fromCreateRequest(TaskRequest request) {
        return new Task(
                request.getTitle(),
                request.getDescription(),
                request.getStatus()
        );
    }

    public TaskResponse toResponse (Task task){
        return  new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getUser().getId());
    }
}
