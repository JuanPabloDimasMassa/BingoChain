package com.bingochain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class BingoChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(BingoChainApplication.class, args);
    }
}
