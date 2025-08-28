package com.bingochain.service;

import com.bingochain.model.WeeklyLottery;
import com.bingochain.repository.WeeklyLotteryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WeeklyLotteryService {

    @Autowired
    private WeeklyLotteryRepository weeklyLotteryRepository;

    /**
     * Get all lotteries ordered by creation date (newest first)
     */
    public List<WeeklyLottery> getAllLotteries() {
        return weeklyLotteryRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * Get lottery by ID
     */
    public Optional<WeeklyLottery> getLotteryById(Long id) {
        return weeklyLotteryRepository.findById(id);
    }

    /**
     * Get current active lottery (latest created)
     */
    public Optional<WeeklyLottery> getCurrentLottery() {
        return weeklyLotteryRepository.findTopByOrderByCreatedAtDesc();
    }

    /**
     * Get lotteries with active ticket sales
     */
    public List<WeeklyLottery> getActiveTicketSales() {
        LocalDateTime now = LocalDateTime.now();
        return weeklyLotteryRepository.findActiveTicketSales(now);
    }

    /**
     * Get lotteries in drawing phase
     */
    public List<WeeklyLottery> getLotteriesInDrawingPhase() {
        return weeklyLotteryRepository.findLotteriesInDrawingPhase();
    }

    /**
     * Get completed lotteries
     */
    public List<WeeklyLottery> getCompletedLotteries() {
        return weeklyLotteryRepository.findCompletedLotteries();
    }

    /**
     * Create a new weekly lottery
     */
    public WeeklyLottery createLottery(WeeklyLottery lottery) {
        lottery.setCreatedAt(LocalDateTime.now());
        lottery.setUpdatedAt(LocalDateTime.now());
        return weeklyLotteryRepository.save(lottery);
    }

    /**
     * Update lottery status
     */
    public WeeklyLottery updateLotteryStatus(Long lotteryId, WeeklyLottery.LotteryStatus status) {
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryRepository.findById(lotteryId);
        if (lotteryOpt.isPresent()) {
            WeeklyLottery lottery = lotteryOpt.get();
            lottery.setStatus(status);
            lottery.setUpdatedAt(LocalDateTime.now());
            return weeklyLotteryRepository.save(lottery);
        }
        throw new RuntimeException("Lottery not found with ID: " + lotteryId);
    }

    /**
     * Update lottery with drawn numbers
     */
    public WeeklyLottery updateDrawnNumbers(Long lotteryId, String drawnNumbers) {
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryRepository.findById(lotteryId);
        if (lotteryOpt.isPresent()) {
            WeeklyLottery lottery = lotteryOpt.get();
            lottery.setDrawnNumbers(drawnNumbers);
            lottery.setUpdatedAt(LocalDateTime.now());
            return weeklyLotteryRepository.save(lottery);
        }
        throw new RuntimeException("Lottery not found with ID: " + lotteryId);
    }

    /**
     * Update lottery current draw day
     */
    public WeeklyLottery updateCurrentDrawDay(Long lotteryId, Integer drawDay) {
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryRepository.findById(lotteryId);
        if (lotteryOpt.isPresent()) {
            WeeklyLottery lottery = lotteryOpt.get();
            lottery.setCurrentDrawDay(drawDay);
            lottery.setUpdatedAt(LocalDateTime.now());
            return weeklyLotteryRepository.save(lottery);
        }
        throw new RuntimeException("Lottery not found with ID: " + lotteryId);
    }

    /**
     * Update lottery next draw time
     */
    public WeeklyLottery updateNextDrawTime(Long lotteryId, LocalDateTime nextDrawTime) {
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryRepository.findById(lotteryId);
        if (lotteryOpt.isPresent()) {
            WeeklyLottery lottery = lotteryOpt.get();
            lottery.setNextDrawTime(nextDrawTime);
            lottery.setUpdatedAt(LocalDateTime.now());
            return weeklyLotteryRepository.save(lottery);
        }
        throw new RuntimeException("Lottery not found with ID: " + lotteryId);
    }

    /**
     * Mark prizes as distributed
     */
    public WeeklyLottery markPrizesDistributed(Long lotteryId) {
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryRepository.findById(lotteryId);
        if (lotteryOpt.isPresent()) {
            WeeklyLottery lottery = lotteryOpt.get();
            lottery.setPrizesDistributed(true);
            lottery.setUpdatedAt(LocalDateTime.now());
            return weeklyLotteryRepository.save(lottery);
        }
        throw new RuntimeException("Lottery not found with ID: " + lotteryId);
    }

    /**
     * Get lotteries ready for draw
     */
    public List<WeeklyLottery> getLotteriesReadyForDraw() {
        LocalDateTime now = LocalDateTime.now();
        return weeklyLotteryRepository.findLotteriesReadyForDraw(now);
    }

    /**
     * Get lottery statistics
     */
    public LotteryStats getLotteryStatistics() {
        Long totalLotteries = weeklyLotteryRepository.count();
        Long completedLotteries = weeklyLotteryRepository.countCompletedLotteries();
        Double totalPrizesPaid = weeklyLotteryRepository.getTotalPrizePoolPaid();
        
        return new LotteryStats(totalLotteries, completedLotteries, totalPrizesPaid != null ? totalPrizesPaid : 0.0);
    }

    /**
     * Helper class for lottery statistics
     */
    public static class LotteryStats {
        private final Long totalLotteries;
        private final Long completedLotteries;
        private final Double totalPrizesPaid;

        public LotteryStats(Long totalLotteries, Long completedLotteries, Double totalPrizesPaid) {
            this.totalLotteries = totalLotteries;
            this.completedLotteries = completedLotteries;
            this.totalPrizesPaid = totalPrizesPaid;
        }

        public Long getTotalLotteries() { return totalLotteries; }
        public Long getCompletedLotteries() { return completedLotteries; }
        public Double getTotalPrizesPaid() { return totalPrizesPaid; }
    }
}
