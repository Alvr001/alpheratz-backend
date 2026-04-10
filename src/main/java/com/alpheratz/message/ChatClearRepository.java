package com.alpheratz.message;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ChatClearRepository extends JpaRepository<ChatClear, Long> {

    // findTop = trae solo 1 resultado (el más reciente)
    // Evita NonUniqueResultException si hay múltiples registros
    Optional<ChatClear> findTopByGroupIdAndUserIdOrderByClearedAtDesc(Long groupId, Long userId);

    // Alias usado en MessageService y AlertService
    default Optional<ChatClear> findByGroupIdAndUserId(Long groupId, Long userId) {
        return findTopByGroupIdAndUserIdOrderByClearedAtDesc(groupId, userId);
    }

    @Modifying
    @Transactional
    void deleteByGroupId(Long groupId);
}