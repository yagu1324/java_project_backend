package com.javaprgraming.javaproject.service;

import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

        // 3. "새로운 유저 데이터를 저장" (user 테이블에 새 '줄'을 추가)
        return userRepository.save(user);
    }

    // (이 밑에 로그인 로직도 추가할 수 있습니다)
    // public User loginUser(...) { ... }
}