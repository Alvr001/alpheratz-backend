package com.alpheratz.message.dto;

import com.alpheratz.message.Message.MessageType;

public record SendMessageDto(
    String content,
    MessageType type,
    Long senderId,
    Long groupId
) {}