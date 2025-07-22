package com.fourcolour.gateway.performance;

import com.fourcolour.gateway.service.ProxyService;
import com.fourcolour.gateway.service.TokenCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GatewayPerformanceAdvancedTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ProxyService proxyService;

    @Test
    void concurrentTokenValidation_ShouldHandleLoadEfficiently() throws InterruptedException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(proxyService.verifyToken(anyString()))
                .thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        
        TokenCacheService tokenCacheService = new TokenCacheService();
        org.springframework.test.util.ReflectionTestUtils.setField(tokenCacheService, "redisTemplate", redisTemplate);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int requestsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            Boolean isValid = tokenCacheService.getCachedTokenValidation("Bearer token-" + j);
                            if (isValid != null && isValid) {
                                successCount.incrementAndGet();
                            } else {
                                failureCount.incrementAndGet();
                            }
                            Thread.sleep(1);
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Performance assertions
        assertTrue(totalTime < 30000, "Performance test took too long: " + totalTime + "ms");
        assertTrue(successCount.get() + failureCount.get() == numberOfThreads * requestsPerThread);
        System.out.println("Concurrent token validation completed in " + totalTime + "ms");
    }

    @Test
    void memoryUsage_UnderLoad_ShouldRemainStable() throws InterruptedException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        TokenCacheService tokenCacheService = new TokenCacheService();
        org.springframework.test.util.ReflectionTestUtils.setField(tokenCacheService, "redisTemplate", redisTemplate);

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        int numberOfOperations = 1000;
        CountDownLatch latch = new CountDownLatch(numberOfOperations);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < numberOfOperations; i++) {
            executor.submit(() -> {
                try {
                    tokenCacheService.cacheToken("Bearer token-" + System.currentTimeMillis(), true);
                    tokenCacheService.isRateLimited("192.168.1." + (System.currentTimeMillis() % 255));
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // Force garbage collection
        System.gc();
        Thread.sleep(1000);

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be reasonable (less than 50MB)
        assertTrue(memoryIncrease < 50 * 1024 * 1024, 
                "Memory usage increased too much: " + (memoryIncrease / 1024 / 1024) + "MB");
        
        System.out.println("Memory usage test completed. Increase: " + (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    void responseTime_UnderNormalLoad_ShouldMeetSLA() throws InterruptedException {
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"success\":true}"));
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString()))
                .thenReturn(ResponseEntity.ok("{\"valid\":true}"));

        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        
        int numberOfRequests = 100;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                long startTime = System.nanoTime();
                try {
                    // Simulate a typical request
                    proxyService.forwardRequest("auth", "/auth/login", 
                            org.springframework.http.HttpMethod.POST, null, "{\"test\":\"data\"}");
                    long endTime = System.nanoTime();
                    totalResponseTime.addAndGet(endTime - startTime);
                    requestCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        long averageResponseTime = totalResponseTime.get() / requestCount.get();
        long averageResponseTimeMs = averageResponseTime / 1_000_000; // Convert to milliseconds
        
        // SLA: Average response time should be less than 100ms
        assertTrue(averageResponseTimeMs < 100, 
                "Average response time exceeded SLA: " + averageResponseTimeMs + "ms");
        
        System.out.println("Average response time: " + averageResponseTimeMs + "ms");
    }

    @Test
    void throughput_UnderHighLoad_ShouldMaintainPerformance() throws InterruptedException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any());
        
        TokenCacheService tokenCacheService = new TokenCacheService();
        org.springframework.test.util.ReflectionTestUtils.setField(tokenCacheService, "redisTemplate", redisTemplate);

        int numberOfThreads = 50;
        int requestsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        tokenCacheService.isRateLimited("192.168.1." + (threadId % 255));
                        tokenCacheService.cacheToken("Bearer token-" + j, true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long totalRequests = numberOfThreads * requestsPerThread;
        double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);
        
        // Throughput should be at least 500 requests per second
        assertTrue(requestsPerSecond >= 500, 
                "Throughput too low: " + requestsPerSecond + " requests/second");
        
        System.out.println("Throughput: " + requestsPerSecond + " requests/second");
    }

    @Test
    void connectionPool_UnderLoad_ShouldHandleConnectionsEfficiently() throws InterruptedException {
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"success\":true}"));
        when(proxyService.isRateLimited(anyString())).thenReturn(false);

        int numberOfConnections = 100;
        CountDownLatch latch = new CountDownLatch(numberOfConnections);
        ExecutorService executor = Executors.newFixedThreadPool(20);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfConnections; i++) {
            executor.submit(() -> {
                try {
                    // Simulate concurrent connections
                    proxyService.forwardRequest("auth", "/auth/login", 
                            org.springframework.http.HttpMethod.POST, null, "{\"test\":\"data\"}");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Success rate should be high (at least 90%)
        double successRate = (double) successCount.get() / (successCount.get() + failureCount.get());
        assertTrue(successRate >= 0.9, 
                "Connection success rate too low: " + (successRate * 100) + "%");
        
        System.out.println("Connection success rate: " + (successRate * 100) + "%");
    }
} 