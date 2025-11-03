package com.javaprgraming.javaproject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javaprgraming.javaproject.table.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
        
}