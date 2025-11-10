package com.javaprgraming.javaproject.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.User;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 (암호화 적용됨)
     */
    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");
        String birthdate = request.get("birthdate");

        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "이미 사용중인 아이디입니다");
            return response;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        
        if (birthdate != null && !birthdate.isEmpty()) {
            user.setBirthdate(birthdate);
        }

        userRepository.save(user);
        response.put("success", true);
        response.put("message", "회원가입 성공!");
        response.put("username", user.getUsername());
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

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "아이디가 존재하지 않습니다");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "비밀번호가 틀렸습니다");
            return response;
        }

        response.put("success", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        return response;
    }

    /**
     * 사용자 정보 조회
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
        response.put("email", user.getEmail());
        response.put("birthdate", user.getBirthdate());
        response.put("hasProfileImage", user.getProfileImage() != null);
        return response;
    }

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/user/{userId}/profile-image")
    public Map<String, Object> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
                return response;
            }

            // 파일 유효성 검사
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다");
                return response;
            }

            // 파일 크기 제한 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB를 초과할 수 없습니다");
                return response;
            }

            // 이미지 파일 형식 확인
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("image/jpeg") && 
                 !contentType.equals("image/png") && 
                 !contentType.equals("image/jpg"))) {
                response.put("success", false);
                response.put("message", "JPG, PNG 파일만 업로드 가능합니다");
                return response;
            }

            // 이미지를 byte[]로 변환하여 저장
            user.setProfileImage(file.getBytes());
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "프로필 이미지가 업로드되었습니다");
            return response;

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    /**
     * 프로필 이미지 조회
     */
    @GetMapping("/user/{userId}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null || user.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        
        return new ResponseEntity<>(user.getProfileImage(), headers, HttpStatus.OK);
    }

    /**
     * 프로필 이미지 삭제
     */
    @DeleteMapping("/user/{userId}/profile-image")
    public Map<String, Object> deleteProfileImage(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "사용자를 찾을 수 없습니다");
            return response;
        }

        user.setProfileImage(null);
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "프로필 이미지가 삭제되었습니다");
        return response;
    }
}