package com.alpheratz.friend;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // Solicitudes recibidas pendientes
    List<Friend> findByReceiverIdAndStatus(Long receiverId, Friend.FriendStatus status);

    // Verificar si ya existe relación (en cualquier dirección)
    @Query("""
        SELECT f FROM Friend f
        WHERE (f.sender.id = :a AND f.receiver.id = :b)
           OR (f.sender.id = :b AND f.receiver.id = :a)
    """)
    Optional<Friend> findRelation(@Param("a") Long a, @Param("b") Long b);

    // Lista de amigos aceptados
    @Query("""
        SELECT f FROM Friend f
        WHERE f.status = 'ACCEPTED'
          AND (f.sender.id = :userId OR f.receiver.id = :userId)
    """)
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);
    @Modifying
    @Transactional
    void deleteBySenderIdOrReceiverId(Long senderId, Long receiverId);
}