package com.alpheratz.group;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alpheratz.group.dto.CreateGroupDto;
import com.alpheratz.user.User;
import com.alpheratz.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public Group createGroup(CreateGroupDto dto) {
        User admin = userRepository.findById(dto.adminId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Group group = new Group();
        group.setName(dto.name());
        group.setDescription(dto.description());
        group.setAdmin(admin);
        group.getMembers().add(admin); // El admin entra automáticamente

        return groupRepository.save(group);
    }

    public Group joinGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean yaEsMiembro = group.getMembers()
                .stream()
                .anyMatch(m -> m.getId().equals(userId));

        if (yaEsMiembro) {
            throw new RuntimeException("El usuario ya es miembro del grupo");
        }

        group.getMembers().add(user);
        return groupRepository.save(group);
    }

    public List<Group> getGroupsByUser(Long userId) {
        return groupRepository.findGroupsByMemberId(userId);
    }

    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }
}