package com.alpheratz.group;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GroupMemberExitRepository extends JpaRepository<GroupMemberExit, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    // findTop = trae solo el registro más reciente
    // Evita NonUniqueResultException cuando el usuario entró/salió varias veces
    Optional<GroupMemberExit> findTopByGroupIdAndUserIdOrderByExitedAtDesc(Long groupId, Long userId);

    // Alias usado en MessageService y AlertService
    default Optional<GroupMemberExit> findByGroupIdAndUserId(Long groupId, Long userId) {
        return findTopByGroupIdAndUserIdOrderByExitedAtDesc(groupId, userId);
    }

    // Fix Bug 2: limpiar TODOS los registros de salida al re-unirse
    @Modifying
    @Transactional
    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    // Usado en GroupService.deleteGroupData()
    @Modifying
    @Transactional
    @Query("DELETE FROM GroupMemberExit e WHERE e.group.id = :groupId")
    void deleteByGroupIdCustom(@Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM GroupMemberExit e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}