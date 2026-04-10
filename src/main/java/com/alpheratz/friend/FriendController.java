package com.alpheratz.friend;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // GET /api/friends/search?animalId=fox@5334&requesterId=1
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String animalId,
            @RequestParam Long requesterId) {
        return ResponseEntity.ok(friendService.searchByAnimalId(animalId, requesterId));
    }

    // POST /api/friends/request  { senderId, receiverId }
    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, Long> body) {
        try {
            friendService.sendRequest(body.get("senderId"), body.get("receiverId"));
            return ResponseEntity.ok(Map.of("message", "Solicitud enviada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/friends/request/{id}/accept
    @PutMapping("/request/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        friendService.acceptRequest(id);
        return ResponseEntity.ok(Map.of("message", "Solicitud aceptada"));
    }

    // DELETE /api/friends/request/{id}/reject
    @DeleteMapping("/request/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        friendService.rejectRequest(id);
        return ResponseEntity.ok().build();
    }

    // GET /api/friends/requests?userId=1
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests(@RequestParam Long userId) {
        return ResponseEntity.ok(friendService.getPendingRequests(userId));
    }

    // GET /api/friends?userId=1
    @GetMapping
    public ResponseEntity<?> getFriends(@RequestParam Long userId) {
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    // DELETE /api/friends?userId=1&friendId=2
    @DeleteMapping
    public ResponseEntity<?> deleteFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        try {
            friendService.deleteFriend(userId, friendId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}