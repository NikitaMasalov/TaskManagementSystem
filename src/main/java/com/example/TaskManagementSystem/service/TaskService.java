package com.example.TaskManagementSystem.service;

import com.example.TaskManagementSystem.dto.TaskRequest;
import com.example.TaskManagementSystem.dto.TaskResponse;
import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.mapper.TaskMapper;
import com.example.TaskManagementSystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper){
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    //POST Создание
 /*   public Task createTask(String title, String description, String status){
        Task task = new Task(title, description, status);
        return taskRepository.save(task);
    }
*/
    public Task createTask(TaskRequest request){
        Task task = taskMapper.fromCreateRequest(request);
        return taskRepository.save(task);
    }


    //GET - выводы
    public List<Task> getAllTask(){
        return taskRepository.findAll();
    }
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> findByUserId(Long userId){
        return taskRepository.findByUser_Id(userId);
    }

    public void deleteTask(Long id){
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, String title, String description, String status){
        Task task = taskRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Task Not Found"));
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        return taskRepository.save(task);
    }

}
