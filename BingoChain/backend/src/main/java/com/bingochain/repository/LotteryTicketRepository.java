package com.bingochain.repository;

import com.bingochain.model.LotteryTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotteryTicketRepository extends JpaRepository<LotteryTicket, Long> {

    List<LotteryTicket> findByWalletAddress(String walletAddress);

    List<LotteryTicket> findByWalletAddressAndWeeklyLotteryId(String walletAddress, Long weeklyLotteryId);

    List<LotteryTicket> findByWeeklyLotteryId(Long weeklyLotteryId);

    Optional<LotteryTicket> findByTicketId(String ticketId);

    @Query("SELECT t FROM LotteryTicket t WHERE t.isWinner = true")
    List<LotteryTicket> findAllWinningTickets();

    @Query("SELECT t FROM LotteryTicket t WHERE t.weeklyLottery.id = :lotteryId AND t.isWinner = true")
    List<LotteryTicket> findWinningTicketsByLotteryId(@Param("lotteryId") Long lotteryId);

    @Query("SELECT COUNT(t) FROM LotteryTicket t WHERE t.walletAddress = :walletAddress")
    Long countTicketsByPlayer(@Param("walletAddress") String walletAddress);

    @Query("SELECT COUNT(t) FROM LotteryTicket t WHERE t.walletAddress = :walletAddress AND t.isWinner = true")
    Long countWinsByPlayer(@Param("walletAddress") String walletAddress);

    @Query("SELECT SUM(t.ticketPricePaid) FROM LotteryTicket t WHERE t.walletAddress = :walletAddress")
    Double getTotalSpentByPlayer(@Param("walletAddress") String walletAddress);

    @Query("SELECT SUM(t.prizeAmount) FROM LotteryTicket t WHERE t.walletAddress = :walletAddress AND t.isWinner = true")
    Double getTotalWonByPlayer(@Param("walletAddress") String walletAddress);

    @Query("SELECT t FROM LotteryTicket t WHERE t.walletAddress = :walletAddress ORDER BY t.purchasedAt DESC")
    List<LotteryTicket> findByWalletAddressOrderByPurchasedAtDesc(@Param("walletAddress") String walletAddress);

    @Query("SELECT t FROM LotteryTicket t WHERE t.weeklyLottery.id = :lotteryId AND t.matchedNumbers = :matchedNumbers")
    List<LotteryTicket> findTicketsByLotteryAndMatches(@Param("lotteryId") Long lotteryId, @Param("matchedNumbers") Integer matchedNumbers);
}
