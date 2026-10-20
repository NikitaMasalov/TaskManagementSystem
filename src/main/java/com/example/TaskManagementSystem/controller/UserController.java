package com.example.TaskManagementSystem.controller;

import com.example.TaskManagementSystem.dto.UserRequest;
import com.example.TaskManagementSystem.dto.UserResponse;
import com.example.TaskManagementSystem.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }

    @GetMapping
    public List<UserResponse> list() { return users.findAll(); }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable @Positive Long id) { return users.findById(id); }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = users.create(request);
        return ResponseEntity.created(URI.create("/user/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable @Positive Long id, @Valid @RequestBody UserRequest request) {
        return users.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        users.delete(id);
        return ResponseEntity.noContent().build();
    }
}
