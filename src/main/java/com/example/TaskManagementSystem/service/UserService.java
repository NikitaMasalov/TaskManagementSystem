package com.example.TaskManagementSystem.service;

import com.example.TaskManagementSystem.dto.UserRequest;
import com.example.TaskManagementSystem.dto.UserResponse;
import com.example.TaskManagementSystem.entity.User;
import com.example.TaskManagementSystem.exception.ResourceConflictException;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.repository.TaskRepository;
import com.example.TaskManagementSystem.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final TaskRepository tasks;
    private final PasswordEncoder passwords = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public UserService(UserRepository users, TaskRepository tasks) {
        this.users = users;
        this.tasks = tasks;
    }

    public List<UserResponse> findAll() {
        return users.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    public UserResponse findById(Long id) { return toResponse(findUser(id)); }

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = new User();
        apply(user, request);
        return toResponse(users.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findUser(id);
        apply(user, request);
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = findUser(id);
        if (tasks.existsByUser_Id(id)) {
            throw new ResourceConflictException("Reassign, unassign or delete this user's tasks before deleting the user");
        }
        users.delete(user);
    }

    private User findUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void apply(User user, UserRequest request) {
        user.setUsername(request.username());
        user.setPhone(request.phone());
        user.setPassword(passwords.encode(request.password()));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getPhone());
    }
}
