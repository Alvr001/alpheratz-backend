package com.alpheratz.alert;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.alpheratz.alert.dto.CreateAlertDto;
import com.alpheratz.group.Group;
import com.alpheratz.group.GroupMemberExitRepository;
import com.alpheratz.group.GroupRepository;
import com.alpheratz.message.ChatClear;
import com.alpheratz.message.ChatClearRepository;
import com.alpheratz.notification.FCMService;
import com.alpheratz.user.User;
import com.alpheratz.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final FCMService fcmService;
    private final AlertRepository alertRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMemberExitRepository exitRepository;
    private final ChatClearRepository chatClearRepository;

    // ── Crear alerta desde DTO ────────────────────────────────────────────
    public Alert createAlert(CreateAlertDto dto) {
        Group group = groupRepository.findByIdWithMembers(dto.groupId())
        .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        User reporter = userRepository.findById(dto.reporterId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Alert alert = new Alert();
        alert.setGroup(group);
        alert.setReporter(reporter);
        alert.setLevel(dto.level());
        alert.setType(dto.type());
        alert.setDescription(dto.description());
        alert.setCreatedAt(LocalDateTime.now());
        alert.setStatus(Alert.AlertStatus.ACTIVE);

        Alert saved = alertRepository.save(alert);

        // El AlertController también hace el broadcast, pero lo dejamos aquí
        // para cuando se llame desde AlertBroadcaster (STOMP directo)
        messagingTemplate.convertAndSend("/topic/alert/" + dto.groupId(), saved);
        String reporterName = reporter.getName();
String nivel = dto.level().name(); // RED, ORANGE, etc.
String titulo = "🚨 Alerta " + nivel + " en " + group.getName();
String cuerpo = dto.description() != null ? dto.description() : "Nueva alerta de emergencia";

List<String> tokens = group.getMembers().stream()
    .filter(m -> !m.getId().equals(dto.reporterId()))
    .map(User::getFcmToken)
    .filter(t -> t != null && !t.isBlank())
    .toList();
if (dto.level().name().equals("RED")) {
    fcmService.sendAlertNotification(tokens, titulo, cuerpo);
} else {
    fcmService.sendToMultiple(tokens, titulo, cuerpo);
}

        return saved;
    }

    // ── Alertas activas de un grupo (sin filtrar) ─────────────────────────
    public List<Alert> getActiveAlerts(Long groupId) {
        return alertRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    // ── Alertas respetando chat_clears y salidas ──────────────────────────
    public List<Alert> getAlertsByGroup(Long groupId, Long userId) {
        if (userId == null) {
            return alertRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        }

        // Fecha de la última limpieza (usa el método que SÍ existe en el repo)
        LocalDateTime clearDate = chatClearRepository
                .findByGroupIdAndUserId(groupId, userId)
                .map(ChatClear::getClearedAt)
                .orElse(null);

        // Fecha en que el usuario salió
        LocalDateTime exitDate = exitRepository
                .findByGroupIdAndUserId(groupId, userId)
                .map(e -> e.getExitedAt())
                .orElse(null);

        return alertRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream()
                .filter(a -> {
                    LocalDateTime at = a.getCreatedAt();
                    if (clearDate != null && at.isBefore(clearDate)) return false;
                    if (exitDate  != null && at.isAfter(exitDate))   return false;
                    return true;
                })
                .toList();
    }

    // ── Resolver alerta ───────────────────────────────────────────────────
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alert.setStatus(Alert.AlertStatus.RESOLVED);
        return alertRepository.save(alert);
    }

    // ── Falsa alarma ──────────────────────────────────────────────────────
    public Alert markAsFalseAlarm(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alert.setStatus(Alert.AlertStatus.FALSE_ALARM);
        return alertRepository.save(alert);
    }
}