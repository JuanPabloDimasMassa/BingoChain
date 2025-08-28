package com.bingochain.service;

import com.bingochain.model.LotteryTicket;
import com.bingochain.model.WeeklyLottery;
import com.bingochain.repository.LotteryTicketRepository;
import com.bingochain.repository.WeeklyLotteryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LotteryTicketService {

    @Autowired
    private LotteryTicketRepository lotteryTicketRepository;

    @Autowired
    private WeeklyLotteryRepository weeklyLotteryRepository;

    /**
     * Create a new lottery ticket
     */
    public LotteryTicket createTicket(LotteryTicket ticket) {
        ticket.setPurchasedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        return lotteryTicketRepository.save(ticket);
    }

    /**
     * Get ticket by ID
     */
    public Optional<LotteryTicket> getTicketById(Long id) {
        return lotteryTicketRepository.findById(id);
    }

    /**
     * Get ticket by contract ticket ID
     */
    public Optional<LotteryTicket> getTicketByTicketId(String ticketId) {
        return lotteryTicketRepository.findByTicketId(ticketId);
    }

    /**
     * Get all tickets for a player
     */
    public List<LotteryTicket> getPlayerTickets(String walletAddress) {
        return lotteryTicketRepository.findByWalletAddressOrderByPurchasedAtDesc(walletAddress);
    }

    /**
     * Get player tickets for a specific lottery
     */
    public List<LotteryTicket> getPlayerTicketsForLottery(String walletAddress, Long lotteryId) {
        return lotteryTicketRepository.findByWalletAddressAndWeeklyLotteryId(walletAddress, lotteryId);
    }

    /**
     * Get all tickets for a lottery
     */
    public List<LotteryTicket> getLotteryTickets(Long lotteryId) {
        return lotteryTicketRepository.findByWeeklyLotteryId(lotteryId);
    }

    /**
     * Get winning tickets for a lottery
     */
    public List<LotteryTicket> getWinningTickets(Long lotteryId) {
        return lotteryTicketRepository.findWinningTicketsByLotteryId(lotteryId);
    }

    /**
     * Get all winning tickets
     */
    public List<LotteryTicket> getAllWinningTickets() {
        return lotteryTicketRepository.findAllWinningTickets();
    }

    /**
     * Update ticket matched numbers
     */
    public LotteryTicket updateMatchedNumbers(Long ticketId, Integer matchedNumbers) {
        Optional<LotteryTicket> ticketOpt = lotteryTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            LotteryTicket ticket = ticketOpt.get();
            ticket.setMatchedNumbers(matchedNumbers);
            ticket.setUpdatedAt(LocalDateTime.now());
            return lotteryTicketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    /**
     * Mark ticket as winner
     */
    public LotteryTicket markAsWinner(Long ticketId, BigDecimal prizeAmount) {
        Optional<LotteryTicket> ticketOpt = lotteryTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            LotteryTicket ticket = ticketOpt.get();
            ticket.setIsWinner(true);
            ticket.setPrizeAmount(prizeAmount);
            ticket.setUpdatedAt(LocalDateTime.now());
            return lotteryTicketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    /**
     * Purchase ticket for lottery
     */
    public LotteryTicket purchaseTicket(String walletAddress, Long lotteryId, String chosenNumbers, 
                                       BigDecimal ticketPrice, String transactionHash, String contractTicketId) {
        
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryRepository.findById(lotteryId);
        if (!lotteryOpt.isPresent()) {
            throw new RuntimeException("Lottery not found with ID: " + lotteryId);
        }

        WeeklyLottery lottery = lotteryOpt.get();
        
        // Validate lottery is in ticket sales phase
        if (lottery.getStatus() != WeeklyLottery.LotteryStatus.TICKET_SALES) {
            throw new RuntimeException("Lottery is not in ticket sales phase");
        }

        // Validate sales period
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(lottery.getSalesStartTime()) || now.isAfter(lottery.getSalesEndTime())) {
            throw new RuntimeException("Ticket sales period has ended");
        }

        // Create ticket
        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketId(contractTicketId);
        ticket.setWalletAddress(walletAddress);
        ticket.setWeeklyLottery(lottery);
        ticket.setChosenNumbers(chosenNumbers);
        ticket.setTicketPricePaid(ticketPrice);
        ticket.setTransactionHash(transactionHash);
        ticket.setMatchedNumbers(0);
        ticket.setIsWinner(false);
        ticket.setPrizeAmount(BigDecimal.ZERO);

        return createTicket(ticket);
    }

    /**
     * Get player statistics
     */
    public PlayerStats getPlayerStatistics(String walletAddress) {
        Long totalTickets = lotteryTicketRepository.countTicketsByPlayer(walletAddress);
        Long winningTickets = lotteryTicketRepository.countWinsByPlayer(walletAddress);
        Double totalSpent = lotteryTicketRepository.getTotalSpentByPlayer(walletAddress);
        Double totalWon = lotteryTicketRepository.getTotalWonByPlayer(walletAddress);

        return new PlayerStats(
            totalTickets != null ? totalTickets : 0L,
            winningTickets != null ? winningTickets : 0L,
            totalSpent != null ? totalSpent : 0.0,
            totalWon != null ? totalWon : 0.0
        );
    }

    /**
     * Get tickets by lottery and number of matches
     */
    public List<LotteryTicket> getTicketsByMatches(Long lotteryId, Integer matchedNumbers) {
        return lotteryTicketRepository.findTicketsByLotteryAndMatches(lotteryId, matchedNumbers);
    }

    /**
     * Validate chosen numbers format
     */
    public boolean validateChosenNumbers(String chosenNumbers) {
        try {
            // Parse JSON array and validate format
            // Expected format: [1,15,33,45,67,89]
            if (!chosenNumbers.startsWith("[") || !chosenNumbers.endsWith("]")) {
                return false;
            }
            
            String[] numbers = chosenNumbers.substring(1, chosenNumbers.length() - 1).split(",");
            if (numbers.length != 6) {
                return false;
            }
            
            for (String number : numbers) {
                int num = Integer.parseInt(number.trim());
                if (num < 1 || num > 100) {
                    return false;
                }
            }
            
            // Check for duplicates
            java.util.Set<Integer> uniqueNumbers = new java.util.HashSet<>();
            for (String number : numbers) {
                int num = Integer.parseInt(number.trim());
                if (!uniqueNumbers.add(num)) {
                    return false; // Duplicate found
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper class for player statistics
     */
    public static class PlayerStats {
        private final Long totalTickets;
        private final Long winningTickets;
        private final Double totalSpent;
        private final Double totalWon;

        public PlayerStats(Long totalTickets, Long winningTickets, Double totalSpent, Double totalWon) {
            this.totalTickets = totalTickets;
            this.winningTickets = winningTickets;
            this.totalSpent = totalSpent;
            this.totalWon = totalWon;
        }

        public Long getTotalTickets() { return totalTickets; }
        public Long getWinningTickets() { return winningTickets; }
        public Double getTotalSpent() { return totalSpent; }
        public Double getTotalWon() { return totalWon; }
        
        public Double getWinPercentage() {
            return totalTickets > 0 ? (winningTickets.doubleValue() / totalTickets.doubleValue()) * 100 : 0.0;
        }
        
        public Double getReturnOnInvestment() {
            return totalSpent > 0 ? (totalWon / totalSpent) * 100 : 0.0;
        }
    }
}
