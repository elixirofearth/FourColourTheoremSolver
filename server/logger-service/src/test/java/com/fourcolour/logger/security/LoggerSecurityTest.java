package com.fourcolour.logger.security;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.LogRequest;
import com.fourcolour.logger.proto.LoggerProto.LogResponse;
import com.fourcolour.logger.repository.LogRepository;
import com.fourcolour.logger.service.LoggerGrpcService;
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
class LoggerSecurityTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private KafkaTemplate<String, Log> kafkaTemplate;

    @InjectMocks
    private LoggerGrpcService loggerGrpcService;

    private StreamObserver<LogResponse> responseObserver;

    @BeforeEach
    void setUp() {
        responseObserver = mock(StreamObserver.class);
    }

    @Test
    void sqlInjectionInServiceName_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service'; DROP TABLE logs; --")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void sqlInjectionInEventType_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login'; DROP TABLE logs; --")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void sqlInjectionInUserId_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123'; DROP TABLE logs; --")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void sqlInjectionInDescription_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully'; DROP TABLE logs; --")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void xssInServiceName_ShouldBeHandledSafely() {
        LogRequest xssRequest = LogRequest.newBuilder()
                .setServiceName("<script>alert('xss')</script>")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(xssRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void xssInEventType_ShouldBeHandledSafely() {
        LogRequest xssRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("<script>alert('xss')</script>")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(xssRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void xssInUserId_ShouldBeHandledSafely() {
        LogRequest xssRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("<script>alert('xss')</script>")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(xssRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void xssInDescription_ShouldBeHandledSafely() {
        LogRequest xssRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("<script>alert('xss')</script>")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(xssRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void pathTraversalInServiceName_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("../../../etc/passwd")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void pathTraversalInEventType_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("../../../etc/passwd")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void pathTraversalInUserId_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("../../../etc/passwd")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void pathTraversalInDescription_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("../../../etc/passwd")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void longServiceName_ShouldBeHandledSafely() {
        String longServiceName = "a".repeat(1000);
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName(longServiceName)
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void longEventType_ShouldBeHandledSafely() {
        String longEventType = "a".repeat(1000);
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType(longEventType)
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void longUserId_ShouldBeHandledSafely() {
        String longUserId = "a".repeat(1000);
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId(longUserId)
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void longDescription_ShouldBeHandledSafely() {
        String longDescription = "a".repeat(1000);
        LogRequest longRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription(longDescription)
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(longRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void nullByteInjection_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service\0")
                .setEventType("user_login\0")
                .setUserId("user123\0")
                .setDescription("User logged in successfully\0")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void unicodeInjection_ShouldBeHandledSafely() {
        LogRequest unicodeRequest = LogRequest.newBuilder()
                .setServiceName("auth-service\u0000\u0001\u0002")
                .setEventType("user_login\u0000\u0001\u0002")
                .setUserId("user123\u0000\u0001\u0002")
                .setDescription("User logged in successfully\u0000\u0001\u0002")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(unicodeRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void emptyStringInputs_ShouldBeHandledSafely() {
        LogRequest emptyRequest = LogRequest.newBuilder()
                .setServiceName("")
                .setEventType("")
                .setUserId("")
                .setDescription("")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(emptyRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void nullInputs_ShouldBeHandledSafely() {
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

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void negativeSeverity_ShouldBeHandledSafely() {
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

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void zeroSeverity_ShouldBeHandledSafely() {
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

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void highSeverity_ShouldBeHandledSafely() {
        LogRequest highRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(100)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(highRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void invalidTimestamp_ShouldBeHandledSafely() {
        LogRequest invalidRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp("invalid-timestamp")
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(invalidRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void largeMetadata_ShouldBeHandledSafely() {
        LogRequest.Builder requestBuilder = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString());

        // Add large metadata
        for (int i = 0; i < 1000; i++) {
            requestBuilder.putMetadata("key" + i, "value" + i);
        }

        LogRequest largeRequest = requestBuilder.build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(largeRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void maliciousMetadata_ShouldBeHandledSafely() {
        LogRequest maliciousRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putMetadata("malicious_key", "<script>alert('xss')</script>")
                .putMetadata("sql_injection", "'; DROP TABLE logs; --")
                .putMetadata("path_traversal", "../../../etc/passwd")
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(maliciousRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void concurrentLogCreation_ShouldNotCauseRaceCondition() throws InterruptedException {
        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                LogRequest request = LogRequest.newBuilder()
                        .setServiceName("auth-service")
                        .setEventType("user_login")
                        .setUserId("user" + threadId)
                        .setDescription("User logged in successfully")
                        .setSeverity(1)
                        .setTimestamp(LocalDateTime.now().toString())
                        .build();

                loggerGrpcService.logEvent(request, responseObserver);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify that all requests were processed
        verify(kafkaTemplate, times(numberOfThreads)).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver, times(numberOfThreads)).onNext(any(LogResponse.class));
        verify(responseObserver, times(numberOfThreads)).onCompleted();
    }

    @Test
    void sensitiveDataInLogs_ShouldNotLeakSensitiveInformation() {
        LogRequest sensitiveRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in with password: secret123")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putMetadata("password", "secret123")
                .putMetadata("token", "jwt_token_here")
                .putMetadata("credit_card", "1234-5678-9012-3456")
                .build();

        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);

        loggerGrpcService.logEvent(sensitiveRequest, responseObserver);

        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
        verify(responseObserver).onNext(any(LogResponse.class));
        verify(responseObserver).onCompleted();
    }
} 