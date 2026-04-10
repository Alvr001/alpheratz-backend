package com.alpheratz.message.dto;

import java.time.LocalDateTime;

import com.alpheratz.message.Message.MessageType;

public record MessageResponseDto(
    Long id,
    String content,
    MessageType type,
    MessageSenderDto sender,
    Long groupId,
    LocalDateTime createdAt
) {}