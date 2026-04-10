package com.alpheratz.message;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpheratz.message.dto.MessageResponseDto;
import com.alpheratz.message.dto.MessageSenderDto;
import com.alpheratz.message.dto.SendMessageDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Message>> getMessagesByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(messageService.getMessagesByGroup(groupId, userId));
    }

    @GetMapping("/group/{groupId}/images")
    public ResponseEntity<List<Message>> getImagesByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(messageService.getImagesByGroup(groupId));
    }

    // userId opcional para respetar chat_clears y ex-miembros
    @GetMapping("/group/{groupId}/alerts")
    public ResponseEntity<List<Message>> getAlertsByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(messageService.getAlertsByGroup(groupId, userId));
    }

    @GetMapping("/{messageId}/content")
    public ResponseEntity<String> getMessageContent(@PathVariable Long messageId) {
        return messageService.getMessageContent(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
public ResponseEntity<Message> sendMessage(@RequestBody SendMessageDto dto) {
    Message message = messageService.sendMessage(dto);

    // 🔥 Mapear sender a DTO liviano
    MessageSenderDto senderDto = message.getSender() == null ? null :
        new MessageSenderDto(
            message.getSender().getId(),
            message.getSender().getName(),
            message.getSender().getAnimalId(),
            message.getSender().getAvatarUrl()
        );

    // 🔥 Crear respuesta liviana
    MessageResponseDto response = new MessageResponseDto(
        message.getId(),
        message.getContent(),
        message.getType(),
        senderDto,
        message.getGroup().getId(),
        message.getCreatedAt()
    );

    // 🔥 Enviar DTO (NO entidad)
    messagingTemplate.convertAndSend("/topic/chat/" + dto.groupId(), response);

    // REST sigue devolviendo la entidad completa al que envió
    return ResponseEntity.ok(message);
}

    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<Void> clearChat(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        messageService.clearChat(groupId, userId);
        return ResponseEntity.ok().build();
    }
}