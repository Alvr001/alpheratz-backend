package com.alpheratz.group;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpheratz.group.dto.CreateGroupDto;
import com.alpheratz.group.dto.JoinGroupDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // GET /api/groups
    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupService.findAll());
    }

    // GET /api/groups/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/groups/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Group>> getGroupsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(groupService.getGroupsByUser(userId));
    }

    // POST /api/groups
    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupDto dto) {
        Group created = groupService.createGroup(dto);
        return ResponseEntity.ok(created);
    }

    // POST /api/groups/{id}/join
    @PostMapping("/{id}/join")
    public ResponseEntity<Group> joinGroup(
            @PathVariable Long id,
            @RequestBody JoinGroupDto dto) {
        Group updated = groupService.joinGroup(id, dto.userId());
        return ResponseEntity.ok(updated);
    }
}