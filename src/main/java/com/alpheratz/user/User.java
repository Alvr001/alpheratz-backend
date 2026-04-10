package com.alpheratz.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(name = "animal_id", unique = true)
    private String animalId;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    private String name;

    private String avatarUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "profile_photo", columnDefinition = "TEXT")
    private String profilePhoto;

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}