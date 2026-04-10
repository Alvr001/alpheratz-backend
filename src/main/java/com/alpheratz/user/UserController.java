package com.alpheratz.user;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/users eliminado — el registro ahora lo maneja AuthController

    @PutMapping("/{id}/name")
    public ResponseEntity<User> updateName(
            @PathVariable Long id,
            @RequestBody UpdateNameRequest request) {
        User updated = userService.updateName(id, request.name());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/photo")
    public ResponseEntity<User> updatePhoto(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.updatePhoto(id, body.get("profilePhoto"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        userService.updateFcmToken(id, body.get("fcmToken"));
        return ResponseEntity.ok().build();
    }

    record UpdateNameRequest(String name) {}
}