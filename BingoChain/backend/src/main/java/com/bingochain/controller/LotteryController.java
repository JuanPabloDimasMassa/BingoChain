package com.bingochain.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080"})
public class LotteryController {

    @GetMapping("/lotteries")
    public List<Map<String, Object>> getLotteries() {
        List<Map<String, Object>> lotteries = new ArrayList<>();
        
        Map<String, Object> lottery = new HashMap<>();
        lottery.put("id", 1);
        lottery.put("lotteryName", "Sorteo Semanal #1");
        lottery.put("contractAddress", "0x345cA3e014Aaf5dcA488057592ee47305D9B3e10");
        lottery.put("ticketPrice", 0.01);
        lottery.put("status", "TICKET_SALES");
        lottery.put("salesStartTime", "2025-09-02T15:53:45");
        lottery.put("salesEndTime", "2025-09-09T15:53:45");
        lottery.put("nextDrawTime", "2025-09-09T15:53:45");
        lottery.put("prizePool", 0.0);
        lottery.put("totalTickets", 0);
        
        lotteries.add(lottery);
        
        return lotteries;
    }
}

