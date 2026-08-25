package com.example.TaskManagementSystem.repository;

import com.example.TaskManagementSystem.entity.Task;
import com.example.TaskManagementSystem.service.TaskService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser_Id(Long userId);
}
