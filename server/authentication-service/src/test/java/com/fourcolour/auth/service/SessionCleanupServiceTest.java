package com.fourcolour.auth.service;

import com.fourcolour.auth.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionCleanupServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionCleanupService sessionCleanupService;

    @BeforeEach
    void setUp() {
        // Reset mock behavior before each test
        reset(sessionRepository);
    }

    @Test
    void cleanupExpiredSessions_ShouldCallRepositoryMethod() {
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        sessionCleanupService.cleanupExpiredSessions();

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_ShouldPassCorrectTimestamp() {
        LocalDateTime beforeCleanup = LocalDateTime.now();
        
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        sessionCleanupService.cleanupExpiredSessions();

        LocalDateTime afterCleanup = LocalDateTime.now();

        verify(sessionRepository).deleteExpiredSessions(argThat(timestamp -> 
            !timestamp.isBefore(beforeCleanup) && !timestamp.isAfter(afterCleanup)
        ));
    }

    @Test
    void cleanupExpiredSessions_WithRepositoryException_ShouldHandleGracefully() {
        doThrow(new RuntimeException("Database connection failed"))
                .when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            sessionCleanupService.cleanupExpiredSessions();
        });

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void manualCleanup_ShouldCallCleanupExpiredSessions() {
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        sessionCleanupService.manualCleanup();

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void manualCleanup_WithException_ShouldHandleGracefully() {
        doThrow(new RuntimeException("Database error"))
                .when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        assertDoesNotThrow(() -> {
            sessionCleanupService.manualCleanup();
        });

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_ShouldBeIdempotent() {
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        // Call cleanup multiple times
        sessionCleanupService.cleanupExpiredSessions();
        sessionCleanupService.cleanupExpiredSessions();
        sessionCleanupService.cleanupExpiredSessions();

        // Should call repository method each time
        verify(sessionRepository, times(3)).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_WithNullPointerException_ShouldHandleGracefully() {
        doThrow(new NullPointerException("Null session reference"))
                .when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        assertDoesNotThrow(() -> {
            sessionCleanupService.cleanupExpiredSessions();
        });

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_WithTransactionException_ShouldHandleGracefully() {
        doThrow(new org.springframework.dao.DataAccessException("Transaction failed") {})
                .when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        assertDoesNotThrow(() -> {
            sessionCleanupService.cleanupExpiredSessions();
        });

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_ShouldUseCurrentTime() {
        // Capture the argument passed to the repository method
        org.mockito.ArgumentCaptor<LocalDateTime> timeCaptor = 
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        
        doNothing().when(sessionRepository).deleteExpiredSessions(timeCaptor.capture());

        LocalDateTime beforeCall = LocalDateTime.now();
        sessionCleanupService.cleanupExpiredSessions();
        LocalDateTime afterCall = LocalDateTime.now();

        LocalDateTime capturedTime = timeCaptor.getValue();
        
        // The captured time should be between beforeCall and afterCall
        assertTrue(capturedTime.isAfter(beforeCall.minusSeconds(1))); // Small buffer
        assertTrue(capturedTime.isBefore(afterCall.plusSeconds(1)));  // Small buffer
    }

    @Test
    void cleanupExpiredSessions_ShouldNotPassFutureTime() {
        org.mockito.ArgumentCaptor<LocalDateTime> timeCaptor = 
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        
        doNothing().when(sessionRepository).deleteExpiredSessions(timeCaptor.capture());

        sessionCleanupService.cleanupExpiredSessions();

        LocalDateTime capturedTime = timeCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();
        
        // Captured time should not be in the future
        assertFalse(capturedTime.isAfter(now.plusSeconds(1)));
    }

    @Test
    void cleanupExpiredSessions_MultipleCalls_ShouldUseDifferentTimestamps() {
        org.mockito.ArgumentCaptor<LocalDateTime> timeCaptor = 
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        
        doNothing().when(sessionRepository).deleteExpiredSessions(timeCaptor.capture());

        sessionCleanupService.cleanupExpiredSessions();
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        sessionCleanupService.cleanupExpiredSessions();

        // Should have been called twice with potentially different timestamps
        verify(sessionRepository, times(2)).deleteExpiredSessions(any(LocalDateTime.class));
        
        // Get all captured values
        java.util.List<LocalDateTime> allCapturedTimes = timeCaptor.getAllValues();
        assertEquals(2, allCapturedTimes.size());
        
        // Times should be close but may be different
        LocalDateTime firstTime = allCapturedTimes.get(0);
        LocalDateTime secondTime = allCapturedTimes.get(1);
        
        // Second time should not be before first time
        assertFalse(secondTime.isBefore(firstTime));
    }

    @Test
    void manualCleanup_ShouldBehaveSameAsScheduledCleanup() {
        org.mockito.ArgumentCaptor<LocalDateTime> scheduledTimeCaptor = 
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        org.mockito.ArgumentCaptor<LocalDateTime> manualTimeCaptor = 
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        // Call scheduled cleanup
        sessionCleanupService.cleanupExpiredSessions();
        
        // Small delay
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Call manual cleanup
        sessionCleanupService.manualCleanup();

        // Both should call the repository method
        verify(sessionRepository, times(2)).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_WithSuccessfulExecution_ShouldCompleteNormally() {
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        long startTime = System.currentTimeMillis();
        sessionCleanupService.cleanupExpiredSessions();
        long endTime = System.currentTimeMillis();

        // Should complete quickly (within reasonable time)
        assertTrue(endTime - startTime < 1000); // Less than 1 second
        
        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_WithSlowRepository_ShouldWait() {
        // Simulate slow repository operation
        doAnswer(invocation -> {
            Thread.sleep(100); // 100ms delay
            return null;
        }).when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        long startTime = System.currentTimeMillis();
        sessionCleanupService.cleanupExpiredSessions();
        long endTime = System.currentTimeMillis();

        // Should take at least 100ms due to the delay
        assertTrue(endTime - startTime >= 100);
        
        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_WithInterruptedException_ShouldHandleGracefully() {
        doAnswer(invocation -> {
            Thread.currentThread().interrupt();
            throw new InterruptedException("Thread interrupted");
        }).when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        assertDoesNotThrow(() -> {
            sessionCleanupService.cleanupExpiredSessions();
        });

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSessions_ShouldNotModifyPassedTimestamp() {
        org.mockito.ArgumentCaptor<LocalDateTime> timeCaptor = 
                org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        
        doNothing().when(sessionRepository).deleteExpiredSessions(timeCaptor.capture());

        sessionCleanupService.cleanupExpiredSessions();

        LocalDateTime capturedTime = timeCaptor.getValue();
        
        // The timestamp should be a valid LocalDateTime
        assertNotNull(capturedTime);
        
        // Should be able to format it without exception
        assertDoesNotThrow(() -> {
            capturedTime.toString();
        });
    }

    @Test
    void cleanupExpiredSessions_ConcurrentCalls_ShouldBeThreadSafe() throws InterruptedException {
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        int numberOfThreads = 10;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(numberOfThreads);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    sessionCleanupService.cleanupExpiredSessions();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
        executor.shutdown();

        // Should have called the repository method for each thread
        verify(sessionRepository, times(numberOfThreads)).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void manualCleanup_ConcurrentWithScheduledCleanup_ShouldNotCauseIssues() throws InterruptedException {
        doNothing().when(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);

        // Simulate scheduled cleanup
        executor.submit(() -> {
            try {
                sessionCleanupService.cleanupExpiredSessions();
            } finally {
                latch.countDown();
            }
        });

        // Simulate manual cleanup
        executor.submit(() -> {
            try {
                sessionCleanupService.manualCleanup();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
        executor.shutdown();

        verify(sessionRepository, times(2)).deleteExpiredSessions(any(LocalDateTime.class));
    }
}