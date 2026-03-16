package com.alpheratz.alert;

import java.time.LocalDateTime;

import com.alpheratz.group.Group;
import com.alpheratz.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "alerts")
@Data
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false)
    private String description;

    // Ubicación opcional
    private Double latitude;
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = AlertStatus.ACTIVE;
    }

    public enum AlertLevel {
        RED,    // Emergencia — robo, violencia
        YELLOW, // Sospechoso — persona merodeando
        GREEN   // Zona despejada — todo tranquilo
    }

    public enum AlertType {
        ROBBERY,        // Robo
        SUSPICIOUS,     // Persona sospechosa
        EMERGENCY,      // Emergencia general
        CLEARED         // Zona despejada
    }

    public enum AlertStatus {
        ACTIVE,     // Alerta activa
        RESOLVED,   // Resuelta
        FALSE_ALARM // Falsa alarma
    }
}