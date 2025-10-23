package com.javaprgraming.javaproject;

import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository를 상속받는 것만으로도 기본적인 DB 작업(save, findById 등)이 가능해집니다.
public interface UserRepository extends JpaRepository<User, Long> {
}