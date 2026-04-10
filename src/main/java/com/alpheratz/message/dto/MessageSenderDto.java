package com.alpheratz.message.dto;

public record MessageSenderDto(
    Long id,
    String name,
    String animalId,
    String avatarUrl
    // 🔥 sin profilePhoto, sin password, sin nada pesado
) {}