package com.example.TaskManagementSystem.controller;


import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @GetMapping
    public List<Task> getAllTask(){
        return taskService.getAllTask();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) throws ReflectiveOperationException {
        return taskService.getTaskById(id)
                .orElseThrow(()-> new RuntimeException("Task Not Found"));
    }

    @DeleteMapping("/{id}")
    public void  deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }

    @PostMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task updateTask) {
        return taskService.updateTask(id, updateTask.getTitle(),updateTask.getDescription(),updateTask.getStatus());
    }

}