package com.javaprgraming.javaproject.controller;

import com.javaprgraming.javaproject.table.User;
import com.javaprgraming.javaproject.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    
    private final UserRepository userRepository;
    
    // 2. SecurityConfig에 등록한 '비밀번호 해싱기' 부품을 가져옵니다.
    private final PasswordEncoder passwordEncoder; 
    
    // 3. 생성자(Constructor)를 통해 부품 2개(DB 저장소, 해싱기)를 주입받습니다.
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // 3-2. 해싱기 초기화
    }
    
    /**
     * 회원가입 (암호화 적용됨)
     */
    @PostMapping("/signup")
public Map<String, Object> signup(@RequestBody Map<String, String> request) {
    Map<String, Object> response = new HashMap<>();

    // 1. 프론트에서 보낸 데이터를 받습니다.
    String username = request.get("username");
    String password = request.get("password");
    String email = request.get("email"); // ⭐ email 데이터 받기 추가
    String birthdate = request.get("birthdate");

    if (userRepository.existsByUsername(username)) {
        response.put("success", false);
        response.put("message", "이미 사용중인 아이디입니다");
        return response;
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(email); // ⭐ User 객체에 email 설정 추가

    String hashedPassword = passwordEncoder.encode(password);
    user.setPassword(hashedPassword);

    if (birthdate != null && !birthdate.isEmpty()) {
        user.setBirthdate(LocalDate.parse(birthdate));
    }

    userRepository.save(user);

    response.put("success", true);
    response.put("message", "회원가입 성공!");
    response.put("username", user.getUsername()); // 프론트 알림창을 위해 username 추가
    return response;
}
    
    /**
     * 로그인 (암호화 비교 적용됨)
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String username = request.get("username");
        String password = request.get("password");
        
        // 사용자 찾기
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "아이디가 존재하지 않습니다");
            return response;
        }
        
        // 5. (!!중요!!) 해시값과 원본 비밀번호를 비교합니다.
        // if (!user.getPassword().equals(password)) { <-- (이전 평문 비교)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "비밀번호가 틀렸습니다");
            return response;
        }
        
        // 로그인 성공
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("points", user.getPoints());
        return response;
    }
    
    /**
     * 사용자 정보 조회
     * GET /api/auth/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "사용자를 찾을 수 없습니다");
            return response;
        }
        
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("points", user.getPoints());
        return response;
    }
}