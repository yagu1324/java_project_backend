package com.javaprgraming.javaproject.service;

import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 앤코더 객체 형성
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 회원가입 핵심 로직
     */
    public User registerUser(User user) {

        // 2. "id 가 일단 존재하는지 확인"
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());

        // 2. "만약 id가 존재한다면 안된다고 반환"
        if (existingUser.isPresent()) {
            // "이미 존재하는 아이디입니다" 라는 에러를 발생시킵니다.
            throw new RuntimeException("이미 존재하는 아이디입니다: " + user.getUsername());
        }

        // 비밀번호  암호화
        String rawPassword = user.getPassword();
        if (rawPassword != null && !rawPassword.isEmpty()) { //비밀번호가 비어있지 않으면, 암호화 진행
            String hashed = passwordEncoder.encode(rawPassword);
            user.setPassword(hashed);
        }

        // 3. "새로운 유저 데이터를 저장" (user 테이블에 새 '줄'을 추가)
        return userRepository.save(user);
    }

    /**
     * 로그인: username + password 를 받아 인증합니다.
     * 성공하면 User 객체(비밀번호는 null 처리)를 반환합니다.
     */
    public User loginUser(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username); // username으로 존재하는지 확인
        if (opt.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다: " + username);
        }

        User user = opt.get();

        // 비교: DB에 저장된 것은 해시값이므로 matches로 확인 // 암호화 한 비밀번호와 평문 암호와 match 시켜 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 반환 시 비밀번호는 외부로 노출하지 않도록 null 처리
        user.setPassword(null);
        return user;
    }

    
}