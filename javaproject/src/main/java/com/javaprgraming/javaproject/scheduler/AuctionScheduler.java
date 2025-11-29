package com.javaprgraming.javaproject.scheduler;

import com.javaprgraming.javaproject.repository.BidRepository;
import com.javaprgraming.javaproject.repository.HistoryRepository;
import com.javaprgraming.javaproject.repository.ItemRepository;
import com.javaprgraming.javaproject.repository.UserRepository;
import com.javaprgraming.javaproject.table.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuctionScheduler {

    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    public AuctionScheduler(ItemRepository itemRepository, BidRepository bidRepository, UserRepository userRepository,
            HistoryRepository historyRepository) {
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void checkEndedAuctions() {
        List<Item> endedItems = itemRepository.findByStatusAndAuctionEndTimeBefore(ItemStatus.ON_AUCTION,
                LocalDateTime.now());

        for (Item item : endedItems) {
            Bid highestBid = bidRepository.findTopByItemOrderByBidAmountDesc(item);

            if (highestBid != null) {
                // 낙찰 처리
                User buyer = highestBid.getBidder();
                User seller = item.getSeller();
                Long price = highestBid.getBidAmount();

                // 포인트 정산
                // 구매자 포인트 차감 (이미 입찰 시 차감하지 않았다면 여기서 차감)
                if (buyer.getPoints() >= price.intValue()) {
                    buyer.setPoints(buyer.getPoints() - price.intValue());
                    seller.setPoints(seller.getPoints() + price.intValue());

                    userRepository.save(buyer);
                    userRepository.save(seller);

                    // 거래 기록 저장
                    History history = new History(buyer, seller, item, price, LocalDateTime.now());
                    historyRepository.save(history);

                    // 아이템 상태 변경
                    item.setStatus(ItemStatus.SOLD);
                } else {
                    // 포인트 부족으로 낙찰 취소 -> 유찰 처리
                    System.out.println("낙찰 실패: 구매자(" + buyer.getId() + ") 포인트 부족. 상품 ID: " + item.getId());
                    item.setStatus(ItemStatus.CLOSED);
                }
            } else {
                // 유찰 처리
                item.setStatus(ItemStatus.CLOSED);
            }
            itemRepository.save(item);
        }
    }
}
