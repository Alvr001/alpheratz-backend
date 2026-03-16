package com.alpheratz.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alpheratz.alert.Alert;
import com.alpheratz.alert.AlertService;
import com.alpheratz.alert.dto.CreateAlertDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AlertBroadcaster {

    private final AlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/alert/{groupId}")
    public void sendAlert(
            @DestinationVariable Long groupId,
            CreateAlertDto dto) {
        Alert saved = alertService.createAlert(dto);
        messagingTemplate.convertAndSend("/topic/alert/" + groupId, saved);
    }
}