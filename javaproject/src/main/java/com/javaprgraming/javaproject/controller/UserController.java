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

/**
 * 사용자 인증(회원가입, 로그인) 및 사용자 정보 관련 API를 처리하는 컨트롤러입니다.
 * @RestController: 이 클래스가 JSON 형태의 데이터를 반환하는 REST API 컨트롤러임을 나타냅니다.
 * @RequestMapping("/api/auth"): 
 * 이 클래스의 모든 API는 공통적으로 "/api/auth" 라는 URL 경로를 가집니다.
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    // User 테이블에 접근하기 위한 Repository
    private final UserRepository userRepository;
    
    // SecurityConfig에 Bean으로 등록된 비밀번호 암호화 객체 (BCrypt)
    private final PasswordEncoder passwordEncoder;

    /**
     * 생성자 주입(Constructor Injection) 방식.
     * Spring이 UserRepository와 PasswordEncoder 객체를 자동으로 주입(넣어줌)해줍니다.
     */
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 API
     * [요청 방식] POST
     * [요청 URL] /api/auth/signup
     * @RequestBody: 프론트에서 보낸 JSON 데이터를 Map<String, String> 형태로 받습니다.
     * @return Map<String, Object> (성공/실패 여부를 담은 JSON 객체)
     */
    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody Map<String, String> request) {
        // 프론트에 응답할 JSON 객체 생성
        Map<String, Object> response = new HashMap<>();
        
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");
        String birthdate = request.get("birthdate");

        // 1. 아이디 중복 검사
        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "이미 사용중인 아이디입니다");
            return response; // 중복 시, 여기서 함수 종료
        }

        // 2. 새로운 User 객체를 생성하고, 폼 데이터를 채워넣습니다.
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // 3. (중요) 비밀번호를 암호화하여 저장합니다.
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        
        if (birthdate != null && !birthdate.isEmpty()) {
            user.setBirthdate(birthdate);
        }

        // 4. DB에 User 객체 저장 (INSERT 쿼리)
        userRepository.save(user);
        
        // 5. 성공 응답 반환
        response.put("success", true);
        response.put("message", "회원가입 성공!");
        response.put("username", user.getUsername());
        return response;
    }

    /**
     * 로그인 API (암호화 비교 적용됨)
     * [요청 방식] POST
     * [요청 URL] /api/auth/login
     * @RequestBody: 프론트에서 보낸 JSON 데이터를 Map<String, String> 형태로 받습니다.
     * @return Map<String, Object> (성공/실패 여부 및 사용자 정보를 담은 JSON 객체)
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String username = request.get("username");
        String password = request.get("password"); // 프론트에서 받은 '평문' 비밀번호

        // 1. 아이디로 DB에서 사용자 정보 조회
        User user = userRepository.findByUsername(username).orElse(null);
        
        // 2. 아이디 존재 여부 확인
        if (user == null) {
            response.put("success", false);
            response.put("message", "아이디가 존재하지 않습니다");
            return response;
        }

        // 3. (중요) 비밀번호 비교
        // passwordEncoder.matches(평문 비밀번호, DB에 저장된 암호화된 비밀번호)
        // -> 내부적으로 BCrypt가 비교하여 일치 여부를 boolean으로 반환
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "비밀번호가 틀렸습니다");
            return response;
        }

        // 4. 로그인 성공 응답
        // (주의) 비밀번호(user.getPassword())는 절대 응답에 포함하면 안 됩니다.
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        return response;
    }

    /**
     * 사용자 정보 조회 API
     * [요청 방식] GET
     * [요청 URL] /api/auth/user/{userId} (예: /api/auth/user/1)
     * @PathVariable: URL 경로에 포함된 변수(userId)를 가져옵니다.
     * @return Map<String, Object> (사용자 정보를 담은 JSON 객체)
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

        // 1. 성공 응답 (비밀번호 제외)
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("birthdate", user.getBirthdate());
        // 2. 프로필 이미지가 있는지(null이 아닌지) 여부를 boolean으로 전달
        response.put("hasProfileImage", user.getProfileImage() != null);
        return response;
    }

    /**
     * 프로필 이미지 업로드 API
     * [요청 방식] POST
     * [요청 URL] /api/auth/user/{userId}/profile-image
     * @param file (FormData의 'file' 항목으로 전송된 이미지 파일)
     * @return Map<String, Object> (성공/실패 여부를 담은 JSON 객체)
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

            // 1. 파일 유효성 검사 (비어있는지)
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다");
                return response;
            }

            // 2. 파일 크기 제한 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                response.put("success", false);
                response.put("message", "파일 크기는 5MB를 초과할 수 없습니다");
                return response;
            }

            // 3. 이미지 파일 형식 확인 (JPG, PNG)
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("image/jpeg") && 
                 !contentType.equals("image/png") && 
                 !contentType.equals("image/jpg"))) {
                response.put("success", false);
                response.put("message", "JPG, PNG 파일만 업로드 가능합니다");
                return response;
            }

            // 4. (중요) 이미지를 byte 배열(byte[])로 변환하여 DB에 저장
            //    (User.java의 profileImage 필드가 byte[] 타입이어야 함)
            user.setProfileImage(file.getBytes());
            userRepository.save(user); // DB에 업데이트

            response.put("success", true);
            response.put("message", "프로필 이미지가 업로드되었습니다");
            return response;

        } catch (IOException e) {
            // 파일 처리 중 입출력 오류 발생 시
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    /**
     * 프로필 이미지 조회 API
     * [요청 방식] GET
     * [요청 URL] /api/auth/user/{userId}/profile-image
     * (이 API는 브라우저의 <img src="..."> 태그가 직접 호출합니다)
     * @return ResponseEntity<byte[]> (JSON이 아닌, 순수 이미지 데이터(byte)를 반환)
     */
    @GetMapping("/user/{userId}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        // 1. 사용자가 없거나, 프로필 이미지가 없으면 404 Not Found 응답
        if (user == null || user.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. HTTP 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        // 2-1. (중요) 이 응답의 데이터가 '이미지(jpeg)' 타입임을 브라우저에 알림
        headers.setContentType(MediaType.IMAGE_JPEG); 
        
        // 3. 이미지 byte 배열, 헤더, HTTP 상태(200 OK)를 함께 반환
        return new ResponseEntity<>(user.getProfileImage(), headers, HttpStatus.OK);
    }

    /**
     * 프로필 이미지 삭제 API
     * [요청 방식] DELETE
     * [요청 URL] /api/auth/user/{userId}/profile-image
     * @return Map<String, Object> (성공/실패 여부를 담은 JSON 객체)
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

        // 1. DB의 profileImage 필드를 null로 설정
        user.setProfileImage(null);
        userRepository.save(user); // DB에 업데이트

        response.put("success", true);
        response.put("message", "프로필 이미지가 삭제되었습니다");
        return response;
    }
}