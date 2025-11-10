package com.javaprgraming.javaproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.javaprgraming.javaproject.table.Item;

/**
 * 'Item' 테이블의 데이터베이스 작업을 처리하는 JPA 인터페이스입니다.
 * JpaRepository<Item, Long>: 'Item' 엔티티를 관리하며, 기본 키(ID)의 타입은 'Long'입니다.
 * * - JpaRepository 상속만으로 기본적인 CRUD(save, findById, findAll, delete) 기능이 자동 생성됩니다.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}