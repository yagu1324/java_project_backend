package com.javaprgraming.javaproject.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입
    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다: " + user.getUsername());
        }

        String rawPassword = user.getPassword();
        if (rawPassword != null && !rawPassword.isEmpty()) {
            String hashed = passwordEncoder.encode(rawPassword);
            user.setPassword(hashed);
        }

        // 포인트 초기값 0 보장
        if (user.getPoints() == null) {
            user.setPoints(0);
        }

        return userRepository.save(user);
    }

    // 로그인
    public User loginUser(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다: " + username);
        }

        User user = opt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(null);
        return user;
    }

    // ⭐ [추가] 포인트 충전
    public User addPoints(Long userId, int pointsToAdd) {
        if (userId == null) {
            throw new RuntimeException("사용자 ID가 없습니다.");
        }
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }

        User user = opt.get();
        Integer current = user.getPoints();
        if (current == null)
            current = 0;

        user.setPoints(current + pointsToAdd);
        return userRepository.save(user);
    }

    // ⭐ [추가] 포인트 조회
    public int getPoints(Long userId) {
        if (userId == null) {
            throw new RuntimeException("사용자 ID가 없습니다.");
        }
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }
        Integer p = opt.get().getPoints();
        return p == null ? 0 : p;
    }
}