package com.bingochain.repository;

import com.bingochain.model.DrawEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrawEventRepository extends JpaRepository<DrawEvent, Long> {

    List<DrawEvent> findByWeeklyLotteryIdOrderByDrawDay(Long weeklyLotteryId);

    List<DrawEvent> findByWeeklyLotteryId(Long weeklyLotteryId);

    Optional<DrawEvent> findByWeeklyLotteryIdAndDrawDay(Long weeklyLotteryId, Integer drawDay);

    @Query("SELECT d FROM DrawEvent d WHERE d.weeklyLottery.id = :lotteryId AND d.drawDay = :drawDay")
    Optional<DrawEvent> findDrawForLotteryAndDay(@Param("lotteryId") Long lotteryId, @Param("drawDay") Integer drawDay);

    @Query("SELECT d FROM DrawEvent d WHERE d.scheduledTime <= :currentTime AND d.drawnAt IS NULL")
    List<DrawEvent> findPendingDraws(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT MAX(d.drawDay) FROM DrawEvent d WHERE d.weeklyLottery.id = :lotteryId")
    Optional<Integer> findMaxDrawDayForLottery(@Param("lotteryId") Long lotteryId);

    @Query("SELECT d.drawnNumber FROM DrawEvent d WHERE d.weeklyLottery.id = :lotteryId ORDER BY d.drawDay")
    List<Integer> findDrawnNumbersByLotteryId(@Param("lotteryId") Long lotteryId);

    boolean existsByWeeklyLotteryIdAndDrawnNumber(Long weeklyLotteryId, Integer drawnNumber);

    @Query("SELECT COUNT(d) FROM DrawEvent d WHERE d.weeklyLottery.id = :lotteryId")
    Long countDrawsByLotteryId(@Param("lotteryId") Long lotteryId);
}
