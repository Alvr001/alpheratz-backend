package com.alpheratz.auth;

import com.alpheratz.auth.dto.AuthResponse;
import com.alpheratz.auth.dto.LoginRequest;
import com.alpheratz.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "EMAIL_ALREADY_EXISTS" ->
                    ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
                default ->
                    ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar"));
            };
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "USER_NOT_FOUND"   -> ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
                case "INVALID_PASSWORD" -> ResponseEntity.badRequest().body(Map.of("error", "Contraseña incorrecta"));
                default                 -> ResponseEntity.internalServerError().body(Map.of("error", "Error al iniciar sesión"));
            };
        }
    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authHeader) {
    try {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(token);
        authService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Cuenta eliminada"));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Error al eliminar cuenta"));
    }
    }
}