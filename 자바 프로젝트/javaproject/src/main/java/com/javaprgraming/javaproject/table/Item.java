package com.javaprgraming.javaproject.table;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
@Entity 
public class Item {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 물건 이름
    private String name;
    
    // 물건 카테고리
    private String category;
    
    // 물건 상태 (Enum 타입을 문자열 형태로 DB에 저장합니다)
    @Enumerated(EnumType.STRING) 
    private ItemStatus status;
    
    // 경매 마감 시간
    private LocalDateTime auctionEndTime;
    
    // 경매 시작 가격
    private Long startPrice;
    
    // 현재 입찰 가격 (입찰이 들어올 때마다 업데이트 됩니다)
    private Long currentPrice;
    
    // '다대일' 관계 설정: 여러개의 물품(Item)은 한 명의 판매자(User)에게 속합니다.
    @ManyToOne 
    private User seller;

    // --- 이하 Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public Long getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(Long startPrice) {
        this.startPrice = startPrice;
    }

    public Long getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Long currentPrice) {
        this.currentPrice = currentPrice;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }
}