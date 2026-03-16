package com.alpheratz.alert;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpheratz.alert.dto.CreateAlertDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // GET /api/alerts/group/{groupId}
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Alert>> getAlertsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(alertService.getAlertsByGroup(groupId));
    }

    // GET /api/alerts/group/{groupId}/active
    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<List<Alert>> getActiveAlerts(@PathVariable Long groupId) {
        return ResponseEntity.ok(alertService.getActiveAlerts(groupId));
    }

    // POST /api/alerts
    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody CreateAlertDto dto) {
        Alert alert = alertService.createAlert(dto);
        return ResponseEntity.ok(alert);
    }

    // PUT /api/alerts/{id}/resolve
    @PutMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        Alert alert = alertService.resolveAlert(id);
        return ResponseEntity.ok(alert);
    }

    // PUT /api/alerts/{id}/false-alarm
    @PutMapping("/{id}/false-alarm")
    public ResponseEntity<Alert> markAsFalseAlarm(@PathVariable Long id) {
        Alert alert = alertService.markAsFalseAlarm(id);
        return ResponseEntity.ok(alert);
    }
}