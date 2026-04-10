package com.alpheratz.group;

import java.util.List;

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

import com.alpheratz.group.dto.CreateGroupDto;
import com.alpheratz.group.dto.JoinGroupDto;
import com.alpheratz.message.Message;
import com.alpheratz.message.MessageRepository;
import com.alpheratz.message.MessageService;
import com.alpheratz.user.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final MessageRepository messageRepository;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Group>> getGroupsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(groupService.getAllGroupsByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupDto dto) {
        Group created = groupService.createGroup(dto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Group> joinGroup(
            @PathVariable Long id,
            @RequestBody JoinGroupDto dto) {
        Group updated = groupService.joinGroup(id, dto.userId());
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/{groupId}/leave-and-delete")
    public ResponseEntity<?> leaveAndDelete(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        groupService.leaveAndDelete(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<User>> getMembers(@PathVariable Long id) {
        List<User> members = groupService.getMembers(id);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{groupId}/last-message")
    public ResponseEntity<?> getLastMessage(
        @PathVariable Long groupId,
        @RequestParam(required = false) Long userId) {

    return messageService.getLastMessageForUser(groupId, userId)
            .map(m -> {
                // Evitar mandar base64 en imágenes
                if (m.getType() == Message.MessageType.IMAGE) {
                    Message copy = new Message();
                    copy.setId(m.getId());
                    copy.setType(m.getType());
                    copy.setContent("__IMAGE__");
                    copy.setSender(m.getSender());
                    copy.setGroup(m.getGroup());
                    copy.setCreatedAt(m.getCreatedAt());
                    return ResponseEntity.ok(copy);
                }
                return ResponseEntity.ok(m);
            })
            .orElse(ResponseEntity.ok(null));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long id,
            @RequestParam Long userId) {
        groupService.leaveGroup(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/is-former-member")
    public ResponseEntity<Boolean> isFormerMember(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(groupService.isFormerMember(id, userId));
    }
    @PutMapping("/{id}/description")
    public ResponseEntity<Group> updateDescription(
        @PathVariable Long id,
        @RequestBody UpdateDescriptionRequest request) {
    return ResponseEntity.ok(groupService.updateDescription(id, request.description()));
    }
    @PutMapping("/{id}/name")
    public ResponseEntity<Group> updateName(
        @PathVariable Long id,
        @RequestBody UpdateNameRequest request) {
    return ResponseEntity.ok(groupService.updateName(id, request.name()));
    }

    record UpdateNameRequest(String name) {}
    @PutMapping("/{id}/photo")
    public ResponseEntity<Group> updatePhoto(
            @PathVariable Long id,
            @RequestBody UpdatePhotoRequest request) {
        return ResponseEntity.ok(groupService.updateGroupPhoto(id, request.groupPhoto()));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<Group> updatePermissions(
            @PathVariable Long id,
            @RequestBody UpdatePermissionsRequest request) {
        return ResponseEntity.ok(groupService.updatePermissions(
            id,
            request.canMembersEditInfo(),
            request.canMembersSendMessages(),
            request.canMembersAddMembers()
        ));
    }
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        groupService.removeMember(id, memberId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long userId) {
        groupService.deleteGroup(groupId, userId != null ? userId : -1L);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/members/{memberId}/make-admin")
    public ResponseEntity<Void> makeAdmin(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        groupService.makeAdmin(id, memberId);
        return ResponseEntity.ok().build();
    }

    record UpdatePermissionsRequest(
        boolean canMembersEditInfo,
        boolean canMembersSendMessages,
        boolean canMembersAddMembers
    ) {}
    record UpdatePhotoRequest(String groupPhoto) {}


    record UpdateDescriptionRequest(String description) {}
}