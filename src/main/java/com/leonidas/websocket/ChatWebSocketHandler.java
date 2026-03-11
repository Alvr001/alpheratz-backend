package com.leonidas.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.leonidas.message.Message;
import com.leonidas.message.MessageService;
import com.leonidas.message.dto.SendMessageDto;

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