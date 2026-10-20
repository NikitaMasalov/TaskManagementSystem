package com.example.TaskManagementSystem.controller;

import com.example.TaskManagementSystem.dto.TaskRequest;
import com.example.TaskManagementSystem.dto.TaskResponse;
import com.example.TaskManagementSystem.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {
    private final TaskService tasks;
    public TaskController(TaskService tasks) { this.tasks = tasks; }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) @Positive Long userId) {
        return tasks.findAll(userId);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable @Positive Long id) { return tasks.findById(id); }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = tasks.create(request);
        return ResponseEntity.created(URI.create("/task/" + response.id())).body(response);
    }

    @RequestMapping(path = "/{id}", method = {RequestMethod.PUT, RequestMethod.POST})
    public TaskResponse update(@PathVariable @Positive Long id, @Valid @RequestBody TaskRequest request) {
        return tasks.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        tasks.delete(id);
        return ResponseEntity.noContent().build();
    }
}
