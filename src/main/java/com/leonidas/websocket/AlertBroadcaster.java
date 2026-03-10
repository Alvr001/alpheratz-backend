package com.leonidas.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.leonidas.alert.Alert;
import com.leonidas.alert.AlertService;
import com.leonidas.alert.dto.CreateAlertDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AlertBroadcaster {

    private final AlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;

    // Cliente envía a: /app/alert/{groupId}
    // Todos en el grupo reciben en: /topic/alert/{groupId}
    @MessageMapping("/alert/{groupId}")
    @SendTo("/topic/alert/{groupId}")
    public Alert sendAlert(
            @DestinationVariable Long groupId,
            CreateAlertDto dto) {
        return alertService.createAlert(dto);
    }

    // Método para broadcast manual desde cualquier parte del backend
    // Por ejemplo cuando se resuelve una alerta
    public void broadcastAlert(Long groupId, Alert alert) {
        messagingTemplate.convertAndSend(
            "/topic/alert/" + groupId,
            alert
        );
    }
}
