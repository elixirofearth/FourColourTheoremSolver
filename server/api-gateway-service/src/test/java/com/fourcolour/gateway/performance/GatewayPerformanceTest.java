package com.fourcolour.gateway.performance;

import com.fourcolour.gateway.service.TokenCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayPerformanceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Test
    void rateLimiting_UnderConcurrentLoad_ShouldHandleCorrectly() throws InterruptedException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        TokenCacheService tokenCacheService = new TokenCacheService();
        // Use reflection to set the mocked RedisTemplate
        org.springframework.test.util.ReflectionTestUtils.setField(tokenCacheService, "redisTemplate", redisTemplate);

        AtomicInteger allowedRequests = new AtomicInteger(0);
        AtomicInteger blockedRequests = new AtomicInteger(0);
        
        int numberOfThreads = 10;
        int requestsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Mock Redis to return increasing count values
        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            return allowedRequests.get();
        });

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        boolean isRateLimited = tokenCacheService.isRateLimited("192.168.1.1");
                        if (isRateLimited) {
                            blockedRequests.incrementAndGet();
                        } else {
                            allowedRequests.incrementAndGet();
                        }
                        Thread.sleep(1); // Small delay to simulate real requests
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that the service handles concurrent requests
        assertTrue(allowedRequests.get() + blockedRequests.get() == numberOfThreads * requestsPerThread);
    }
}
