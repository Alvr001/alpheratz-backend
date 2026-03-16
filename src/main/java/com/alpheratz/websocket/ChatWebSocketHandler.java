package com.alpheratz.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alpheratz.message.Message;
import com.alpheratz.message.MessageService;
import com.alpheratz.message.dto.SendMessageDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{groupId}")
    public void sendMessage(
            @DestinationVariable Long groupId,
            SendMessageDto dto) {
        Message saved = messageService.sendMessage(dto);
        messagingTemplate.convertAndSend("/topic/chat/" + groupId, saved);
    }
}