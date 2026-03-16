package com.alpheratz.alert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Alertas de un grupo ordenadas por fecha
    List<Alert> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    // Solo alertas activas de un grupo
    List<Alert> findByGroupIdAndStatus(Long groupId, Alert.AlertStatus status);

    // Alertas por nivel
    List<Alert> findByGroupIdAndLevel(Long groupId, Alert.AlertLevel level);
}