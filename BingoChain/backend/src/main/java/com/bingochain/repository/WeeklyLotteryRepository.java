package com.bingochain.repository;

import com.bingochain.model.WeeklyLottery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyLotteryRepository extends JpaRepository<WeeklyLottery, Long> {

    Optional<WeeklyLottery> findByContractAddress(String contractAddress);

    List<WeeklyLottery> findByStatus(WeeklyLottery.LotteryStatus status);

    @Query("SELECT l FROM WeeklyLottery l WHERE l.status = 'TICKET_SALES' AND l.salesStartTime <= :currentTime AND l.salesEndTime >= :currentTime")
    List<WeeklyLottery> findActiveTicketSales(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT l FROM WeeklyLottery l WHERE l.status = 'DRAWING_PHASE'")
    List<WeeklyLottery> findLotteriesInDrawingPhase();

    @Query("SELECT l FROM WeeklyLottery l WHERE l.status = 'DRAWING_PHASE' AND l.nextDrawTime <= :currentTime")
    List<WeeklyLottery> findLotteriesReadyForDraw(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT l FROM WeeklyLottery l ORDER BY l.createdAt DESC")
    List<WeeklyLottery> findAllOrderByCreatedAtDesc();

    @Query("SELECT l FROM WeeklyLottery l WHERE l.status = 'COMPLETED'")
    List<WeeklyLottery> findCompletedLotteries();

    @Query("SELECT COUNT(l) FROM WeeklyLottery l WHERE l.status = 'COMPLETED'")
    Long countCompletedLotteries();

    @Query("SELECT SUM(l.prizePool) FROM WeeklyLottery l WHERE l.status = 'COMPLETED' AND l.prizesDistributed = true")
    Double getTotalPrizePoolPaid();

    @Query("SELECT l FROM WeeklyLottery l WHERE l.salesStartTime >= :startDate AND l.salesStartTime <= :endDate")
    List<WeeklyLottery> findLotteriesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Optional<WeeklyLottery> findTopByOrderByCreatedAtDesc();
}
