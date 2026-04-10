package com.alpheratz.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    List<Message> findByGroupIdAndType(Long groupId, Message.MessageType type);

    List<Message> findByGroupIdAndCreatedAtAfterOrderByCreatedAtAsc(Long groupId, LocalDateTime after);

    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId AND m.type = 'IMAGE' ORDER BY m.createdAt DESC")
    List<Message> findImagesByGroupId(@Param("groupId") Long groupId);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.sender = null WHERE m.sender.id = :userId")
    void nullifySender(@Param("userId") Long userId);

    Optional<Message> findTopByGroupIdOrderByCreatedAtDesc(Long groupId);

    List<Message> findByGroupIdAndCreatedAtBeforeOrderByCreatedAtAsc(
        Long groupId, LocalDateTime before);

    void deleteByGroupId(Long groupId);
}