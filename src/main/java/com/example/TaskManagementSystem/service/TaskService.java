package com.example.TaskManagementSystem.service;

import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public Task createTask(String title, String description, String status){
        Task task = new Task(title, description, status);
        return taskRepository.save(task);
    }
}
