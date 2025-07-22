package com.fourcolour.common.service;

import com.fourcolour.logger.proto.LoggerProto;
import com.fourcolour.logger.proto.LoggerServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggerClientTest {

    @Mock
    private LoggerServiceGrpc.LoggerServiceBlockingStub loggerServiceStub;

    private LoggerClient loggerClient;

    @BeforeEach
    void setUp() {
        loggerClient = new LoggerClient();
        ReflectionTestUtils.setField(loggerClient, "loggerServiceStub", loggerServiceStub);
    }

    @Test
    void testLogEventWithBasicParameters() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message"));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithAllParameters() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "ERROR", "user123", "Test log message", 3, metadata));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithNullMetadata() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "WARN", "user123", "Test log message", 2, null));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithEmptyMetadata() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "DEBUG", "user123", "Test log message", 1, Collections.emptyMap()));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWhenServiceReturnsFailure() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to log")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "ERROR", "user123", "Test log message"));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWhenServiceThrowsException() {
        // Arrange
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "ERROR", "user123", "Test log message"));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithNullParameters() {
        // Act & Assert
        // The method should handle null parameters gracefully and not throw exceptions
        assertDoesNotThrow(() -> loggerClient.logEvent(null, null, null, null));
        
        // Since null parameters cause NPE in the gRPC builder, the service won't be called
        verify(loggerServiceStub, times(0)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithEmptyParameters() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("", "", "", ""));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithZeroSeverity() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message", 0, null));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithNegativeSeverity() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message", -1, null));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithHighSeverity() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "CRITICAL", "user123", "Test log message", 10, null));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithSpecialCharacters() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user@123", "Test log message with special chars: !@#$%^&*()"));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithLongDescription() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        String longDescription = "This is a very long log message that contains many characters and should be handled properly by the logging system without any issues. " +
                "It includes multiple sentences and various types of content to test the robustness of the logging functionality.";

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", longDescription));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithUnicodeCharacters() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message with unicode: 你好世界"));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithMetadataContainingSpecialCharacters() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key with spaces", "value with spaces");
        metadata.put("key-with-special-chars", "value!@#$%^&*()");
        metadata.put("unicode-key", "unicode-value-你好");

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message", 1, metadata));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithNullMetadataValues() {
        // Arrange
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", null);
        metadata.put("key2", "value2");

        // Act & Assert
        // The method should handle null metadata values gracefully and not throw exceptions
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message", 1, metadata));
        
        // Since null metadata values cause NPE in the gRPC builder, the service won't be called
        verify(loggerServiceStub, times(0)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithEmptyMetadataValues() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "");
        metadata.put("key2", "value2");

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message", 1, metadata));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }

    @Test
    void testLogEventWithLargeMetadata() {
        // Arrange
        LoggerProto.LogResponse mockResponse = LoggerProto.LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged successfully")
                .build();
        when(loggerServiceStub.logEvent(any(LoggerProto.LogRequest.class))).thenReturn(mockResponse);

        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            metadata.put("key" + i, "value" + i);
        }

        // Act
        assertDoesNotThrow(() -> loggerClient.logEvent("test-service", "INFO", "user123", "Test log message", 1, metadata));

        // Assert
        verify(loggerServiceStub, times(1)).logEvent(any(LoggerProto.LogRequest.class));
    }
} 