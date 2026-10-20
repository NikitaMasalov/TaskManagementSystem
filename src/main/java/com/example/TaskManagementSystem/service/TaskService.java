package com.example.TaskManagementSystem.service;

import com.example.TaskManagementSystem.dto.TaskRequest;
import com.example.TaskManagementSystem.dto.TaskResponse;
import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.entity.User;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.mapper.TaskMapper;
import com.example.TaskManagementSystem.repository.TaskRepository;
import com.example.TaskManagementSystem.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;
    private final TaskMapper mapper;

    public TaskService(TaskRepository tasks, UserRepository users, TaskMapper mapper) {
        this.tasks = tasks;
        this.users = users;
        this.mapper = mapper;
    }

    public List<TaskResponse> findAll(Long userId) {
        if (userId != null) findUser(userId);
        List<Task> result = userId == null ? tasks.findAll(Sort.by("id")) : tasks.findByUser_IdOrderByIdAsc(userId);
        return result.stream().map(mapper::toResponse).toList();
    }

    public TaskResponse findById(Long id) { return mapper.toResponse(findTask(id)); }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        apply(task, request);
        return mapper.toResponse(tasks.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTask(id);
        apply(task, request);
        return mapper.toResponse(task);
    }

    @Transactional
    public void delete(Long id) { tasks.delete(findTask(id)); }

    private Task findTask(Long id) {
        return tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private User findUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void apply(Task task, TaskRequest request) {
        User user = request.userId() == null ? null : findUser(request.userId());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setUser(user);
    }
}
