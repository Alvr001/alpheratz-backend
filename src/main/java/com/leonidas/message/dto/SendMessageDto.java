package com.leonidas.message.dto;

import com.leonidas.message.Message.MessageType;

public record SendMessageDto(
    String content,
    MessageType type,
    Long senderId,
    Long groupId
) {}