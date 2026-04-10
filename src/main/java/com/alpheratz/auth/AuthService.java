package com.alpheratz.auth;

import java.util.Random;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import com.alpheratz.friend.FriendRepository;
import com.alpheratz.group.Group;
import com.alpheratz.group.GroupRepository;
import com.alpheratz.group.GroupMemberExitRepository;
import com.alpheratz.message.MessageRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.alpheratz.auth.dto.AuthResponse;
import com.alpheratz.auth.dto.LoginRequest;
import com.alpheratz.auth.dto.RegisterRequest;
import com.alpheratz.friend.FriendRepository;
import com.alpheratz.group.GroupMemberExitRepository;
import com.alpheratz.group.GroupRepository;
import com.alpheratz.message.MessageRepository;
import com.alpheratz.user.User;
import com.alpheratz.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FriendRepository friendRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberExitRepository groupMemberExitRepository;
    private final MessageRepository messageRepository;

    private static final String[] ANIMALS = {
        "fox", "tiger", "eagle", "wolf", "bear", "shark",
        "lion", "hawk", "lynx", "puma", "crane", "raven",
        "otter", "cobra", "bison", "moose", "viper", "gecko"
    };

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        String animalId = generateAnimalId();

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAnimalId(animalId);
        user.setEmailVerified(true); // sin verificación por ahora

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getAnimalId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("INVALID_PASSWORD");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getAnimalId());
    }

    private String generateAnimalId() {
        Random random = new Random();
        String candidate;
        do {
            String animal = ANIMALS[random.nextInt(ANIMALS.length)];
            int number = 1000 + random.nextInt(9000);
            candidate = animal + "@" + number;
        } while (userRepository.existsByAnimalId(candidate));
        return candidate;
    }
   @Transactional
public void deleteAccount(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

    // 1. Nullear sender en mensajes
    messageRepository.nullifySender(userId);

    // 2. Eliminar amistades
    friendRepository.deleteBySenderIdOrReceiverId(userId, userId);

    // 3. Eliminar salidas de grupos
    groupMemberExitRepository.deleteByUserId(userId);

    // 4. Sacar de grupos
    List<Group> groups = groupRepository.findByMembersId(userId);
    for (Group group : groups) {
        group.getMembers().remove(user);

        if (group.getAdmin().getId().equals(userId)) {
            List<User> remaining = group.getMembers().stream()
                    .filter(m -> !m.getId().equals(userId))
                    .toList();

            if (remaining.isEmpty()) {
                groupRepository.delete(group);
            } else {
                group.setAdmin(remaining.get(0));
                groupRepository.save(group);
            }
        } else {
            groupRepository.save(group);
        }
    }

    // 5. Limpiar grupos vacíos
    groupRepository.deleteEmptyGroupsByAdmin(userId);

    // 6. Borrar usuario
    userRepository.delete(user);
    }
}