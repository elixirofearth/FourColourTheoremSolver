package com.fourcolour.logger.service;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogConsumerServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogConsumerService logConsumerService;

    private Log testLog;

    @BeforeEach
    void setUp() {
        testLog = new Log();
        testLog.setId("507f1f77bcf86cd799439011");
        testLog.setServiceName("auth-service");
        testLog.setEventType("user_login");
        testLog.setUserId("user123");
        testLog.setDescription("User logged in successfully");
        testLog.setSeverity(1);
        testLog.setTimestamp(LocalDateTime.now());
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        testLog.setMetadata(metadata);
    }

    @Test
    void handleAuthLog_WithValidLog_ShouldSaveSuccessfully() {
        when(logRepository.save(any(Log.class))).thenReturn(testLog);

        logConsumerService.handleAuthLog(testLog);

        verify(logRepository).save(testLog);
    }

    @Test
    void handleAuthLog_WithNullValues_ShouldHandleGracefully() {
        Log nullLog = new Log();
        nullLog.setServiceName(null);
        nullLog.setEventType(null);
        nullLog.setUserId(null);
        nullLog.setDescription(null);
        nullLog.setSeverity(null);
        nullLog.setTimestamp(LocalDateTime.now());
        nullLog.setMetadata(null);

        when(logRepository.save(any(Log.class))).thenReturn(nullLog);

        logConsumerService.handleAuthLog(nullLog);

        verify(logRepository).save(nullLog);
    }

    @Test
    void handleAuthLog_WithEmptyValues_ShouldHandleGracefully() {
        Log emptyLog = new Log();
        emptyLog.setServiceName("");
        emptyLog.setEventType("");
        emptyLog.setUserId("");
        emptyLog.setDescription("");
        emptyLog.setSeverity(1);
        emptyLog.setTimestamp(LocalDateTime.now());
        emptyLog.setMetadata(new HashMap<>());

        when(logRepository.save(any(Log.class))).thenReturn(emptyLog);

        logConsumerService.handleAuthLog(emptyLog);

        verify(logRepository).save(emptyLog);
    }

    @Test
    void handleAuthLog_WithRepositoryException_ShouldHandleGracefully() {
        when(logRepository.save(any(Log.class))).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            logConsumerService.handleAuthLog(testLog);
        });

        verify(logRepository).save(testLog);
    }

    @Test
    void handleMapColoringLog_WithValidLog_ShouldSaveSuccessfully() {
        when(logRepository.save(any(Log.class))).thenReturn(testLog);

        logConsumerService.handleMapColoringLog(testLog);

        verify(logRepository).save(testLog);
    }

    @Test
    void handleMapColoringLog_WithNullValues_ShouldHandleGracefully() {
        Log nullLog = new Log();
        nullLog.setServiceName(null);
        nullLog.setEventType(null);
        nullLog.setUserId(null);
        nullLog.setDescription(null);
        nullLog.setSeverity(null);
        nullLog.setTimestamp(LocalDateTime.now());
        nullLog.setMetadata(null);

        when(logRepository.save(any(Log.class))).thenReturn(nullLog);

        logConsumerService.handleMapColoringLog(nullLog);

        verify(logRepository).save(nullLog);
    }

    @Test
    void handleMapColoringLog_WithEmptyValues_ShouldHandleGracefully() {
        Log emptyLog = new Log();
        emptyLog.setServiceName("");
        emptyLog.setEventType("");
        emptyLog.setUserId("");
        emptyLog.setDescription("");
        emptyLog.setSeverity(1);
        emptyLog.setTimestamp(LocalDateTime.now());
        emptyLog.setMetadata(new HashMap<>());

        when(logRepository.save(any(Log.class))).thenReturn(emptyLog);

        logConsumerService.handleMapColoringLog(emptyLog);

        verify(logRepository).save(emptyLog);
    }

    @Test
    void handleMapColoringLog_WithRepositoryException_ShouldHandleGracefully() {
        when(logRepository.save(any(Log.class))).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            logConsumerService.handleMapColoringLog(testLog);
        });

        verify(logRepository).save(testLog);
    }

    @Test
    void handleMapStorageLog_WithValidLog_ShouldSaveSuccessfully() {
        when(logRepository.save(any(Log.class))).thenReturn(testLog);

        logConsumerService.handleMapStorageLog(testLog);

        verify(logRepository).save(testLog);
    }

    @Test
    void handleMapStorageLog_WithNullValues_ShouldHandleGracefully() {
        Log nullLog = new Log();
        nullLog.setServiceName(null);
        nullLog.setEventType(null);
        nullLog.setUserId(null);
        nullLog.setDescription(null);
        nullLog.setSeverity(null);
        nullLog.setTimestamp(LocalDateTime.now());
        nullLog.setMetadata(null);

        when(logRepository.save(any(Log.class))).thenReturn(nullLog);

        logConsumerService.handleMapStorageLog(nullLog);

        verify(logRepository).save(nullLog);
    }

    @Test
    void handleMapStorageLog_WithEmptyValues_ShouldHandleGracefully() {
        Log emptyLog = new Log();
        emptyLog.setServiceName("");
        emptyLog.setEventType("");
        emptyLog.setUserId("");
        emptyLog.setDescription("");
        emptyLog.setSeverity(1);
        emptyLog.setTimestamp(LocalDateTime.now());
        emptyLog.setMetadata(new HashMap<>());

        when(logRepository.save(any(Log.class))).thenReturn(emptyLog);

        logConsumerService.handleMapStorageLog(emptyLog);

        verify(logRepository).save(emptyLog);
    }

    @Test
    void handleMapStorageLog_WithRepositoryException_ShouldHandleGracefully() {
        when(logRepository.save(any(Log.class))).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            logConsumerService.handleMapStorageLog(testLog);
        });

        verify(logRepository).save(testLog);
    }

    @Test
    void handleAuthLog_WithVeryLongServiceName_ShouldHandleGracefully() {
        Log longLog = new Log();
        longLog.setServiceName("very-long-service-name-that-exceeds-normal-length-limits-and-should-be-handled-correctly");
        longLog.setEventType("user_login");
        longLog.setUserId("user123");
        longLog.setDescription("User logged in successfully");
        longLog.setSeverity(1);
        longLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(longLog);

        logConsumerService.handleAuthLog(longLog);

        verify(logRepository).save(longLog);
    }

    @Test
    void handleAuthLog_WithVeryLongEventType_ShouldHandleGracefully() {
        Log longLog = new Log();
        longLog.setServiceName("auth-service");
        longLog.setEventType("very-long-event-type-that-exceeds-normal-length-limits-and-should-be-handled-correctly");
        longLog.setUserId("user123");
        longLog.setDescription("User logged in successfully");
        longLog.setSeverity(1);
        longLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(longLog);

        logConsumerService.handleAuthLog(longLog);

        verify(logRepository).save(longLog);
    }

    @Test
    void handleAuthLog_WithVeryLongUserId_ShouldHandleGracefully() {
        Log longLog = new Log();
        longLog.setServiceName("auth-service");
        longLog.setEventType("user_login");
        longLog.setUserId("user123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        longLog.setDescription("User logged in successfully");
        longLog.setSeverity(1);
        longLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(longLog);

        logConsumerService.handleAuthLog(longLog);

        verify(logRepository).save(longLog);
    }

    @Test
    void handleAuthLog_WithVeryLongDescription_ShouldHandleGracefully() {
        Log longLog = new Log();
        longLog.setServiceName("auth-service");
        longLog.setEventType("user_login");
        longLog.setUserId("user123");
        longLog.setDescription("This is a very long description that contains multiple sentences and should be handled correctly by the system without any issues. It includes various details about the user login process and any relevant information that might be useful for debugging or monitoring purposes.");
        longLog.setSeverity(1);
        longLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(longLog);

        logConsumerService.handleAuthLog(longLog);

        verify(logRepository).save(longLog);
    }

    @Test
    void handleAuthLog_WithLargeMetadata_ShouldHandleGracefully() {
        Log largeLog = new Log();
        largeLog.setServiceName("auth-service");
        largeLog.setEventType("user_login");
        largeLog.setUserId("user123");
        largeLog.setDescription("User logged in successfully");
        largeLog.setSeverity(1);
        largeLog.setTimestamp(LocalDateTime.now());
        
        Map<String, String> largeMetadata = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largeMetadata.put("key" + i, "value" + i);
        }
        largeLog.setMetadata(largeMetadata);

        when(logRepository.save(any(Log.class))).thenReturn(largeLog);

        logConsumerService.handleAuthLog(largeLog);

        verify(logRepository).save(largeLog);
    }

    @Test
    void handleAuthLog_WithSpecialCharacters_ShouldHandleGracefully() {
        Log specialLog = new Log();
        specialLog.setServiceName("auth-service@v1.0.0");
        specialLog.setEventType("user_login_failed");
        specialLog.setUserId("user123");
        specialLog.setDescription("User login failed with special characters: !@#$%^&*()");
        specialLog.setSeverity(1);
        specialLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(specialLog);

        logConsumerService.handleAuthLog(specialLog);

        verify(logRepository).save(specialLog);
    }

    @Test
    void handleAuthLog_WithUnicodeCharacters_ShouldHandleGracefully() {
        Log unicodeLog = new Log();
        unicodeLog.setServiceName("auth-service");
        unicodeLog.setEventType("user_login");
        unicodeLog.setUserId("user123");
        unicodeLog.setDescription("User logged in with unicode: 你好世界 🌍");
        unicodeLog.setSeverity(1);
        unicodeLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(unicodeLog);

        logConsumerService.handleAuthLog(unicodeLog);

        verify(logRepository).save(unicodeLog);
    }

    @Test
    void handleAuthLog_WithNegativeSeverity_ShouldHandleGracefully() {
        Log negativeLog = new Log();
        negativeLog.setServiceName("auth-service");
        negativeLog.setEventType("user_login");
        negativeLog.setUserId("user123");
        negativeLog.setDescription("User logged in successfully");
        negativeLog.setSeverity(-1);
        negativeLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(negativeLog);

        logConsumerService.handleAuthLog(negativeLog);

        verify(logRepository).save(negativeLog);
    }

    @Test
    void handleAuthLog_WithZeroSeverity_ShouldHandleGracefully() {
        Log zeroLog = new Log();
        zeroLog.setServiceName("auth-service");
        zeroLog.setEventType("user_login");
        zeroLog.setUserId("user123");
        zeroLog.setDescription("User logged in successfully");
        zeroLog.setSeverity(0);
        zeroLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(zeroLog);

        logConsumerService.handleAuthLog(zeroLog);

        verify(logRepository).save(zeroLog);
    }

    @Test
    void handleAuthLog_WithHighSeverity_ShouldHandleGracefully() {
        Log highLog = new Log();
        highLog.setServiceName("auth-service");
        highLog.setEventType("user_login");
        highLog.setUserId("user123");
        highLog.setDescription("User logged in successfully");
        highLog.setSeverity(10);
        highLog.setTimestamp(LocalDateTime.now());

        when(logRepository.save(any(Log.class))).thenReturn(highLog);

        logConsumerService.handleAuthLog(highLog);

        verify(logRepository).save(highLog);
    }
} 