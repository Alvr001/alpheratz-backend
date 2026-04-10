package com.alpheratz.group;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alpheratz.alert.AlertRepository;
import com.alpheratz.group.dto.CreateGroupDto;
import com.alpheratz.message.Message;
import com.alpheratz.message.MessageRepository;
import com.alpheratz.user.User;
import com.alpheratz.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberExitRepository exitRepository;
    private final MessageRepository messageRepository;
    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.alpheratz.message.ChatClearRepository chatClearRepository;

    @Autowired
    @Lazy
    private GroupService self;

    public Group createGroup(CreateGroupDto dto) {
        User admin = userRepository.findById(dto.adminId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Group group = new Group();
        group.setName(dto.name());
        group.setDescription(dto.description());
        group.setAdmin(admin);
        group.getMembers().add(admin);
        return groupRepository.save(group);
    }

    public Group joinGroup(Long groupId, Long userId) {
    Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    boolean yaEsMiembro = group.getMembers()
            .stream().anyMatch(m -> m.getId().equals(userId));

    if (yaEsMiembro) {
        throw new RuntimeException("El usuario ya es miembro del grupo");
    }

   group.getMembers().add(user);

    // ✅ FIX: si era ex-miembro, eliminar registro de salida
    exitRepository.deleteByGroupIdAndUserId(groupId, userId);

    // 🔥 NUEVO: notificar al usuario agregado
    messagingTemplate.convertAndSend(
        "/topic/user/" + userId,
        Map.of("type", "GROUP_ADDED", "groupId", groupId)
    );

    return groupRepository.save(group);
    }

    @Transactional
    public List<User> getMembers(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        return group.getMembers();
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean wasAdmin = group.getAdmin().getId().equals(userId);

        group.getMembers().removeIf(m -> m.getId().equals(userId));
        groupRepository.save(group);

        GroupMemberExit exit = new GroupMemberExit();
        exit.setGroup(group);
        exit.setUser(user);
        exit.setExitedAt(LocalDateTime.now());
        exitRepository.save(exit);

        if (wasAdmin) {
            assignNewAdmin(group, userId);
        }
    }

    // ── deleteGroup: broadcast PRIMERO (fuera de transacción), luego borra ──
    // Separar en dos métodos para garantizar que el WS llegue antes del delete
    public void deleteGroup(Long groupId, Long deletedByUserId) {
    // Incluir userId para que solo ese usuario navegue
    broadcastSystemMessage(groupId, "__GROUP_DELETED__:" + groupId + ":deletedBy:" + deletedByUserId);
    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    self.deleteGroupData(groupId);
}

    @Transactional
    public void deleteGroupData(Long groupId) {
        alertRepository.deleteByGroupId(groupId);
    chatClearRepository.deleteByGroupId(groupId);
    exitRepository.deleteByGroupIdCustom(groupId);
    messageRepository.deleteByGroupId(groupId);
    groupRepository.deleteById(groupId);
    }

    @Transactional
    public Group updateDescription(Long groupId, String description) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        group.setDescription(description);
        return groupRepository.save(group);
    }

    @Transactional
    public Group updateName(Long groupId, String name) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        group.setName(name);
        return groupRepository.save(group);
    }

    public Group updateGroupPhoto(Long id, String photoUrl) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.setGroupPhoto(photoUrl);
        return groupRepository.save(group);
    }

    @Transactional
    public Group updatePermissions(Long groupId, boolean canEditInfo, boolean canSendMessages, boolean canAddMembers) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        group.setCanMembersEditInfo(canEditInfo);
        group.setCanMembersSendMessages(canSendMessages);
        group.setCanMembersAddMembers(canAddMembers);
        return groupRepository.save(group);
    }

    @Transactional
    public void removeMember(Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        group.getMembers().remove(member);
        groupRepository.save(group);

        GroupMemberExit exit = new GroupMemberExit();
        exit.setGroup(group);
        exit.setUser(member);
        exit.setExitedAt(LocalDateTime.now());
        exitRepository.save(exit);

        sendSystemMessage(group, member.getName() + " fue eliminado del grupo");
    }

    @Transactional
    public void makeAdmin(Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        group.setAdmin(member);
        groupRepository.save(group);

        sendSystemMessage(group, member.getName() + " es ahora el administrador");
    }

    public List<Group> getGroupsByUser(Long userId) {
        return groupRepository.findGroupsByMemberId(userId);
    }

    public List<Group> getAllGroupsByUser(Long userId) {
        return groupRepository.findAllGroupsByUserId(userId);
    }

    public boolean isFormerMember(Long groupId, Long userId) {
        return exitRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    // ── helpers privados ──────────────────────────────────────────

    private void assignNewAdmin(Group group, Long excludeUserId) {
        group.getMembers().stream()
                .filter(m -> !m.getId().equals(excludeUserId))
                .findFirst()
                .ifPresent(newAdmin -> {
                    group.setAdmin(newAdmin);
                    groupRepository.save(group);
                    sendSystemMessage(group, newAdmin.getName() + " es ahora el administrador");
                });
    }

    private void sendSystemMessage(Group group, String content) {
        Message msg = new Message();
        msg.setContent(content);
        msg.setType(Message.MessageType.SYSTEM);
        msg.setGroup(group);
        msg.setSender(null);
        messageRepository.save(msg);
        messagingTemplate.convertAndSend("/topic/chat/" + group.getId(), msg);
    }

    // Broadcast sin persistir — para señales internas como __GROUP_DELETED__
    private void broadcastSystemMessage(Long groupId, String content) {
        Message msg = new Message();
        msg.setContent(content);
        msg.setType(Message.MessageType.SYSTEM);
        messagingTemplate.convertAndSend("/topic/chat/" + groupId, msg);
    }
    // ❌ SIN @Transactional
public void leaveAndDelete(Long groupId, Long userId) {
    self.leaveAndDeleteData(groupId, userId);

    // Solo avisar que ESTE usuario se fue y lo elimina de su lista
    broadcastSystemMessage(groupId, "__LEFT_AND_DELETED__:" + userId);
}

@Transactional
public void leaveAndDeleteData(Long groupId, Long userId) {
    Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    boolean wasAdmin = group.getAdmin().getId().equals(userId);

    group.getMembers().removeIf(m -> m.getId().equals(userId));
    groupRepository.save(group);

    GroupMemberExit exit = new GroupMemberExit();
    exit.setGroup(group);
    exit.setUser(user);
    exit.setExitedAt(LocalDateTime.now());
    exitRepository.save(exit);

    // 🔥 clave estilo WhatsApp
    if (wasAdmin && !group.getMembers().isEmpty()) {
        assignNewAdmin(group, userId);
    }
}
}
// NOTA ADICIONAL — en GroupActions.jsx el flujo handleLeaveAndDelete es:
// 1. leaveGroup → manda WS "X es ahora admin" 
// 2. deleteGroup → manda WS "__GROUP_DELETED__" → borra BD
// 
// El usuario B en GroupsPage recibe "__GROUP_DELETED__" y filtra el grupo.
// El usuario B en ChatPage recibe "__GROUP_DELETED__" y navega a /groups.
// Ambos casos están cubiertos.