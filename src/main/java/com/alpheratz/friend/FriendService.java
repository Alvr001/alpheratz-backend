package com.alpheratz.friend;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alpheratz.user.User;
import com.alpheratz.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    // Buscar usuario por animalId y devolver estado respecto al requester
    public Map<String, Object> searchByAnimalId(String animalId, Long requesterId) {
        Optional<User> found = userRepository.findByAnimalId(animalId);
        if (found.isEmpty() || found.get().getId().equals(requesterId)) {
            return Map.of("found", false);
        }
        User user = found.get();
        Optional<Friend> relation = friendRepository.findRelation(requesterId, user.getId());

        String status = "none";
        if (relation.isPresent()) {
            Friend f = relation.get();
            if (f.getStatus() == Friend.FriendStatus.ACCEPTED) {
                status = "friends";
            } else if (f.getSender().getId().equals(requesterId)) {
                status = "pending_sent";
            } else {
                status = "pending_received";
            }
        }

        return Map.of(
            "found", true,
            "user", Map.of(
                "id", user.getId(),
                "animalId", user.getAnimalId(),
                "name", user.getName() != null ? user.getName() : ""
            ),
            "status", status
        );
    }

    // Enviar solicitud
    public void sendRequest(Long senderId, Long receiverId) {
        if (friendRepository.findRelation(senderId, receiverId).isPresent()) {
            throw new RuntimeException("ALREADY_EXISTS");
        }
        User sender   = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        Friend f = new Friend();
        f.setSender(sender);
        f.setReceiver(receiver);
        f.setStatus(Friend.FriendStatus.PENDING);
        friendRepository.save(f);
    }

    // Aceptar solicitud
    public void acceptRequest(Long requestId) {
        Friend f = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));
        f.setStatus(Friend.FriendStatus.ACCEPTED);
        friendRepository.save(f);
    }

    // Rechazar / cancelar solicitud
    public void rejectRequest(Long requestId) {
        friendRepository.deleteById(requestId);
    }

    // Solicitudes recibidas pendientes
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        return friendRepository
            .findByReceiverIdAndStatus(userId, Friend.FriendStatus.PENDING)
            .stream()
            .map(f -> Map.<String, Object>of(
                "id",            f.getId(),
                "senderAnimalId", f.getSender().getAnimalId(),
                "sender", Map.of(
                    "id",       f.getSender().getId(),
                    "animalId", f.getSender().getAnimalId(),
                    "name",     f.getSender().getName() != null ? f.getSender().getName() : ""
                )
            ))
            .toList();
    }

    // Lista de amigos aceptados
    public List<Map<String, Object>> getFriends(Long userId) {
    return friendRepository.findAcceptedFriends(userId)
        .stream()
        .map(f -> {
            User other = f.getSender().getId().equals(userId)
                ? f.getReceiver()
                : f.getSender();
            return Map.<String, Object>of(
                "id",           other.getId(),
                "animalId",     other.getAnimalId(),
                "name",         other.getName() != null ? other.getName() : "",
                "profilePhoto", other.getProfilePhoto() != null ? other.getProfilePhoto() : ""
);
        })
        .toList();
    }

    // Eliminar amistad
    public void deleteFriend(Long userId, Long friendId) {
        Friend f = friendRepository.findRelation(userId, friendId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));
        friendRepository.delete(f);
    }
}