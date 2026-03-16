package com.alpheratz.message;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpheratz.message.dto.SendMessageDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // GET /api/messages/group/{groupId}
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Message>> getMessagesByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(messageService.getMessagesByGroup(groupId));
    }

    // GET /api/messages/group/{groupId}/alerts
    @GetMapping("/group/{groupId}/alerts")
    public ResponseEntity<List<Message>> getAlertsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(messageService.getAlertsByGroup(groupId));
    }

    // POST /api/messages
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageDto dto) {
        Message message = messageService.sendMessage(dto);
        return ResponseEntity.ok(message);
    }
}