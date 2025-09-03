package com.bingochain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class BlockchainConfig {

    @Value("${blockchain.ethereum.network-url}")
    private String networkUrl;

    @Value("${blockchain.ethereum.gas-price}")
    private BigInteger gasPrice;

    @Value("${blockchain.ethereum.gas-limit}")
    private BigInteger gasLimit;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(networkUrl));
    }

    @Bean
    public StaticGasProvider gasProvider() {
        return new StaticGasProvider(gasPrice, gasLimit);
    }

    @Bean
    public DefaultGasProvider defaultGasProvider() {
        return new DefaultGasProvider();
    }
}
