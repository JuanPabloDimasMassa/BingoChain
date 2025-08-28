package com.bingochain.controller;

import com.bingochain.model.LotteryTicket;
import com.bingochain.service.LotteryTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tickets")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class LotteryTicketController {

    @Autowired
    private LotteryTicketService lotteryTicketService;

    /**
     * Get ticket by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LotteryTicket> getTicketById(@PathVariable Long id) {
        Optional<LotteryTicket> ticket = lotteryTicketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get ticket by contract ticket ID
     */
    @GetMapping("/contract/{ticketId}")
    public ResponseEntity<LotteryTicket> getTicketByTicketId(@PathVariable String ticketId) {
        Optional<LotteryTicket> ticket = lotteryTicketService.getTicketByTicketId(ticketId);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all tickets for a player
     */
    @GetMapping("/player/{walletAddress}")
    public ResponseEntity<List<LotteryTicket>> getPlayerTickets(@PathVariable String walletAddress) {
        List<LotteryTicket> tickets = lotteryTicketService.getPlayerTickets(walletAddress);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get player tickets for a specific lottery
     */
    @GetMapping("/player/{walletAddress}/lottery/{lotteryId}")
    public ResponseEntity<List<LotteryTicket>> getPlayerTicketsForLottery(
            @PathVariable String walletAddress,
            @PathVariable Long lotteryId) {
        List<LotteryTicket> tickets = lotteryTicketService.getPlayerTicketsForLottery(walletAddress, lotteryId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get all tickets for a lottery
     */
    @GetMapping("/lottery/{lotteryId}")
    public ResponseEntity<List<LotteryTicket>> getLotteryTickets(@PathVariable Long lotteryId) {
        List<LotteryTicket> tickets = lotteryTicketService.getLotteryTickets(lotteryId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get winning tickets for a lottery
     */
    @GetMapping("/lottery/{lotteryId}/winners")
    public ResponseEntity<List<LotteryTicket>> getWinningTickets(@PathVariable Long lotteryId) {
        List<LotteryTicket> tickets = lotteryTicketService.getWinningTickets(lotteryId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get all winning tickets
     */
    @GetMapping("/winners")
    public ResponseEntity<List<LotteryTicket>> getAllWinningTickets() {
        List<LotteryTicket> tickets = lotteryTicketService.getAllWinningTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get player statistics
     */
    @GetMapping("/player/{walletAddress}/statistics")
    public ResponseEntity<LotteryTicketService.PlayerStats> getPlayerStatistics(@PathVariable String walletAddress) {
        LotteryTicketService.PlayerStats stats = lotteryTicketService.getPlayerStatistics(walletAddress);
        return ResponseEntity.ok(stats);
    }

    /**
     * Purchase a ticket
     */
    @PostMapping("/purchase")
    public ResponseEntity<LotteryTicket> purchaseTicket(@RequestBody PurchaseTicketRequest request) {
        try {
            // Validate chosen numbers
            if (!lotteryTicketService.validateChosenNumbers(request.getChosenNumbers())) {
                return ResponseEntity.badRequest().build();
            }

            LotteryTicket ticket = lotteryTicketService.purchaseTicket(
                request.getWalletAddress(),
                request.getLotteryId(),
                request.getChosenNumbers(),
                request.getTicketPrice(),
                request.getTransactionHash(),
                request.getContractTicketId()
            );

            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update ticket matched numbers (for draw processing)
     */
    @PutMapping("/{id}/matches")
    public ResponseEntity<LotteryTicket> updateMatchedNumbers(
            @PathVariable Long id,
            @RequestBody UpdateMatchesRequest request) {
        try {
            LotteryTicket ticket = lotteryTicketService.updateMatchedNumbers(id, request.getMatchedNumbers());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark ticket as winner
     */
    @PutMapping("/{id}/winner")
    public ResponseEntity<LotteryTicket> markAsWinner(
            @PathVariable Long id,
            @RequestBody MarkWinnerRequest request) {
        try {
            LotteryTicket ticket = lotteryTicketService.markAsWinner(id, request.getPrizeAmount());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate chosen numbers format
     */
    @PostMapping("/validate-numbers")
    public ResponseEntity<ValidateNumbersResponse> validateNumbers(@RequestBody ValidateNumbersRequest request) {
        boolean isValid = lotteryTicketService.validateChosenNumbers(request.getChosenNumbers());
        return ResponseEntity.ok(new ValidateNumbersResponse(isValid));
    }

    // Request DTOs
    public static class PurchaseTicketRequest {
        private String walletAddress;
        private Long lotteryId;
        private String chosenNumbers;
        private BigDecimal ticketPrice;
        private String transactionHash;
        private String contractTicketId;

        // Getters and setters
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
        
        public Long getLotteryId() { return lotteryId; }
        public void setLotteryId(Long lotteryId) { this.lotteryId = lotteryId; }
        
        public String getChosenNumbers() { return chosenNumbers; }
        public void setChosenNumbers(String chosenNumbers) { this.chosenNumbers = chosenNumbers; }
        
        public BigDecimal getTicketPrice() { return ticketPrice; }
        public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
        
        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
        
        public String getContractTicketId() { return contractTicketId; }
        public void setContractTicketId(String contractTicketId) { this.contractTicketId = contractTicketId; }
    }

    public static class UpdateMatchesRequest {
        private Integer matchedNumbers;

        public Integer getMatchedNumbers() { return matchedNumbers; }
        public void setMatchedNumbers(Integer matchedNumbers) { this.matchedNumbers = matchedNumbers; }
    }

    public static class MarkWinnerRequest {
        private BigDecimal prizeAmount;

        public BigDecimal getPrizeAmount() { return prizeAmount; }
        public void setPrizeAmount(BigDecimal prizeAmount) { this.prizeAmount = prizeAmount; }
    }

    public static class ValidateNumbersRequest {
        private String chosenNumbers;

        public String getChosenNumbers() { return chosenNumbers; }
        public void setChosenNumbers(String chosenNumbers) { this.chosenNumbers = chosenNumbers; }
    }

    public static class ValidateNumbersResponse {
        private boolean valid;

        public ValidateNumbersResponse(boolean valid) {
            this.valid = valid;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
    }
}
