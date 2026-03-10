package com.leonidas.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Todos los mensajes de un grupo ordenados por fecha
    List<Message> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    // Mensajes de un grupo por tipo
    List<Message> findByGroupIdAndType(Long groupId, Message.MessageType type);
}