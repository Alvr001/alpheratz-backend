package com.alpheratz.group;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.alpheratz.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "groups")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // El usuario que creó el grupo
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // Miembros del grupo
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "group_photo", columnDefinition = "TEXT")
    private String groupPhoto;

    // Por defecto todos en true (igual que WhatsApp)
    @Column(nullable = false)
    private boolean canMembersEditInfo = true;

    @Column(nullable = false)
    private boolean canMembersSendMessages = true;

    @Column(nullable = false)
    private boolean canMembersAddMembers = true;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}