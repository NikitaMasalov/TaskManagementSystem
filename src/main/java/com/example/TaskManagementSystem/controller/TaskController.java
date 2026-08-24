package com.example.TaskManagementSystem.controller;


import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController  {
    private final TaskService taskService;

    @Autowired
    public TaskController (TaskService taskService){
        this.taskService = taskService;

    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return  taskService.createTask(task.getTitle(), task.getDescription(), task.getStatus());
    }


}