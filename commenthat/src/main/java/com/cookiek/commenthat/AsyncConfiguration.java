package com.cookiek.commenthat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // 기본적으로 유지할 스레드 수
        executor.setMaxPoolSize(50);   // 최대 생성할 수 있는 스레드 수
        executor.setQueueCapacity(100); // 대기 중인 요청 작업을 담을 수 있는 용량
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}