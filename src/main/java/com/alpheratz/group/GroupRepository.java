package com.alpheratz.group;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    // Grupos donde el usuario es miembro
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.id = :userId")
    List<Group> findGroupsByMemberId(@Param("userId") Long userId);

    // Grupos donde el usuario es admin
    List<Group> findByAdminId(Long adminId);

    // Verificar si un usuario pertenece a un grupo
    @Query("SELECT COUNT(m) > 0 FROM Group g JOIN g.members m WHERE g.id = :groupId AND m.id = :userId")
    boolean existsMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
}