package com.javaprgraming.javaproject.table;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

// 이 클래스가 DB 테이블임을 나타냅니다.
@Entity
public class User {

    // 이 필드가 테이블의 고유 식별자(Primary Key)임을 나타냅니다.
    @Id
    // ID 값을 데이터베이스가 자동으로 생성하도록 설정합니다. (1, 2, 3...)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원의 로그인 ID
    private String username;

    // 회원의 비밀번호
    private String password;
    
    // 회원의 생년월일
    private LocalDate birthdate;
    
    //전화번호
    private String email;

    // 회원이 가진 포인트 (경매에 사용될 돈)
    private Long points = 0L; // 기본값으로 0을 설정합니다.

    // --- 이하 Getters and Setters (데이터를 넣고 꺼내기 위한 필수 메소드) ---
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
    public String getemail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}