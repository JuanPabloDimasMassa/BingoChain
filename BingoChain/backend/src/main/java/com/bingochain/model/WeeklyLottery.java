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
@Table(name = "weekly_lotteries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyLottery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_address", unique = true, nullable = false)
    private String contractAddress;

    @Column(name = "lottery_name", nullable = false)
    private String lotteryName;

    @Column(name = "ticket_price", precision = 18, scale = 8, nullable = false)
    private BigDecimal ticketPrice;

    @Column(name = "prize_pool", precision = 18, scale = 8)
    private BigDecimal prizePool = BigDecimal.ZERO;

    @Column(name = "total_tickets")
    private Integer totalTickets = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LotteryStatus status = LotteryStatus.TICKET_SALES;

    @Column(name = "sales_start_time", nullable = false)
    private LocalDateTime salesStartTime;

    @Column(name = "sales_end_time", nullable = false)
    private LocalDateTime salesEndTime;

    @Column(name = "current_draw_day")
    private Integer currentDrawDay = 0;

    @Column(name = "next_draw_time")
    private LocalDateTime nextDrawTime;

    @Column(name = "drawn_numbers", length = 500)
    private String drawnNumbers; // JSON array of drawn numbers

    @Column(name = "winner_addresses", length = 1000)
    private String winnerAddresses; // JSON array of winner addresses

    @Column(name = "prizes_distributed")
    private Boolean prizesDistributed = false;

    @OneToMany(mappedBy = "weeklyLottery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LotteryTicket> tickets;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LotteryStatus {
        TICKET_SALES,
        DRAWING_PHASE,
        COMPLETED,
        CANCELLED
    }
}
