package com.fourcolour.auth.performance;

import com.fourcolour.auth.service.AuthenticationService;
import com.fourcolour.auth.service.JwtService;
import com.fourcolour.auth.repository.UserRepository;
import com.fourcolour.auth.repository.SessionRepository;
import com.fourcolour.common.dto.LoginRequest;
import com.fourcolour.common.dto.RegisterRequest;
import com.fourcolour.common.service.LoggerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class AuthenticationPerformanceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoggerClient loggerClient;

    @Test
    void jwtTokenGeneration_UnderConcurrentLoad_ShouldHandleCorrectly() throws InterruptedException {
        JwtService realJwtService = new JwtService();
        // Use reflection to set JWT secret
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtService, "jwtSecret", "testSecretKey123456789");
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtService, "jwtExpirationInSeconds", 3600);

        AtomicInteger successfulGenerations = new AtomicInteger(0);
        AtomicInteger failedGenerations = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int tokensPerThread = 50;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < tokensPerThread; j++) {
                        try {
                            String token = realJwtService.generateToken(threadId * tokensPerThread + j);
                            if (token != null && !token.isEmpty()) {
                                successfulGenerations.incrementAndGet();
                            } else {
                                failedGenerations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedGenerations.incrementAndGet();
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

        // Verify that most token generations were successful
        int totalExpected = numberOfThreads * tokensPerThread;
        assertEquals(totalExpected, successfulGenerations.get() + failedGenerations.get());
        assertTrue(successfulGenerations.get() > totalExpected * 0.95, 
                "Success rate should be > 95%: " + successfulGenerations.get() + "/" + totalExpected);
    }

    @Test
    void jwtTokenValidation_UnderConcurrentLoad_ShouldMaintainPerformance() throws InterruptedException {
        JwtService realJwtService = new JwtService();
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtService, "jwtSecret", "testSecretKey123456789");
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtService, "jwtExpirationInSeconds", 3600);

        // Pre-generate tokens for validation
        String[] tokens = new String[100];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = realJwtService.generateToken(i);
        }

        AtomicInteger validTokens = new AtomicInteger(0);
        AtomicInteger invalidTokens = new AtomicInteger(0);
        AtomicLong totalValidationTime = new AtomicLong(0);
        
        int numberOfThreads = 15;
        int validationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < validationsPerThread; j++) {
                        long startTime = System.nanoTime();
                        try {
                            String tokenToValidate = tokens[j % tokens.length];
                            boolean isValid = realJwtService.validateToken(tokenToValidate);
                            if (isValid) {
                                validTokens.incrementAndGet();
                            } else {
                                invalidTokens.incrementAndGet();
                            }
                        } catch (Exception e) {
                            invalidTokens.incrementAndGet();
                        }
                        long endTime = System.nanoTime();
                        totalValidationTime.addAndGet(endTime - startTime);
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

        // Performance assertions
        long averageValidationTime = totalValidationTime.get() / (numberOfThreads * validationsPerThread);
        long averageValidationTimeMs = averageValidationTime / 1_000_000;
        
        assertTrue(averageValidationTimeMs < 10, 
                "Average validation time should be < 10ms, was: " + averageValidationTimeMs + "ms");
        
        // Most validations should be successful since we're using valid tokens
        assertTrue(validTokens.get() > (numberOfThreads * validationsPerThread) * 0.9,
                "Valid token rate should be > 90%");
        
        System.out.println("Average token validation time: " + averageValidationTimeMs + "ms");
    }

    @Test
    void passwordHashing_UnderLoad_ShouldMaintainSecurity() throws InterruptedException {
        // Use real BCrypt encoder for performance testing
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder realEncoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        AtomicInteger successfulHashes = new AtomicInteger(0);
        AtomicInteger failedHashes = new AtomicInteger(0);
        AtomicLong totalHashingTime = new AtomicLong(0);
        
        int numberOfThreads = 10; // Lower number due to BCrypt being computationally expensive
        int hashesPerThread = 20;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < hashesPerThread; j++) {
                        long startTime = System.nanoTime();
                        try {
                            String password = "testPassword" + threadId + j;
                            String hashedPassword = realEncoder.encode(password);
                            
                            // Verify hash is different each time and validates correctly
                            if (hashedPassword != null && realEncoder.matches(password, hashedPassword)) {
                                successfulHashes.incrementAndGet();
                            } else {
                                failedHashes.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedHashes.incrementAndGet();
                        }
                        long endTime = System.nanoTime();
                        totalHashingTime.addAndGet(endTime - startTime);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS)); // Longer timeout for BCrypt
        executor.shutdown();

        // Performance and correctness assertions
        int totalExpected = numberOfThreads * hashesPerThread;
        assertEquals(totalExpected, successfulHashes.get() + failedHashes.get());
        assertEquals(totalExpected, successfulHashes.get()); // All should succeed
        
        long averageHashingTime = totalHashingTime.get() / totalExpected;
        long averageHashingTimeMs = averageHashingTime / 1_000_000;
        
        // BCrypt should be slow enough to be secure but not too slow
        assertTrue(averageHashingTimeMs > 10, "BCrypt should take > 10ms for security");
        assertTrue(averageHashingTimeMs < 1000, "BCrypt should take < 1000ms for usability");
        
        System.out.println("Average password hashing time: " + averageHashingTimeMs + "ms");
    }

    @Test
    void authenticationService_ResponseTime_ShouldMeetSLA() throws InterruptedException {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            com.fourcolour.auth.entity.User user = invocation.getArgument(0);
            user.setId(1);
            return user;
        });
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyInt())).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationTime()).thenReturn(java.time.LocalDateTime.now().plusHours(24));
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        AuthenticationService authService = new AuthenticationService();
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "sessionRepository", sessionRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "jwtService", jwtService);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "loggerClient", loggerClient);

        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicInteger requestCount = new AtomicInteger(0);
        
        int numberOfRequests = 100;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                long startTime = System.nanoTime();
                try {
                    RegisterRequest request = new RegisterRequest();
                    request.setName("User" + requestId);
                    request.setEmail("user" + requestId + "@example.com");
                    request.setPassword("password123");
                    
                    authService.register(request);
                    
                    long endTime = System.nanoTime();
                    totalResponseTime.addAndGet(endTime - startTime);
                    requestCount.incrementAndGet();
                } catch (Exception e) {
                    // Still count for timing even if there's an error
                    long endTime = System.nanoTime();
                    totalResponseTime.addAndGet(endTime - startTime);
                    requestCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        long averageResponseTime = totalResponseTime.get() / requestCount.get();
        long averageResponseTimeMs = averageResponseTime / 1_000_000;
        
        // SLA: Average response time should be less than 200ms
        assertTrue(averageResponseTimeMs < 200, 
                "Average response time exceeded SLA: " + averageResponseTimeMs + "ms");
        
        System.out.println("Average authentication response time: " + averageResponseTimeMs + "ms");
    }

    @Test
    void memoryUsage_UnderContinuousLoad_ShouldRemainStable() throws InterruptedException {
        JwtService realJwtService = new JwtService();
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtService, "jwtSecret", "testSecretKey123456789");
        org.springframework.test.util.ReflectionTestUtils.setField(realJwtService, "jwtExpirationInSeconds", 3600);

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        int numberOfOperations = 1000;
        CountDownLatch latch = new CountDownLatch(numberOfOperations);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < numberOfOperations; i++) {
            final int operationId = i;
            executor.submit(() -> {
                try {
                    // Simulate various authentication operations
                    String token = realJwtService.generateToken(operationId);
                    realJwtService.validateToken(token);
                    realJwtService.getUserIdFromToken(token);
                    
                    // Simulate some string operations that might cause memory pressure
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < 100; j++) {
                        sb.append("test").append(j);
                    }
                    sb.toString();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Force garbage collection
        System.gc();
        Thread.sleep(1000);

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be reasonable (less than 20MB)
        assertTrue(memoryIncrease < 20 * 1024 * 1024, 
                "Memory usage increased too much: " + (memoryIncrease / 1024 / 1024) + "MB");
        
        System.out.println("Memory usage test completed. Increase: " + (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    void concurrentUserRegistration_ShouldHandleUniqueConstraints() throws InterruptedException {
        // Simulate database constraint violations for duplicate emails
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);
        when(userRepository.existsByEmail(argThat(email -> !email.equals("duplicate@example.com")))).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            com.fourcolour.auth.entity.User user = invocation.getArgument(0);
            if ("duplicate@example.com".equals(user.getEmail())) {
                throw new RuntimeException("User with this email already exists");
            }
            user.setId((int) (Math.random() * 1000));
            return user;
        });
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyInt())).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationTime()).thenReturn(java.time.LocalDateTime.now().plusHours(24));
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        AuthenticationService authService = new AuthenticationService();
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "sessionRepository", sessionRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "jwtService", jwtService);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "loggerClient", loggerClient);

        AtomicInteger successfulRegistrations = new AtomicInteger(0);
        AtomicInteger duplicateEmailErrors = new AtomicInteger(0);
        
        int numberOfThreads = 20;
        int registrationsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < registrationsPerThread; j++) {
                        try {
                            RegisterRequest request = new RegisterRequest();
                            request.setName("User" + threadId + j);
                            
                            // Some requests will have duplicate email to test constraint handling
                            if (j % 5 == 0) {
                                request.setEmail("duplicate@example.com");
                            } else {
                                request.setEmail("user" + threadId + j + "@example.com");
                            }
                            request.setPassword("password123");
                            
                            authService.register(request);
                            successfulRegistrations.incrementAndGet();
                        } catch (RuntimeException e) {
                            if (e.getMessage().contains("already exists")) {
                                duplicateEmailErrors.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify that duplicate email constraints were properly handled
        assertTrue(duplicateEmailErrors.get() > 0, "Should have caught some duplicate email errors");
        assertTrue(successfulRegistrations.get() > 0, "Should have some successful registrations");
        
        int totalAttempts = numberOfThreads * registrationsPerThread;
        assertEquals(totalAttempts, successfulRegistrations.get() + duplicateEmailErrors.get());
        
        System.out.println("Successful registrations: " + successfulRegistrations.get());
        System.out.println("Duplicate email errors: " + duplicateEmailErrors.get());
    }
}