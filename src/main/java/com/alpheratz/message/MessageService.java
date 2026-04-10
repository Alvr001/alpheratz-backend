package com.alpheratz.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alpheratz.alert.AlertRepository;
import com.alpheratz.group.Group;
import com.alpheratz.group.GroupMemberExitRepository;
import com.alpheratz.group.GroupRepository;
import com.alpheratz.message.dto.SendMessageDto;
import com.alpheratz.user.User;
import com.alpheratz.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AlertRepository alertRepository;
    private final GroupMemberExitRepository exitRepository;
    private final ChatClearRepository chatClearRepository;

    private static final List<String> INTERNAL_PREFIXES = List.of(
        "__CHAT_CLEARED__",
        "__GROUP_UPDATED__",
        "__PERMISSIONS_UPDATED__"
    );

    private boolean isInternalSystemMessage(Message m) {
        if (m.getType() != Message.MessageType.SYSTEM) return false;
        return INTERNAL_PREFIXES.stream()
            .anyMatch(prefix -> m.getContent() != null && m.getContent().startsWith(prefix));
    }

    private Message stripImageContent(Message m) {
        if (m.getType() == Message.MessageType.IMAGE) {
            Message copy = new Message();
            copy.setId(m.getId());
            copy.setType(m.getType());
            copy.setContent("__IMAGE__");
            copy.setSender(m.getSender());
            copy.setGroup(m.getGroup());
            copy.setCreatedAt(m.getCreatedAt());
            return copy;
        }
        return m;
    }

    @Transactional
    public Message sendMessage(SendMessageDto dto) {
        User sender = null;
        if (dto.senderId() != null) {
            sender = userRepository.findById(dto.senderId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        Group group = groupRepository.findById(dto.groupId())
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (sender != null) {
            boolean esMiembro = groupRepository.existsMember(dto.groupId(), dto.senderId());
            if (!esMiembro) {
                throw new RuntimeException("El usuario no es miembro de este grupo");
            }
        }

        Message message = new Message();
        message.setContent(dto.content());
        message.setType(dto.type());
        message.setSender(sender);
        message.setGroup(group);

        if (dto.type() == Message.MessageType.SYSTEM &&
            dto.content() != null &&
            dto.content().startsWith("__")) {
            message.setCreatedAt(LocalDateTime.now());
            return message;
        }

        return messageRepository.save(message);
    }

    public List<Message> getMessagesByGroup(Long groupId, Long userId) {
        if (userId != null) {
            Optional<com.alpheratz.group.GroupMemberExit> exit =
                exitRepository.findByGroupIdAndUserId(groupId, userId);

            if (exit.isPresent()) {
                return messageRepository
                    .findByGroupIdAndCreatedAtBeforeOrderByCreatedAtAsc(
                        groupId, exit.get().getExitedAt())
                    .stream()
                    .filter(m -> !isInternalSystemMessage(m))
                    .map(this::stripImageContent)
                    .collect(Collectors.toList());
            }

            Optional<ChatClear> chatClear =
                chatClearRepository.findByGroupIdAndUserId(groupId, userId);

            if (chatClear.isPresent()) {
                LocalDateTime clearedAt = chatClear.get().getClearedAt();
                return messageRepository
                    .findByGroupIdOrderByCreatedAtAsc(groupId)
                    .stream()
                    .filter(m -> !isInternalSystemMessage(m))
                    .filter(m -> m.getType() == Message.MessageType.ALERT
                                 || m.getCreatedAt().isAfter(clearedAt))
                    .map(this::stripImageContent)
                    .collect(Collectors.toList());
            }
        }

        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId)
            .stream()
            .filter(m -> !isInternalSystemMessage(m))
            .map(this::stripImageContent)
            .collect(Collectors.toList());
    }

    public List<Message> getAlertsByGroup(Long groupId) {
        return messageRepository.findByGroupIdAndType(groupId, Message.MessageType.ALERT);
    }
    // Método correcto con userId (usar en /alerts)
public List<Message> getAlertsByGroup(Long groupId, Long userId) {
    List<Message> alerts = messageRepository.findByGroupIdAndType(groupId, Message.MessageType.ALERT);

    if (userId != null) {
        // Respeta chat_clears
        Optional<ChatClear> chatClear = chatClearRepository.findByGroupIdAndUserId(groupId, userId);
        if (chatClear.isPresent()) {
            LocalDateTime clearedAt = chatClear.get().getClearedAt();
            alerts = alerts.stream()
                    .filter(a -> a.getCreatedAt().isAfter(clearedAt))
                    .collect(Collectors.toList());
        }

        // Respeta salida del grupo
        Optional<com.alpheratz.group.GroupMemberExit> exit =
                exitRepository.findByGroupIdAndUserId(groupId, userId);
        if (exit.isPresent()) {
            LocalDateTime exitedAt = exit.get().getExitedAt();
            alerts = alerts.stream()
                    .filter(a -> a.getCreatedAt().isBefore(exitedAt))
                    .collect(Collectors.toList());
        }
    }

    return alerts;
}

    public List<Message> getImagesByGroup(Long groupId) {
        return messageRepository.findImagesByGroupId(groupId);
    }

    @Transactional
    public void clearChat(Long groupId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        ChatClear clear = chatClearRepository
            .findByGroupIdAndUserId(groupId, userId)
            .orElse(new ChatClear());

        clear.setUser(user);
        clear.setGroup(group);
        clear.setClearedAt(LocalDateTime.now());
        chatClearRepository.save(clear);
    }
    // En MessageService.java — agregar este método
    public Optional<String> getMessageContent(Long messageId) {
        return messageRepository.findById(messageId)
                .map(Message::getContent);
    }
    public Optional<Message> getLastMessageForUser(Long groupId, Long userId) {
    // Si es ex-miembro
    Optional<com.alpheratz.group.GroupMemberExit> exit =
            exitRepository.findByGroupIdAndUserId(groupId, userId);
    if (exit.isPresent()) {
        return messageRepository
                .findByGroupIdAndCreatedAtBeforeOrderByCreatedAtAsc(groupId, exit.get().getExitedAt())
                .stream()
                .filter(m -> !isInternalSystemMessage(m))
                .reduce((first, second) -> second);
    }

    // Si limpió chat
    Optional<ChatClear> chatClear =
            chatClearRepository.findByGroupIdAndUserId(groupId, userId);
    if (chatClear.isPresent()) {
        LocalDateTime clearedAt = chatClear.get().getClearedAt();
        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId)
                .stream()
                .filter(m -> !isInternalSystemMessage(m))
                .filter(m -> m.getCreatedAt().isAfter(clearedAt))
                .reduce((first, second) -> second);
    }

    // Normal
    return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId)
            .stream()
            .filter(m -> !isInternalSystemMessage(m))
            .reduce((first, second) -> second);
    }
}