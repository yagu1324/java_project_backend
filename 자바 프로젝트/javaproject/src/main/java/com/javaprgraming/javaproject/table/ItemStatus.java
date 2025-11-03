package com.javaprgraming.javaproject.table; // Item.java와 동일한 패키지

import jakarta.persistence.Entity;

@Entity
// public으로 선언된 자신만의 파일을 가집니다.
public enum ItemStatus {
    ON_AUCTION, // 경매중
    SOLD,       // 판매완료
    CANCELLED   // 판매취소
}