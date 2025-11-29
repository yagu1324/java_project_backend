package com.javaprgraming.javaproject.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.javaprgraming.javaproject.dto.BidRequest;
import com.javaprgraming.javaproject.dto.BidResponse;
import com.javaprgraming.javaproject.repository.BidRepository;
import com.javaprgraming.javaproject.repository.ItemRepository;
import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.Bid;
import com.javaprgraming.javaproject.table.Item;
import com.javaprgraming.javaproject.table.User;

@Controller
public class BidController {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BidRepository bidRepository;

    // ⭐ 핵심 로직: 클라이언트가 "/app/bid/{auctionId}"로 메시지를 보내면 이 함수가 실행됨
    @MessageMapping("/bid/{auctionId}")
    @SendTo("/topic/auction/{auctionId}") // 처리 결과를 "/topic/auction/{auctionId}" 구독자들에게 쏩니다
    @Transactional // DB 거래 안전하게 처리
    public BidResponse handleBid(@DestinationVariable Long auctionId, BidRequest bidRequest) {

        if (auctionId == null) {
            throw new RuntimeException("경매 ID가 없습니다.");
        }
        // 1. 경매 물품 조회
        Item item = itemRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("물품이 없습니다."));

        Long bidderId = bidRequest.getBidderId();
        if (bidderId == null) {
            throw new RuntimeException("입찰자 ID가 없습니다.");
        }
        // 2. 입찰자 조회
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new RuntimeException("사용자가 없습니다."));

        // 3. 유효성 검사 (현재가보다 높아야 입찰 가능)
        if (bidRequest.getBidAmount() <= item.getCurrentPrice()) {
            throw new RuntimeException("현재가보다 높은 금액만 입찰 가능합니다.");
        }

        // [추가] 자가 입찰 방지
        if (item.getSeller() != null && item.getSeller().getId().equals(bidder.getId())) {
            throw new RuntimeException("본인의 물건에는 입찰할 수 없습니다.");
        }

        // 4. 아이템 가격 갱신 (DB 업데이트)
        item.setCurrentPrice(bidRequest.getBidAmount());
        itemRepository.save(item);

        // 5. 입찰 내역 저장 (Bid 테이블)
        Bid bid = new Bid();
        bid.setItem(item);
        bid.setBidder(bidder);
        bid.setBidAmount(bidRequest.getBidAmount());
        bid.setBidTime(LocalDateTime.now());
        bidRepository.save(bid);

        // 6. 결과 메시지 생성 (구독자들에게 보낼 데이터)
        String timeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return new BidResponse(bidder.getUsername(), item.getCurrentPrice(), timeString);
    }

    @GetMapping("/api/auctions/{auctionId}/bids")
    @ResponseBody
    public List<BidResponse> getBidHistory(@PathVariable Long auctionId) {
        return bidRepository.findByItem_IdOrderByBidTimeDesc(auctionId).stream()
                .map(bid -> new BidResponse(
                        bid.getBidder().getUsername(),
                        bid.getBidAmount(),
                        bid.getBidTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .collect(Collectors.toList());
    }
}
