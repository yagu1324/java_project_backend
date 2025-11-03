package com.javaprgraming.javaproject.controller;

import com.javaprgraming.javaproject.table.User;
import com.javaprgraming.javaproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 2. 이 컨트롤러에 오는 모든 요청을 허용합니다.
//    (Live Server의 기본 주소인 127.0.0.1:5500을 허용해줍니다)
@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
public class UserController {

    @Autowired
    private UserService userService; // 로직을 담당할 Service를 연결

    
    /**
     * 회원가입 API (/register)
     */
    @PostMapping("/register")
    // 1. "html 에서 회원가입 정보들을 입력받는다"
    //    (@RequestBody User user 가 JSON 데이터를 User 객체로 자동 변환해줍니다)
    public User registerUser(@RequestBody User user) {
        
        // 2, 3단계의 실제 로직은 Service에게 맡깁니다.
        return userService.registerUser(user);
    }

    // (이 밑에 로그인 API도 추가할 수 있습니다)
    // @PostMapping("/login") ...
}