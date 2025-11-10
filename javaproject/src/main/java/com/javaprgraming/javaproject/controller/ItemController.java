package com.javaprgraming.javaproject.controller;

import com.javaprgraming.javaproject.repository.ItemRepository;
import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.Item;
import com.javaprgraming.javaproject.table.ItemStatus;
import com.javaprgraming.javaproject.table.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime; // ⭐ [추가]
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions") // '/api/auctions'로 시작하는 모든 요청을 처리
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path rootLocation = Paths.get("uploads");

    public ItemController() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    /**
     * 경매 등록 (이미지 파일 처리 포함)
     * POST /api/auctions/register
     */
    @PostMapping("/register")
    public Map<String, Object> registerAuction(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("startingPrice") Long startingPrice,
            @RequestParam(value = "buyNowPrice", required = false) Long buyNowPrice,
            @RequestParam("bidIncrement") Long bidIncrement,
            @RequestParam("endTime") String endTimeString, // ⭐ [수정] "duration" 대신 "endTime"
            @RequestParam("sellerId") Long sellerId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            User seller = userRepository.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없습니다: " + sellerId));

            Item item = new Item();
            item.setName(title);
            item.setCategory(category);
            item.setDescription(description); 
            item.setStartPrice(startingPrice);
            item.setCurrentPrice(startingPrice); 
            item.setBuyNowPrice(buyNowPrice);
            item.setBidIncrement(bidIncrement);
            
            // ⭐ [수정] 'Z'가 붙은 시간(ZonedDateTime)을 일반 시간(LocalDateTime)으로 변환
            ZonedDateTime zdt = ZonedDateTime.parse(endTimeString);
            item.setAuctionEndTime(zdt.toLocalDateTime()); 
            
            item.setStatus(ItemStatus.ON_AUCTION);
            item.setSeller(seller);
            
            // ⭐ [수정] DB 오류 해결
            item.setSellerUsername(seller.getUsername()); 

            // 이미지 파일 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                Files.copy(imageFile.getInputStream(), this.rootLocation.resolve(uniqueFilename));
                item.setImageUrl("/uploads/" + uniqueFilename);
            } else {
                item.setImageUrl(null); 
            }

            itemRepository.save(item);

            response.put("success", true);
            response.put("message", "경매가 성공적으로 등록되었습니다.");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "경매 등록 실패: " + e.getMessage());
            e.printStackTrace(); // 콘솔에 자세한 오류 출력
        }
        return response;
    }

    /**
     * 메인 페이지 경매 목록
     * GET /api/auctions
     */
    @GetMapping
    public List<Item> getAllAuctions() {
        return itemRepository.findAll();
    }
}