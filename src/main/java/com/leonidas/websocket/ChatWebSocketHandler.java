package com.leonidas.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.leonidas.message.Message;
import com.leonidas.message.MessageService;
import com.leonidas.message.dto.SendMessageDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final MessageService messageService;

    // Cliente envía a: /app/chat/{groupId}
    // Todos en el grupo reciben en: /topic/chat/{groupId}
    @MessageMapping("/chat/{groupId}")
    @SendTo("/topic/chat/{groupId}")
    public Message sendMessage(
            @DestinationVariable Long groupId,
            SendMessageDto dto) {
        return messageService.sendMessage(dto);
    }
}