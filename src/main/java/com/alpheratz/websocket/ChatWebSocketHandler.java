package com.alpheratz.websocket;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alpheratz.group.GroupRepository;
import com.alpheratz.message.Message;
import com.alpheratz.message.MessageService;
import com.alpheratz.message.dto.MessageResponseDto;
import com.alpheratz.message.dto.MessageSenderDto;
import com.alpheratz.message.dto.SendMessageDto;
import com.alpheratz.notification.FCMService;
import com.alpheratz.user.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final FCMService fcmService;
    private final GroupRepository groupRepository;

    @MessageMapping("/chat/{groupId}")
    public void sendMessage(@DestinationVariable Long groupId, SendMessageDto dto) {
        System.out.println("📨 DTO recibido: content=" + dto.content()
                + " type=" + dto.type()
                + " senderId=" + dto.senderId()
                + " groupId=" + dto.groupId());
        try {
            Message saved = messageService.sendMessage(dto);

            MessageSenderDto senderDto = saved.getSender() == null ? null :
                new MessageSenderDto(
                    saved.getSender().getId(),
                    saved.getSender().getName(),
                    saved.getSender().getAnimalId(),
                    saved.getSender().getAvatarUrl()
                );

            MessageResponseDto response = new MessageResponseDto(
                saved.getId(),
                saved.getContent(),
                saved.getType(),
                senderDto,
                saved.getGroup().getId(),
                saved.getCreatedAt()
            );

            messagingTemplate.convertAndSend("/topic/chat/" + groupId, response);

            // 🔔 FCM — notificar a miembros excepto al sender
            String senderName = saved.getSender() != null ? saved.getSender().getName() : "Alguien";
            String groupName = saved.getGroup().getName();
            String body = "IMAGE".equals(saved.getType().name()) ? "📷 Imagen" : saved.getContent();

            List<String> tokens = groupRepository.findByIdWithMembers(groupId)
                .map(g -> g.getMembers().stream()
                    .filter(m -> !m.getId().equals(dto.senderId()))
                    .map(User::getFcmToken)
                    .filter(t -> t != null && !t.isBlank())
                    .toList())
                .orElse(List.of());

            fcmService.sendToMultiple(tokens, senderName + " en " + groupName, body);

            System.out.println("✅ Mensaje enviado: tipo=" + saved.getType() + " id=" + saved.getId());
        } catch (Exception e) {
            System.err.println("❌ Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}