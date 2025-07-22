package com.fourcolour.logger;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.LogRequest;
import com.fourcolour.logger.proto.LoggerProto.LogResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static Log createValidLog() {
        Log log = new Log();
        log.setId("507f1f77bcf86cd799439011");
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        log.setUserId("user123");
        log.setDescription("User logged in successfully");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        log.setMetadata(createValidMetadata());
        return log;
    }

    public static Log createLogWithId(String id) {
        Log log = createValidLog();
        log.setId(id);
        return log;
    }

    public static Log createLogWithServiceName(String serviceName) {
        Log log = createValidLog();
        log.setServiceName(serviceName);
        return log;
    }

    public static Log createLogWithEventType(String eventType) {
        Log log = createValidLog();
        log.setEventType(eventType);
        return log;
    }

    public static Log createLogWithUserId(String userId) {
        Log log = createValidLog();
        log.setUserId(userId);
        return log;
    }

    public static Log createLogWithSeverity(Integer severity) {
        Log log = createValidLog();
        log.setSeverity(severity);
        return log;
    }

    public static Log createLogWithTimestamp(LocalDateTime timestamp) {
        Log log = createValidLog();
        log.setTimestamp(timestamp);
        return log;
    }

    public static Log createLogWithMetadata(Map<String, String> metadata) {
        Log log = createValidLog();
        log.setMetadata(metadata);
        return log;
    }

    public static Log createLogWithNullValues() {
        Log log = new Log();
        log.setId("507f1f77bcf86cd799439011");
        log.setServiceName(null);
        log.setEventType(null);
        log.setUserId(null);
        log.setDescription(null);
        log.setSeverity(null);
        log.setTimestamp(LocalDateTime.now());
        log.setMetadata(null);
        return log;
    }

    public static Log createLogWithEmptyValues() {
        Log log = new Log();
        log.setId("507f1f77bcf86cd799439011");
        log.setServiceName("");
        log.setEventType("");
        log.setUserId("");
        log.setDescription("");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        log.setMetadata(new HashMap<>());
        return log;
    }

    public static LogRequest createValidLogRequest() {
        return LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();
    }

    public static LogRequest createLogRequestWithServiceName(String serviceName) {
        return LogRequest.newBuilder()
                .setServiceName(serviceName)
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();
    }

    public static LogRequest createLogRequestWithEventType(String eventType) {
        return LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType(eventType)
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();
    }

    public static LogRequest createLogRequestWithUserId(String userId) {
        return LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId(userId)
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();
    }

    public static LogRequest createLogRequestWithSeverity(Integer severity) {
        return LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(severity)
                .setTimestamp(LocalDateTime.now().toString())
                .putAllMetadata(createValidMetadata())
                .build();
    }

    public static LogRequest createLogRequestWithInvalidTimestamp() {
        return LogRequest.newBuilder()
                .setServiceName("auth-service")
                .setEventType("user_login")
                .setUserId("user123")
                .setDescription("User logged in successfully")
                .setSeverity(1)
                .setTimestamp("invalid-timestamp")
                .putAllMetadata(createValidMetadata())
                .build();
    }

    public static LogRequest createLogRequestWithNullValues() {
        return LogRequest.newBuilder()
                .setServiceName("")
                .setEventType("")
                .setUserId("")
                .setDescription("")
                .setSeverity(1)
                .setTimestamp(LocalDateTime.now().toString())
                .build();
    }

    public static LogResponse createSuccessLogResponse() {
        return LogResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Log published successfully")
                .build();
    }

    public static LogResponse createErrorLogResponse(String errorMessage) {
        return LogResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to publish log: " + errorMessage)
                .build();
    }

    public static Map<String, String> createValidMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        metadata.put("session_id", "session123");
        return metadata;
    }

    public static Map<String, String> createEmptyMetadata() {
        return new HashMap<>();
    }

    public static Map<String, String> createLargeMetadata() {
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            metadata.put("key" + i, "value" + i);
        }
        return metadata;
    }

    public static String createValidObjectId() {
        return "507f1f77bcf86cd799439011";
    }

    public static String createInvalidObjectId() {
        return "invalid-id";
    }

    public static String createShortObjectId() {
        return "123";
    }

    public static String createLongObjectId() {
        return "507f1f77bcf86cd799439011123456789";
    }

    public static String createObjectIdWithInvalidCharacters() {
        return "507f1f77bcf86cd79943901g";
    }

    public static Log createAuthLog() {
        Log log = createValidLog();
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        return log;
    }

    public static Log createMapColoringLog() {
        Log log = createValidLog();
        log.setServiceName("solver-service");
        log.setEventType("map_coloring_started");
        return log;
    }

    public static Log createMapStorageLog() {
        Log log = createValidLog();
        log.setServiceName("map-storage-service");
        log.setEventType("map_created");
        return log;
    }
} 