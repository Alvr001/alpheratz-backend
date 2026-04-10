package com.alpheratz.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAnimalId(String animalId);

    boolean existsByEmail(String email);

    boolean existsByAnimalId(String animalId);
}