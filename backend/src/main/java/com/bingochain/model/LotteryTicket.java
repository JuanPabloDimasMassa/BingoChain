package com.bingochain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lottery_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotteryTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", unique = true, nullable = false)
    private String ticketId; // Contract ticket ID

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_lottery_id", nullable = false)
    private WeeklyLottery weeklyLottery;

    @Column(name = "chosen_numbers", nullable = false, length = 50)
    private String chosenNumbers; // JSON array of 6 numbers [1,15,33,45,67,89]

    @Column(name = "matched_numbers")
    private Integer matchedNumbers = 0;

    @Column(name = "ticket_price_paid", precision = 18, scale = 8, nullable = false)
    private BigDecimal ticketPricePaid;

    @Column(name = "is_winner")
    private Boolean isWinner = false;

    @Column(name = "prize_amount", precision = 18, scale = 8)
    private BigDecimal prizeAmount = BigDecimal.ZERO;

    @Column(name = "transaction_hash")
    private String transactionHash;

    @CreationTimestamp
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
