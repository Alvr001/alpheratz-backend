package com.alpheratz.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/users
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        if (userService.existsByPhone(request.phone())) {
            return ResponseEntity.badRequest().build();
        }
        User created = userService.createUser(request.phone(), request.name());
        return ResponseEntity.ok(created);
    }

    // PUT /api/users/{id}/name
    @PutMapping("/{id}/name")
    public ResponseEntity<User> updateName(
            @PathVariable Long id,
            @RequestBody UpdateNameRequest request) {
        User updated = userService.updateName(id, request.name());
        return ResponseEntity.ok(updated);
    }

    // Records internos para los request bodies
    record CreateUserRequest(String phone, String name) {}
    record UpdateNameRequest(String name) {}
}