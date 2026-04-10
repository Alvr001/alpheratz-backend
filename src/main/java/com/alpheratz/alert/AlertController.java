package com.alpheratz.alert;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.alpheratz.alert.dto.CreateAlertDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;

    // userId opcional para respetar chat_clears
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Alert>> getAlertsByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(alertService.getAlertsByGroup(groupId, userId));
    }

    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<List<Alert>> getActiveAlerts(@PathVariable Long groupId) {
        return ResponseEntity.ok(alertService.getActiveAlerts(groupId));
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody CreateAlertDto dto) {
        Alert alert = alertService.createAlert(dto);
        // ── Broadcast por WS para que ChatPage y App.js lo reciban en tiempo real ──
        messagingTemplate.convertAndSend("/topic/alert/" + dto.groupId(), alert);
        return ResponseEntity.ok(alert);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        Alert alert = alertService.resolveAlert(id);
        return ResponseEntity.ok(alert);
    }

    @PutMapping("/{id}/false-alarm")
    public ResponseEntity<Alert> markAsFalseAlarm(@PathVariable Long id) {
        Alert alert = alertService.markAsFalseAlarm(id);
        return ResponseEntity.ok(alert);
    }
}