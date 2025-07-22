package com.fourcolour.logger.service;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.LogRequest;
import com.fourcolour.logger.proto.LoggerProto.LogResponse;
import com.fourcolour.logger.repository.LogRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggerGrpcServiceTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private KafkaTemplate<String, Log> kafkaTemplate;

    @InjectMocks
    private LoggerGrpcService loggerGrpcService;

    private LogRequest testLogRequest;
    private StreamObserver<LogResponse> responseObserver;

    @BeforeEach
    void setUp() {
        testLogRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();

        responseObserver = mock(StreamObserver.class);
    }

    @Test
    void logEvent_WithValidRequest_ShouldReturnSuccessResponse() {
        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(testLogRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void logEvent_WithAuthService_ShouldSendToAuthTopic() {
        LogRequest authRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(authRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
    }

    @Test
    void logEvent_WithMapColoringService_ShouldSendToMapColoringTopic() {
        LogRequest mapColoringRequest = LogRequest.newBuilder()
                .setServiceName("solver-service")
                .setEventType("map_coloring_started")
                .setUserId("user123")
                .setDescription("Map coloring started")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(mapColoringRequest, responseObserver);

        verify(kafkaTemplate).send("map_coloring_logs", any(Log.class));
    }

    @Test
    void logEvent_WithMapStorageService_ShouldSendToMapStorageTopic() {
        LogRequest mapStorageRequest = LogRequest.newBuilder()
                .setServiceName("map-storage-service")
                .setEventType("map_created")
                .setUserId("user123")
                .setDescription("Map created successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(mapStorageRequest, responseObserver);

        verify(kafkaTemplate).send("map_storage_logs", any(Log.class));
    }

    @Test
    void logEvent_WithUnknownService_ShouldSendToDefaultTopic() {
        LogRequest unknownRequest = LogRequest.newBuilder()
                .setServiceName("unknown-service")
                .setEventType("unknown_event")
                .setUserId("user123")
                .setDescription("Unknown event")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(unknownRequest, responseObserver);

        verify(kafkaTemplate).send("map_coloring_logs", any(Log.class));
    }

    @Test
    void logEvent_WithInvalidTimestamp_ShouldUseCurrentTime() {
        LogRequest invalidTimestampRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp("invalid-timestamp")
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(invalidTimestampRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithNullValues_ShouldHandleGracefully() {
        LogRequest nullRequest = LogRequest.newBuilder()
                .setServiceName("")
                .setEventType("")
                .setUserId("")
                .setDescription("")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(nullRequest, responseObserver);

        verify(kafkaTemplate).send("map_coloring_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithKafkaException_ShouldReturnErrorResponse() {
        when(kafkaTemplate.send(anyString(), any(Log.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        loggerGrpcService.logEvent(testLogRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(argThat(response -> 
            !response.getSuccess() && response.getMessage().contains("Failed to publish log")));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void logEvent_WithVeryLongServiceName_ShouldHandleGracefully() {
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("very-long-service-name-that-exceeds-normal-length-limits-and-should-be-handled-correctly")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send("map_coloring_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithVeryLongEventType_ShouldHandleGracefully() {
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("very-long-event-type-that-exceeds-normal-length-limits-and-should-be-handled-correctly")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithVeryLongUserId_ShouldHandleGracefully() {
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithVeryLongDescription_ShouldHandleGracefully() {
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("This is a very long description that contains multiple sentences and should be handled correctly by the system without any issues. It includes various details about the user login process and any relevant information that might be useful for debugging or monitoring purposes.")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithLargeMetadata_ShouldHandleGracefully() {
        LogRequest.Builder requestBuilder = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString());

        for (int i = 0; i < 100; i++) {
            requestBuilder.putMetadata("key" + i, "value" + i);
        }

        LogRequest largeRequest = requestBuilder.build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(largeRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithSpecialCharacters_ShouldHandleGracefully() {
        LogRequest specialRequest = LogRequest.newBuilder()
                .setServiceName("auth-service@v1.0.0")
                .setEventType("user_login_failed")
                .setUserId("user123")
                .setDescription("User login failed with special characters: !@#$%^&*()")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(specialRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithUnicodeCharacters_ShouldHandleGracefully() {
        LogRequest unicodeRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in with unicode: 你好世界 🌍")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(unicodeRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithNegativeSeverity_ShouldHandleGracefully() {
        LogRequest negativeRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(-1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(negativeRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithZeroSeverity_ShouldHandleGracefully() {
        LogRequest zeroRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(0)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(zeroRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithHighSeverity_ShouldHandleGracefully() {
        LogRequest highRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(10)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(highRequest, responseObserver);

        verify(kafkaTemplate).send("auth_logs", any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void logEvent_WithDifferentServiceNameVariations_ShouldRouteCorrectly() {
        // Test different variations of auth service names
        String[] authServiceNames = {"auth", "authentication", "authentication-service"};
        for (String serviceName : authServiceNames) {
            LogRequest request = LogRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setEventType("user_login")
                    .setUserId("user123")
                    .setDescription("User logged in successfully")
                    .setSeverity(1)
                    .setTimestamp(LocalDateTime.now().toString())
                    .build();

            when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

            loggerGrpcService.logEvent(request, responseObserver);

            verify(kafkaTemplate).send("auth_logs", any(Log.class));
            reset(kafkaTemplate);
        }
    }

    @Test
    void logEvent_WithDifferentMapColoringServiceNames_ShouldRouteCorrectly() {
        // Test different variations of map coloring service names
        String[] mapColoringServiceNames = {"map_coloring", "map_coloring_service", "solver", "solver-service"};
        for (String serviceName : mapColoringServiceNames) {
            LogRequest request = LogRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setEventType("map_coloring_started")
                    .setUserId("user123")
                    .setDescription("Map coloring started")
                    .setSeverity(1)
                    .setTimestamp(LocalDateTime.now().toString())
                    .build();

            when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

            loggerGrpcService.logEvent(request, responseObserver);

            verify(kafkaTemplate).send("map_coloring_logs", any(Log.class));
            reset(kafkaTemplate);
        }
    }

    @Test
    void logEvent_WithDifferentMapStorageServiceNames_ShouldRouteCorrectly() {
        // Test different variations of map storage service names
        String[] mapStorageServiceNames = {"map_storage", "map_storage_service", "map-storage", "map-storage-service"};
        for (String serviceName : mapStorageServiceNames) {
            LogRequest request = LogRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setEventType("map_created")
                    .setUserId("user123")
                    .setDescription("Map created successfully")
                    .setSeverity(1)
                    .setTimestamp(LocalDateTime.now().toString())
                    .build();

            when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

            loggerGrpcService.logEvent(request, responseObserver);

            verify(kafkaTemplate).send("map_storage_logs", any(Log.class));
            reset(kafkaTemplate);
        }
    }

    private Map<String, String> createValidMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        metadata.put("session_id", "session123");
        return metadata;
    }
} 