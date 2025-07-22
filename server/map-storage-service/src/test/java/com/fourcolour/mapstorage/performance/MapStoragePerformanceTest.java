package com.fourcolour.mapstorage.performance;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.common.service.LoggerClient;
import com.fourcolour.mapstorage.entity.Map;
import com.fourcolour.mapstorage.repository.MapRepository;
import com.fourcolour.mapstorage.service.MapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
class MapStoragePerformanceTest {

    @Mock
    private MapRepository mapRepository;

    @Mock
    private LoggerClient loggerClient;

    @Test
    void mapCreation_UnderConcurrentLoad_ShouldHandleCorrectly() throws InterruptedException {
        MapService realMapService = new MapService();
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "mapRepository", mapRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "loggerClient", loggerClient);

        AtomicInteger successfulCreations = new AtomicInteger(0);
        AtomicInteger failedCreations = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int mapsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(mapRepository.save(any(Map.class))).thenAnswer(invocation -> {
            Map map = invocation.getArgument(0);
            map.setId("507f1f77bcf86cd799439011");
            return map;
        });
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < mapsPerThread; j++) {
                        try {
                            MapRequest request = createMapRequest("user" + threadId, "Map " + j);
                            Map savedMap = realMapService.saveMap(request);
                            if (savedMap != null && savedMap.getId() != null) {
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

        // Verify that most map creations were successful
        int totalExpected = numberOfThreads * mapsPerThread;
        assertEquals(totalExpected, successfulCreations.get() + failedCreations.get());
        assertTrue(successfulCreations.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulCreations.get() + "/" + totalExpected);
    }

    @Test
    void mapRetrieval_UnderConcurrentLoad_ShouldMaintainPerformance() throws InterruptedException {
        MapService realMapService = new MapService();
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "mapRepository", mapRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "loggerClient", loggerClient);

        AtomicInteger successfulRetrievals = new AtomicInteger(0);
        AtomicInteger failedRetrievals = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int retrievalsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(mapRepository.findByUserId(anyString())).thenReturn(Arrays.asList(createTestMap()));
        when(mapRepository.findById(anyString())).thenReturn(java.util.Optional.of(createTestMap()));

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < retrievalsPerThread; j++) {
                        try {
                            List<Map> maps = realMapService.getMapsByUserId("user" + threadId);
                            if (maps != null) {
                                successfulRetrievals.incrementAndGet();
                            } else {
                                failedRetrievals.incrementAndGet();
                            }
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
    void largeMatrixHandling_UnderLoad_ShouldMaintainPerformance() throws InterruptedException {
        MapService realMapService = new MapService();
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "mapRepository", mapRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "loggerClient", loggerClient);

        AtomicInteger successfulOperations = new AtomicInteger(0);
        AtomicInteger failedOperations = new AtomicInteger(0);
        
        int numberOfThreads = 10;
        int operationsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(mapRepository.save(any(Map.class))).thenAnswer(invocation -> {
            Map map = invocation.getArgument(0);
            map.setId("507f1f77bcf86cd799439011");
            return map;
        });
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            // Create large matrix
                            int[][] largeMatrix = new int[100][100];
                            for (int row = 0; row < 100; row++) {
                                for (int col = 0; col < 100; col++) {
                                    largeMatrix[row][col] = row + col;
                                }
                            }
                            
                            MapRequest request = createMapRequestWithMatrix("user" + threadId, "Large Map " + j, largeMatrix);
                            Map savedMap = realMapService.saveMap(request);
                            if (savedMap != null && savedMap.getId() != null) {
                                successfulOperations.incrementAndGet();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedOperations.incrementAndGet();
                        }
                        Thread.sleep(10); // Small delay to simulate real usage
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that most operations were successful
        int totalExpected = numberOfThreads * operationsPerThread;
        assertEquals(totalExpected, successfulOperations.get() + failedOperations.get());
        assertTrue(successfulOperations.get() > totalExpected * 0.90, 
                "Success rate should be > 90%: " + successfulOperations.get() + "/" + totalExpected);
    }

    @Test
    void mapService_ResponseTime_ShouldMeetSLA() throws InterruptedException {
        MapService realMapService = new MapService();
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "mapRepository", mapRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "loggerClient", loggerClient);

        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        
        int numberOfRequests = 100;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        ExecutorService executor = Executors.newFixedThreadPool(20);

        // Setup mocks
        when(mapRepository.save(any(Map.class))).thenAnswer(invocation -> {
            Map map = invocation.getArgument(0);
            map.setId("507f1f77bcf86cd799439011");
            return map;
        });
        when(mapRepository.findByUserId(anyString())).thenReturn(Arrays.asList(createTestMap()));
        when(mapRepository.findById(anyString())).thenReturn(java.util.Optional.of(createTestMap()));
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // Perform map operations
                    MapRequest request = createMapRequest("user" + requestId, "Performance Test Map");
                    Map savedMap = realMapService.saveMap(request);
                    List<Map> maps = realMapService.getMapsByUserId("user" + requestId);
                    Map retrievedMap = realMapService.getMapById("507f1f77bcf86cd799439011");
                    
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;
                    
                    totalResponseTime.addAndGet(responseTime);
                    requestCount.incrementAndGet();
                    
                    // Verify response time is within SLA (e.g., 1000ms)
                    assertTrue(responseTime < 1000, "Response time " + responseTime + "ms exceeded SLA of 1000ms");
                    
                } catch (Exception e) {
                    // Log but don't fail the test for individual request failures
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Calculate average response time
        if (requestCount.get() > 0) {
            long averageResponseTime = totalResponseTime.get() / requestCount.get();
            assertTrue(averageResponseTime < 500, "Average response time " + averageResponseTime + "ms exceeded target of 500ms");
        }
    }

    @Test
    void memoryUsage_UnderContinuousLoad_ShouldRemainStable() throws InterruptedException {
        MapService realMapService = new MapService();
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "mapRepository", mapRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "loggerClient", loggerClient);

        AtomicInteger successfulOperations = new AtomicInteger(0);
        AtomicInteger failedOperations = new AtomicInteger(0);
        
        int numberOfThreads = 10;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(mapRepository.save(any(Map.class))).thenAnswer(invocation -> {
            Map map = invocation.getArgument(0);
            map.setId("507f1f77bcf86cd799439011");
            return map;
        });
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        // Record initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            MapRequest request = createMapRequest("user" + threadId, "Memory Test Map " + j);
                            Map savedMap = realMapService.saveMap(request);
                            if (savedMap != null && savedMap.getId() != null) {
                                successfulOperations.incrementAndGet();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedOperations.incrementAndGet();
                        }
                        Thread.sleep(5); // Small delay to simulate real usage
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        executor.shutdown();

        // Record final memory usage
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Verify memory usage is reasonable (less than 100MB increase)
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
                "Memory increase " + (memoryIncrease / 1024 / 1024) + "MB exceeded limit of 100MB");

        // Verify that most operations were successful
        int totalExpected = numberOfThreads * operationsPerThread;
        assertEquals(totalExpected, successfulOperations.get() + failedOperations.get());
        assertTrue(successfulOperations.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulOperations.get() + "/" + totalExpected);
    }

    @Test
    void concurrentMapDeletion_ShouldHandleCorrectly() throws InterruptedException {
        MapService realMapService = new MapService();
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "mapRepository", mapRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapService, "loggerClient", loggerClient);

        AtomicInteger successfulDeletions = new AtomicInteger(0);
        AtomicInteger failedDeletions = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int deletionsPerThread = 25;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Setup mocks
        when(mapRepository.findById(anyString())).thenReturn(java.util.Optional.of(createTestMap()));
        doNothing().when(mapRepository).deleteById(anyString());
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < deletionsPerThread; j++) {
                        try {
                            String mapId = "507f1f77bcf86cd799439011";
                            boolean deleted = realMapService.deleteMap(mapId);
                            if (deleted) {
                                successfulDeletions.incrementAndGet();
                            } else {
                                failedDeletions.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedDeletions.incrementAndGet();
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

        // Verify that most deletions were successful
        int totalExpected = numberOfThreads * deletionsPerThread;
        assertEquals(totalExpected, successfulDeletions.get() + failedDeletions.get());
        assertTrue(successfulDeletions.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulDeletions.get() + "/" + totalExpected);
    }

    private MapRequest createMapRequest(String userId, String name) {
        MapRequest request = new MapRequest();
        request.setUserId(userId);
        request.setName(name);
        request.setWidth(10);
        request.setHeight(10);
        request.setImageData("data:image/png;base64,test");
        request.setMatrix(new int[][]{{0, 1}, {1, 0}});
        return request;
    }

    private MapRequest createMapRequestWithMatrix(String userId, String name, int[][] matrix) {
        MapRequest request = new MapRequest();
        request.setUserId(userId);
        request.setName(name);
        request.setWidth(matrix.length);
        request.setHeight(matrix[0].length);
        request.setImageData("data:image/png;base64,test");
        request.setMatrix(matrix);
        return request;
    }

    private Map createTestMap() {
        Map map = new Map();
        map.setId("507f1f77bcf86cd799439011");
        map.setUserId("user123");
        map.setName("Test Map");
        map.setWidth(10);
        map.setHeight(10);
        map.setImageData("data:image/png;base64,test");
        map.setMatrix(new int[][]{{0, 1}, {1, 0}});
        map.setCreatedAt(LocalDateTime.now());
        map.setUpdatedAt(LocalDateTime.now());
        return map;
    }
} 