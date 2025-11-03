package com.javaprgraming.javaproject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javaprgraming.javaproject.table.User;

// JpaRepository를 상속받는 것만으로도 기본적인 DB 작업(save, findById 등)이 가능해집니다.
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}