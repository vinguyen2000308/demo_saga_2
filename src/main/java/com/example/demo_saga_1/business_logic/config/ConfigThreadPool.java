package com.example.demo_saga_1.business_logic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ConfigThreadPool {

    @Bean
    public ExecutorService getExecutorService() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        return executorService;
    }
}
