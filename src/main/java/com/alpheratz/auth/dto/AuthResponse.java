package com.alpheratz.auth.dto;

public record AuthResponse(
    String token,
    Long userId,
    String name,
    String animalId
) {}