package com.bingochain.controller;

import com.bingochain.model.WeeklyLottery;
import com.bingochain.service.WeeklyLotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/lotteries")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class WeeklyLotteryController {

    @Autowired
    private WeeklyLotteryService weeklyLotteryService;

    /**
     * Get all lotteries
     */
    @GetMapping
    public ResponseEntity<List<WeeklyLottery>> getAllLotteries() {
        List<WeeklyLottery> lotteries = weeklyLotteryService.getAllLotteries();
        return ResponseEntity.ok(lotteries);
    }

    /**
     * Get lottery by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WeeklyLottery> getLotteryById(@PathVariable Long id) {
        Optional<WeeklyLottery> lottery = weeklyLotteryService.getLotteryById(id);
        return lottery.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get current active lottery
     */
    @GetMapping("/current")
    public ResponseEntity<WeeklyLottery> getCurrentLottery() {
        Optional<WeeklyLottery> lottery = weeklyLotteryService.getCurrentLottery();
        return lottery.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get lotteries with active ticket sales
     */
    @GetMapping("/active-sales")
    public ResponseEntity<List<WeeklyLottery>> getActiveTicketSales() {
        List<WeeklyLottery> lotteries = weeklyLotteryService.getActiveTicketSales();
        return ResponseEntity.ok(lotteries);
    }

    /**
     * Get lotteries in drawing phase
     */
    @GetMapping("/drawing")
    public ResponseEntity<List<WeeklyLottery>> getLotteriesInDrawingPhase() {
        List<WeeklyLottery> lotteries = weeklyLotteryService.getLotteriesInDrawingPhase();
        return ResponseEntity.ok(lotteries);
    }

    /**
     * Get completed lotteries
     */
    @GetMapping("/completed")
    public ResponseEntity<List<WeeklyLottery>> getCompletedLotteries() {
        List<WeeklyLottery> lotteries = weeklyLotteryService.getCompletedLotteries();
        return ResponseEntity.ok(lotteries);
    }

    /**
     * Get lottery statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<WeeklyLotteryService.LotteryStats> getLotteryStatistics() {
        WeeklyLotteryService.LotteryStats stats = weeklyLotteryService.getLotteryStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Create a new lottery (admin only)
     */
    @PostMapping
    public ResponseEntity<WeeklyLottery> createLottery(@RequestBody CreateLotteryRequest request) {
        try {
            WeeklyLottery lottery = new WeeklyLottery();
            lottery.setContractAddress(request.getContractAddress());
            lottery.setLotteryName(request.getLotteryName());
            lottery.setTicketPrice(request.getTicketPrice());
            lottery.setSalesStartTime(request.getSalesStartTime());
            lottery.setSalesEndTime(request.getSalesEndTime());
            lottery.setNextDrawTime(request.getNextDrawTime());
            lottery.setStatus(WeeklyLottery.LotteryStatus.TICKET_SALES);

            WeeklyLottery createdLottery = weeklyLotteryService.createLottery(lottery);
            return ResponseEntity.ok(createdLottery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update lottery status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<WeeklyLottery> updateLotteryStatus(
            @PathVariable Long id, 
            @RequestBody UpdateStatusRequest request) {
        try {
            WeeklyLottery updatedLottery = weeklyLotteryService.updateLotteryStatus(id, request.getStatus());
            return ResponseEntity.ok(updatedLottery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update drawn numbers
     */
    @PutMapping("/{id}/drawn-numbers")
    public ResponseEntity<WeeklyLottery> updateDrawnNumbers(
            @PathVariable Long id, 
            @RequestBody UpdateDrawnNumbersRequest request) {
        try {
            WeeklyLottery updatedLottery = weeklyLotteryService.updateDrawnNumbers(id, request.getDrawnNumbers());
            return ResponseEntity.ok(updatedLottery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Request DTOs
    public static class CreateLotteryRequest {
        private String contractAddress;
        private String lotteryName;
        private java.math.BigDecimal ticketPrice;
        private java.time.LocalDateTime salesStartTime;
        private java.time.LocalDateTime salesEndTime;
        private java.time.LocalDateTime nextDrawTime;

        // Getters and setters
        public String getContractAddress() { return contractAddress; }
        public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
        
        public String getLotteryName() { return lotteryName; }
        public void setLotteryName(String lotteryName) { this.lotteryName = lotteryName; }
        
        public java.math.BigDecimal getTicketPrice() { return ticketPrice; }
        public void setTicketPrice(java.math.BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
        
        public java.time.LocalDateTime getSalesStartTime() { return salesStartTime; }
        public void setSalesStartTime(java.time.LocalDateTime salesStartTime) { this.salesStartTime = salesStartTime; }
        
        public java.time.LocalDateTime getSalesEndTime() { return salesEndTime; }
        public void setSalesEndTime(java.time.LocalDateTime salesEndTime) { this.salesEndTime = salesEndTime; }
        
        public java.time.LocalDateTime getNextDrawTime() { return nextDrawTime; }
        public void setNextDrawTime(java.time.LocalDateTime nextDrawTime) { this.nextDrawTime = nextDrawTime; }
    }

    public static class UpdateStatusRequest {
        private WeeklyLottery.LotteryStatus status;

        public WeeklyLottery.LotteryStatus getStatus() { return status; }
        public void setStatus(WeeklyLottery.LotteryStatus status) { this.status = status; }
    }

    public static class UpdateDrawnNumbersRequest {
        private String drawnNumbers;

        public String getDrawnNumbers() { return drawnNumbers; }
        public void setDrawnNumbers(String drawnNumbers) { this.drawnNumbers = drawnNumbers; }
    }
}
