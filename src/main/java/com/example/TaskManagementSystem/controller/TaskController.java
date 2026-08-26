package com.example.TaskManagementSystem.controller;


import com.example.TaskManagementSystem.dto.TaskRequest;
import com.example.TaskManagementSystem.dto.TaskResponse;
import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.mapper.TaskMapper;
import com.example.TaskManagementSystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController  {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskController (TaskService taskService, TaskMapper taskMapper){
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }
    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request){
        Task task = taskService.createTask(request);
        return taskMapper.toResponse(task);
    }

    /*
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return  taskService.createTask(task.getTitle(), task.getDescription(), task.getStatus());
    }
*/
   /* @GetMapping
    public List<Task> getAllTask(){
        return taskService.getAllTask();
    }
*/
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) throws ReflectiveOperationException {
        return taskService.getTaskById(id)
                .orElseThrow(()-> new RuntimeException("Task Not Found"));
    }

    @GetMapping
    public List<Task> getTask(@RequestParam(required = false)Long userId) {
        if (userId != null){
            return taskService.findByUserId(userId);
        }
        return taskService.getAllTask();
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