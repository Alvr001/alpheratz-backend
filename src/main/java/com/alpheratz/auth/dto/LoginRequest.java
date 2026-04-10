package com.alpheratz.auth.dto;

public record LoginRequest(
    String email,
    String password
) {}