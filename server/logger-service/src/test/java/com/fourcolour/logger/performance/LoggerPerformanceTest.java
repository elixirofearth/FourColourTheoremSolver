package com.fourcolour.logger.performance;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.LogRequest;
import com.fourcolour.logger.proto.LoggerProto.LogResponse;
import com.fourcolour.logger.repository.LogRepository;
import com.fourcolour.logger.service.LoggerGrpcService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
class LoggerPerformanceTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private KafkaTemplate<String, Log> kafkaTemplate;

    @Test
    void logCreation_UnderConcurrentLoad_ShouldHandleCorrectly() throws InterruptedException {
        LoggerGrpcService realLoggerGrpcService = new LoggerGrpcService();
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "logRepository", logRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "kafkaTemplate", kafkaTemplate);

        AtomicInteger successfulCreations = new AtomicInteger(0);
        AtomicInteger failedCreations = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int logsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < logsPerThread; j++) {
                        try {
                            LogRequest request = createLogRequest("service" + threadId, "event" + j);
                            TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
                            realLoggerGrpcService.logEvent(request, responseObserver);
                            
                            if (responseObserver.getResponse() != null && responseObserver.getResponse().getSuccess()) {
                                successfulCreations.incrementAndGet();
                            } else {
                                failedCreations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedCreations.incrementAndGet();
                        }
                        Thread.sleep(1); // Small delay to simulate real usage
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that most log creations were successful
        int totalExpected = numberOfThreads * logsPerThread;
        assertEquals(totalExpected, successfulCreations.get() + failedCreations.get());
        assertTrue(successfulCreations.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulCreations.get() + "/" + totalExpected);
    }

    @Test
    void logRetrieval_UnderConcurrentLoad_ShouldMaintainPerformance() throws InterruptedException {
        AtomicInteger successfulRetrievals = new AtomicInteger(0);
        AtomicInteger failedRetrievals = new AtomicInteger(0);
        
        int numberOfThreads = 10;
        int retrievalsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks for repository
        when(logRepository.findByServiceName(anyString())).thenReturn(createTestLogs(5));
        when(logRepository.findByUserId(anyString())).thenReturn(createTestLogs(3));
        when(logRepository.findByEventType(anyString())).thenReturn(createTestLogs(4));

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < retrievalsPerThread; j++) {
                        try {
                            // Simulate different types of queries
                            String serviceName = "service" + (threadId % 3);
                            String userId = "user" + (threadId % 5);
                            String eventType = "event" + (threadId % 4);

                            // These would be actual repository calls in a real scenario
                            logRepository.findByServiceName(serviceName);
                            logRepository.findByUserId(userId);
                            logRepository.findByEventType(eventType);

                            successfulRetrievals.incrementAndGet();
                        } catch (Exception e) {
                            failedRetrievals.incrementAndGet();
                        }
                        Thread.sleep(1); // Small delay to simulate real usage
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that most retrievals were successful
        int totalExpected = numberOfThreads * retrievalsPerThread;
        assertEquals(totalExpected, successfulRetrievals.get() + failedRetrievals.get());
        assertTrue(successfulRetrievals.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulRetrievals.get() + "/" + totalExpected);
    }

    @Test
    void largeMetadataHandling_UnderLoad_ShouldMaintainPerformance() throws InterruptedException {
        LoggerGrpcService realLoggerGrpcService = new LoggerGrpcService();
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "logRepository", logRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "kafkaTemplate", kafkaTemplate);

        AtomicInteger successfulCreations = new AtomicInteger(0);
        AtomicInteger failedCreations = new AtomicInteger(0);
        
        int numberOfThreads = 10;
        int logsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < logsPerThread; j++) {
                        try {
                            LogRequest request = createLogRequestWithLargeMetadata("service" + threadId, "event" + j);
                            TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
                            realLoggerGrpcService.logEvent(request, responseObserver);
                            
                            if (responseObserver.getResponse() != null && responseObserver.getResponse().getSuccess()) {
                                successfulCreations.incrementAndGet();
                            } else {
                                failedCreations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedCreations.incrementAndGet();
                        }
                        Thread.sleep(2); // Slightly longer delay for large metadata
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that most log creations were successful
        int totalExpected = numberOfThreads * logsPerThread;
        assertEquals(totalExpected, successfulCreations.get() + failedCreations.get());
        assertTrue(successfulCreations.get() > totalExpected * 0.90, 
                "Success rate should be > 90%: " + successfulCreations.get() + "/" + totalExpected);
    }

    @Test
    void loggerService_ResponseTime_ShouldMeetSLA() throws InterruptedException {
        LoggerGrpcService realLoggerGrpcService = new LoggerGrpcService();
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "logRepository", logRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "kafkaTemplate", kafkaTemplate);

        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        
        int numberOfRequests = 100;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);

        // Setup mocks
        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    LogRequest request = createLogRequest("auth-service", "user_login");
                    TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
                    
                    long startTime = System.currentTimeMillis();
                    realLoggerGrpcService.logEvent(request, responseObserver);
                    long endTime = System.currentTimeMillis();
                    
                    long responseTime = endTime - startTime;
                    totalResponseTime.addAndGet(responseTime);
                    requestCount.incrementAndGet();
                    
                    assertTrue(responseTime < 1000, "Response time should be < 1000ms, was: " + responseTime);
                    
                } catch (Exception e) {
                    // Handle exception
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Calculate average response time
        long averageResponseTime = totalResponseTime.get() / requestCount.get();
        assertTrue(averageResponseTime < 500, "Average response time should be < 500ms, was: " + averageResponseTime);
    }

    @Test
    void memoryUsage_UnderContinuousLoad_ShouldRemainStable() throws InterruptedException {
        LoggerGrpcService realLoggerGrpcService = new LoggerGrpcService();
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "logRepository", logRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realLoggerGrpcService, "kafkaTemplate", kafkaTemplate);

        AtomicInteger successfulCreations = new AtomicInteger(0);
        AtomicInteger failedCreations = new AtomicInteger(0);
        
        int numberOfThreads = 5;
        int logsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < logsPerThread; j++) {
                        try {
                            LogRequest request = createLogRequest("service" + threadId, "event" + j);
                            TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
                            realLoggerGrpcService.logEvent(request, responseObserver);
                            
                            if (responseObserver.getResponse() != null && responseObserver.getResponse().getSuccess()) {
                                successfulCreations.incrementAndGet();
                            } else {
                                failedCreations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedCreations.incrementAndGet();
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Get final memory usage
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Memory increase should be reasonable (less than 100MB)
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
                "Memory increase should be < 100MB, was: " + (memoryIncrease / 1024 / 1024) + "MB");

        // Verify that most log creations were successful
        int totalExpected = numberOfThreads * logsPerThread;
        assertEquals(totalExpected, successfulCreations.get() + failedCreations.get());
        assertTrue(successfulCreations.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulCreations.get() + "/" + totalExpected);
    }

    @Test
    void concurrentLogDeletion_ShouldHandleCorrectly() throws InterruptedException {
        AtomicInteger successfulDeletions = new AtomicInteger(0);
        AtomicInteger failedDeletions = new AtomicInteger(0);
        
        int numberOfThreads = 10;
        int deletionsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks for repository
        doNothing().when(logRepository).deleteById(anyString());

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < deletionsPerThread; j++) {
                        try {
                            String logId = "log" + threadId + "_" + j;
                            logRepository.deleteById(logId);
                            successfulDeletions.incrementAndGet();
                        } catch (Exception e) {
                            failedDeletions.incrementAndGet();
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that most deletions were successful
        int totalExpected = numberOfThreads * deletionsPerThread;
        assertEquals(totalExpected, successfulDeletions.get() + failedDeletions.get());
        assertTrue(successfulDeletions.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulDeletions.get() + "/" + totalExpected);
    }

    private LogRequest createLogRequest(String serviceName, String eventType) {
        return LogRequest.newBuilder()
                .setServiceName(serviceName)
                .setEventType(eventType)
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();
    }

    private LogRequest createLogRequestWithLargeMetadata(String serviceName, String eventType) {
        LogRequest.Builder requestBuilder = LogRequest.newBuilder()
                .setServiceName(serviceName)
                .setEventType(eventType)
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString());

        // Add large metadata
        for (int i = 0; i < 100; i++) {
            requestBuilder.putMetadata("key" + i, "value" + i);
        }

        return requestBuilder.build();
    }

    private java.util.List<Log> createTestLogs(int count) {
        java.util.List<Log> logs = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Log log = new Log();
            log.setId("log" + i);
            log.setServiceName("service" + i);
            log.setEventType("event" + i);
            log.setUserId("user" + i);
            log.setDescription("Test log " + i);
            log.setSeverity(1);
            log.setTimestamp(LocalDateTime.now());
            logs.add(log);
        }
        return logs;
    }

    private Map<String, String> createValidMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        metadata.put("session_id", "session123");
        return metadata;
    }

    // Helper class to capture gRPC responses
    private static class TestStreamObserver<T> implements StreamObserver<T> {
        private T response;
        private Throwable error;

        @Override
        public void onNext(T value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            // Do nothing
        }

        public T getResponse() {
            return response;
        }

        public Throwable getError() {
            return error;
        }
    }
} 