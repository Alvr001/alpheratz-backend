package com.alpheratz.group;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g JOIN FETCH g.members WHERE g.id = :groupId")
    Optional<Group> findByIdWithMembers(@Param("groupId") Long groupId);

    // Grupos donde el usuario es miembro
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.id = :userId")
    List<Group> findGroupsByMemberId(@Param("userId") Long userId);

    // Grupos donde el usuario es admin
    List<Group> findByAdminId(Long adminId);

    // Grupos donde el usuario es miembro activo O salió del grupo
    @Query("SELECT DISTINCT g FROM Group g WHERE " +
           "EXISTS (SELECT m FROM g.members m WHERE m.id = :userId) OR " +
           "EXISTS (SELECT e FROM GroupMemberExit e WHERE e.group = g AND e.user.id = :userId)")
    List<Group> findAllGroupsByUserId(@Param("userId") Long userId);

    // Verificar si es miembro actual
    @Query("SELECT COUNT(m) > 0 FROM Group g JOIN g.members m WHERE g.id = :groupId AND m.id = :userId")
    boolean existsMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<Group> findByMembersId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Group g WHERE g.admin.id = :userId AND SIZE(g.members) = 0")
    void deleteEmptyGroupsByAdmin(@Param("userId") Long userId);

}