package com.bingochain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "draw_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_lottery_id", nullable = false)
    private WeeklyLottery weeklyLottery;

    @Column(name = "draw_day", nullable = false)
    private Integer drawDay; // 1-6

    @Column(name = "drawn_number", nullable = false)
    private Integer drawnNumber; // 1-100

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @CreationTimestamp
    @Column(name = "drawn_at", nullable = false, updatable = false)
    private LocalDateTime drawnAt;

    @Column(name = "transaction_hash")
    private String transactionHash;

    @Column(name = "block_number")
    private Long blockNumber;
}
