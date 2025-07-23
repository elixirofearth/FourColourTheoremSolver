package com.fourcolour.logger.integration;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.LogRequest;
import com.fourcolour.logger.proto.LoggerProto.LogResponse;
import com.fourcolour.logger.repository.LogRepository;
import com.fourcolour.logger.service.LoggerGrpcService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test for Logger Service
 * Tests the full logger workflow with real database (MongoDB in-memory)
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class LoggerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LoggerGrpcService loggerGrpcService;

    @MockBean
    private KafkaTemplate<String, Log> kafkaTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("logging.level.org.springframework.web", () -> "WARN");
        registry.add("logging.level.org.springframework.data.mongodb", () -> "WARN");
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "logger-service-test-group");
    }

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        reset(kafkaTemplate);
        when(kafkaTemplate.send(anyString(), any(Log.class))).thenReturn(null);
    }

    @Test
    @DisplayName("Application should start and context should load")
    void applicationShouldStartAndContextShouldLoad() {
        assertNotNull(logRepository);
        assertNotNull(loggerGrpcService);
    }

    @Test
    @DisplayName("Log creation and retrieval should work end-to-end")
    void logCreationAndRetrieval_ShouldWorkEndToEnd() {
        // Create a log via gRPC
        LogRequest request = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();

        TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(request, responseObserver);

        // Verify gRPC response
        assertTrue(responseObserver.getResponse().getSuccess());
        assertEquals("Log published successfully", responseObserver.getResponse().getMessage());

        // Verify Kafka was called
        verify(kafkaTemplate).send(eq("auth_logs"), any(Log.class));

        // Simulate Kafka consumer processing
        Log logToSave = new Log();
        logToSave.setServiceName("auth-service");
        logToSave.setEventType("user_login");
        logToSave.setUserId("user123");
        logToSave.setDescription("User logged in successfully");
        logToSave.setSeverity(1);
        logToSave.setTimestamp(LocalDateTime.now());

        Log savedLog = logRepository.save(logToSave);

        // Verify log was saved
        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertEquals("auth-service", savedLog.getServiceName());
        assertEquals("user_login", savedLog.getEventType());
        assertEquals("user123", savedLog.getUserId());
    }

    @Test
    @DisplayName("Log retrieval by service name should work")
    void logRetrievalByServiceName_ShouldWork() {
        // Create multiple logs
        Log log1 = createTestLog("auth-service", "user_login", "user123");
        Log log2 = createTestLog("auth-service", "user_logout", "user123");
        Log log3 = createTestLog("map-storage-service", "map_created", "user456");

        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);

        // Retrieve logs by service name
        List<Log> authLogs = logRepository.findByServiceName("auth-service");
        List<Log> mapStorageLogs = logRepository.findByServiceName("map-storage-service");

        assertEquals(2, authLogs.size());
        assertEquals(1, mapStorageLogs.size());
        assertTrue(authLogs.stream().allMatch(log -> "auth-service".equals(log.getServiceName())));
        assertTrue(mapStorageLogs.stream().allMatch(log -> "map-storage-service".equals(log.getServiceName())));
    }

    @Test
    @DisplayName("Log retrieval by user ID should work")
    void logRetrievalByUserId_ShouldWork() {
        // Create multiple logs
        Log log1 = createTestLog("auth-service", "user_login", "user123");
        Log log2 = createTestLog("map-storage-service", "map_created", "user123");
        Log log3 = createTestLog("auth-service", "user_login", "user456");

        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);

        // Retrieve logs by user ID
        List<Log> user123Logs = logRepository.findByUserId("user123");
        List<Log> user456Logs = logRepository.findByUserId("user456");

        assertEquals(2, user123Logs.size());
        assertEquals(1, user456Logs.size());
        assertTrue(user123Logs.stream().allMatch(log -> "user123".equals(log.getUserId())));
        assertTrue(user456Logs.stream().allMatch(log -> "user456".equals(log.getUserId())));
    }

    @Test
    @DisplayName("Log retrieval by event type should work")
    void logRetrievalByEventType_ShouldWork() {
        // Create multiple logs
        Log log1 = createTestLog("auth-service", "user_login", "user123");
        Log log2 = createTestLog("auth-service", "user_logout", "user123");
        Log log3 = createTestLog("map-storage-service", "user_login", "user456");

        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);

        // Retrieve logs by event type
        List<Log> loginLogs = logRepository.findByEventType("user_login");
        List<Log> logoutLogs = logRepository.findByEventType("user_logout");

        assertEquals(2, loginLogs.size());
        assertEquals(1, logoutLogs.size());
        assertTrue(loginLogs.stream().allMatch(log -> "user_login".equals(log.getEventType())));
        assertTrue(logoutLogs.stream().allMatch(log -> "user_logout".equals(log.getEventType())));
    }

    @Test
    @DisplayName("Log retrieval by timestamp range should work")
    void logRetrievalByTimestampRange_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);

        // Create logs with different timestamps
        Log log1 = createTestLog("auth-service", "user_login", "user123");
        log1.setTimestamp(now);
        Log log2 = createTestLog("map-storage-service", "map_created", "user456");
        log2.setTimestamp(now.plusMinutes(30));
        Log log3 = createTestLog("auth-service", "user_logout", "user123");
        log3.setTimestamp(now.plusHours(2)); // Outside range

        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);

        // Retrieve logs by timestamp range
        List<Log> logsInRange = logRepository.findByTimestampBetween(start, end);

        assertEquals(2, logsInRange.size());
        assertTrue(logsInRange.stream().allMatch(log -> 
            log.getTimestamp().isAfter(start) && log.getTimestamp().isBefore(end)));
    }

    @Test
    @DisplayName("Log retrieval by service name and event type should work")
    void logRetrievalByServiceNameAndEventType_ShouldWork() {
        // Create multiple logs
        Log log1 = createTestLog("auth-service", "user_login", "user123");
        Log log2 = createTestLog("auth-service", "user_logout", "user123");
        Log log3 = createTestLog("map-storage-service", "user_login", "user456");

        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);

        // Retrieve logs by service name and event type
        List<Log> authLoginLogs = logRepository.findByServiceNameAndEventType("auth-service", "user_login");
        List<Log> authLogoutLogs = logRepository.findByServiceNameAndEventType("auth-service", "user_logout");
        List<Log> mapStorageLoginLogs = logRepository.findByServiceNameAndEventType("map-storage-service", "user_login");

        assertEquals(1, authLoginLogs.size());
        assertEquals(1, authLogoutLogs.size());
        assertEquals(1, mapStorageLoginLogs.size());
        assertEquals("auth-service", authLoginLogs.get(0).getServiceName());
        assertEquals("user_login", authLoginLogs.get(0).getEventType());
    }

    @Test
    @DisplayName("gRPC service should handle different service types correctly")
    void grpcService_ShouldHandleDifferentServiceTypesCorrectly() {
        // Test auth service
        LogRequest authRequest = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        TestStreamObserver<LogResponse> authObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(authRequest, authObserver);

        assertTrue(authObserver.getResponse().getSuccess());
        verify(kafkaTemplate).send(eq("auth_logs"), any(Log.class));

        // Test map coloring service
        LogRequest mapColoringRequest = LogRequest.newBuilder()
                .setServiceName("solver-service")
                .setEventType("map_coloring_started")
                .setUserId("user123")
                .setDescription("Map coloring started")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        TestStreamObserver<LogResponse> mapColoringObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(mapColoringRequest, mapColoringObserver);

        assertTrue(mapColoringObserver.getResponse().getSuccess());
        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));

        // Test map storage service
        LogRequest mapStorageRequest = LogRequest.newBuilder()
                .setServiceName("map-storage-service")
                .setEventType("map_created")
                .setUserId("user123")
                .setDescription("Map created successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        TestStreamObserver<LogResponse> mapStorageObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(mapStorageRequest, mapStorageObserver);

        assertTrue(mapStorageObserver.getResponse().getSuccess());
        verify(kafkaTemplate).send(eq("map_storage_logs"), any(Log.class));
    }

    @Test
    @DisplayName("gRPC service should handle invalid timestamp gracefully")
    void grpcService_ShouldHandleInvalidTimestampGracefully() {
        LogRequest request = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp("invalid-timestamp")
                .build();

        TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(request, responseObserver);

        assertTrue(responseObserver.getResponse().getSuccess());
        verify(kafkaTemplate).send(eq("auth_logs"), any(Log.class));
    }

    @Test
    @DisplayName("gRPC service should handle null values gracefully")
    void grpcService_ShouldHandleNullValuesGracefully() {
        LogRequest request = LogRequest.newBuilder()
                .setServiceName("")
                .setEventType("")
                .setUserId("")
                .setDescription("")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(request, responseObserver);

        assertTrue(responseObserver.getResponse().getSuccess());
        verify(kafkaTemplate).send(eq("map_coloring_logs"), any(Log.class));
    }

    @Test
    @DisplayName("gRPC service should handle Kafka exceptions gracefully")
    void grpcService_ShouldHandleKafkaExceptionsGracefully() {
        when(kafkaTemplate.send(anyString(), any(Log.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        LogRequest request = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(request, responseObserver);

        assertFalse(responseObserver.getResponse().getSuccess());
        assertTrue(responseObserver.getResponse().getMessage().contains("Failed to publish log"));
    }

    @Test
    @DisplayName("Complete log workflow should work")
    void completeLogWorkflow_ShouldWork() {
        // Step 1: Create log via gRPC
        LogRequest request = LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();

        TestStreamObserver<LogResponse> responseObserver = new TestStreamObserver<>();
        loggerGrpcService.logEvent(request, responseObserver);

        assertTrue(responseObserver.getResponse().getSuccess());
        verify(kafkaTemplate).send(eq("auth_logs"), any(Log.class));

        // Step 2: Simulate Kafka consumer processing
        Log logToSave = new Log();
        logToSave.setServiceName("auth-service");
        logToSave.setEventType("user_login");
        logToSave.setUserId("user123");
        logToSave.setDescription("User logged in successfully");
        logToSave.setSeverity(1);
        logToSave.setTimestamp(LocalDateTime.now());
        logToSave.setMetadata(createValidMetadata());

        Log savedLog = logRepository.save(logToSave);

        // Step 3: Verify log was saved correctly
        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertEquals("auth-service", savedLog.getServiceName());
        assertEquals("user_login", savedLog.getEventType());
        assertEquals("user123", savedLog.getUserId());
        assertEquals("User logged in successfully", savedLog.getDescription());
        assertEquals(1, savedLog.getSeverity());
        assertNotNull(savedLog.getTimestamp());
        assertNotNull(savedLog.getMetadata());
        assertEquals(3, savedLog.getMetadata().size());

        // Step 4: Verify log can be retrieved
        List<Log> logsByService = logRepository.findByServiceName("auth-service");
        List<Log> logsByUser = logRepository.findByUserId("user123");
        List<Log> logsByEvent = logRepository.findByEventType("user_login");

        assertEquals(1, logsByService.size());
        assertEquals(1, logsByUser.size());
        assertEquals(1, logsByEvent.size());
        assertEquals(savedLog.getId(), logsByService.get(0).getId());
        assertEquals(savedLog.getId(), logsByUser.get(0).getId());
        assertEquals(savedLog.getId(), logsByEvent.get(0).getId());
    }

    @Test
    @DisplayName("Multiple logs for same user should work")
    void multipleLogsForSameUser_ShouldWork() {
        // Create multiple logs for the same user
        Log log1 = createTestLog("auth-service", "user_login", "user123");
        Log log2 = createTestLog("map-storage-service", "map_created", "user123");
        Log log3 = createTestLog("auth-service", "user_logout", "user123");

        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);

        // Verify all logs can be retrieved
        List<Log> userLogs = logRepository.findByUserId("user123");
        assertEquals(3, userLogs.size());
        assertTrue(userLogs.stream().allMatch(log -> "user123".equals(log.getUserId())));

        // Verify logs by service
        List<Log> authLogs = logRepository.findByServiceName("auth-service");
        List<Log> mapStorageLogs = logRepository.findByServiceName("map-storage-service");
        assertEquals(2, authLogs.size());
        assertEquals(1, mapStorageLogs.size());
    }

    @Test
    @DisplayName("Log creation with null values should work")
    void logCreationWithNullValues_ShouldWork() {
        Log log = new Log();
        log.setServiceName(null);
        log.setEventType(null);
        log.setUserId(null);
        log.setDescription(null);
        log.setSeverity(null);
        log.setTimestamp(LocalDateTime.now());
        log.setMetadata(null);

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertNull(savedLog.getServiceName());
        assertNull(savedLog.getEventType());
        assertNull(savedLog.getUserId());
        assertNull(savedLog.getDescription());
        assertNull(savedLog.getSeverity());
        assertNotNull(savedLog.getTimestamp());
        assertNull(savedLog.getMetadata());
    }

    @Test
    @DisplayName("Log creation with large data should work")
    void logCreationWithLargeData_ShouldWork() {
        Log log = new Log();
        log.setServiceName("very-long-service-name-that-exceeds-normal-length-limits-and-should-be-handled-correctly");
        log.setEventType("very-long-event-type-that-exceeds-normal-length-limits-and-should-be-handled-correctly");
        log.setUserId("user123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        log.setDescription("This is a very long description that contains multiple sentences and should be handled correctly by the system without any issues. It includes various details about the user login process and any relevant information that might be useful for debugging or monitoring purposes.");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        
        Map<String, String> largeMetadata = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largeMetadata.put("key" + i, "value" + i);
        }
        log.setMetadata(largeMetadata);

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertEquals(log.getServiceName(), savedLog.getServiceName());
        assertEquals(log.getEventType(), savedLog.getEventType());
        assertEquals(log.getUserId(), savedLog.getUserId());
        assertEquals(log.getDescription(), savedLog.getDescription());
        assertEquals(log.getSeverity(), savedLog.getSeverity());
        assertEquals(100, savedLog.getMetadata().size());
    }

    @Test
    @DisplayName("Log creation with special characters should work")
    void logCreationWithSpecialCharacters_ShouldWork() {
        Log log = new Log();
        log.setServiceName("auth-service@v1.0.0");
        log.setEventType("user_login_failed");
        log.setUserId("user123");
        log.setDescription("User login failed with special characters: !@#$%^&*()");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertEquals("auth-service@v1.0.0", savedLog.getServiceName());
        assertEquals("user_login_failed", savedLog.getEventType());
        assertEquals("User login failed with special characters: !@#$%^&*()", savedLog.getDescription());
    }

    private Log createTestLog(String serviceName, String eventType, String userId) {
        Log log = new Log();
        log.setServiceName(serviceName);
        log.setEventType(eventType);
        log.setUserId(userId);
        log.setDescription("Test log");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        return log;
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

        @SuppressWarnings("unchecked")
        public LogResponse getResponse() {
            return (LogResponse) response;
        }

        public Throwable getError() {
            return error;
        }
    }
} 