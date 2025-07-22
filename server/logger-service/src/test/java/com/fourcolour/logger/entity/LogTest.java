package com.fourcolour.logger.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogTest {

    private Log log;

    @BeforeEach
    void setUp() {
        log = new Log();
    }

    @Test
    void defaultConstructor_ShouldCreateLogWithNullValues() {
        assertNull(log.getId());
        assertNull(log.getServiceName());
        assertNull(log.getEventType());
        assertNull(log.getUserId());
        assertNull(log.getDescription());
        assertNull(log.getSeverity());
        assertNotNull(log.getTimestamp());
        assertNull(log.getMetadata());
    }

    @Test
    void parameterizedConstructor_ShouldSetValues() {
        String serviceName = "auth-service";
        String eventType = "user_login";
        String userId = "user123";
        String description = "User logged in successfully";
        Integer severity = 1;
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");

        Log paramLog = new Log(serviceName, eventType, userId, description, severity, timestamp, metadata);

        assertEquals(serviceName, paramLog.getServiceName());
        assertEquals(eventType, paramLog.getEventType());
        assertEquals(userId, paramLog.getUserId());
        assertEquals(description, paramLog.getDescription());
        assertEquals(severity, paramLog.getSeverity());
        assertEquals(timestamp, paramLog.getTimestamp());
        assertEquals(metadata, paramLog.getMetadata());
        assertNull(paramLog.getId()); // ID should still be null as it's auto-generated
    }

    @Test
    void parameterizedConstructor_WithNullTimestamp_ShouldUseCurrentTime() {
        String serviceName = "auth-service";
        String eventType = "user_login";
        String userId = "user123";
        String description = "User logged in successfully";
        Integer severity = 1;
        Map<String, String> metadata = new HashMap<>();

        Log paramLog = new Log(serviceName, eventType, userId, description, severity, null, metadata);

        assertNotNull(paramLog.getTimestamp());
        assertTrue(paramLog.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        String id = "507f1f77bcf86cd799439011";
        String serviceName = "auth-service";
        String eventType = "user_login";
        String userId = "user123";
        String description = "User logged in successfully";
        Integer severity = 1;
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");

        log.setId(id);
        log.setServiceName(serviceName);
        log.setEventType(eventType);
        log.setUserId(userId);
        log.setDescription(description);
        log.setSeverity(severity);
        log.setTimestamp(timestamp);
        log.setMetadata(metadata);

        assertEquals(id, log.getId());
        assertEquals(serviceName, log.getServiceName());
        assertEquals(eventType, log.getEventType());
        assertEquals(userId, log.getUserId());
        assertEquals(description, log.getDescription());
        assertEquals(severity, log.getSeverity());
        assertEquals(timestamp, log.getTimestamp());
        assertEquals(metadata, log.getMetadata());
    }

    @Test
    void setId_WithValidId_ShouldSetId() {
        String validId = "507f1f77bcf86cd799439011";
        
        log.setId(validId);
        
        assertEquals(validId, log.getId());
    }

    @Test
    void setId_WithNullId_ShouldSetNull() {
        log.setId(null);
        
        assertNull(log.getId());
    }

    @Test
    void setId_WithEmptyId_ShouldSetEmptyString() {
        log.setId("");
        
        assertEquals("", log.getId());
    }

    @Test
    void setServiceName_WithValidServiceName_ShouldSetServiceName() {
        String serviceName = "auth-service";
        
        log.setServiceName(serviceName);
        
        assertEquals(serviceName, log.getServiceName());
    }

    @Test
    void setServiceName_WithNullServiceName_ShouldSetNull() {
        log.setServiceName(null);
        
        assertNull(log.getServiceName());
    }

    @Test
    void setServiceName_WithEmptyServiceName_ShouldSetEmptyString() {
        log.setServiceName("");
        
        assertEquals("", log.getServiceName());
    }

    @Test
    void setEventType_WithValidEventType_ShouldSetEventType() {
        String eventType = "user_login";
        
        log.setEventType(eventType);
        
        assertEquals(eventType, log.getEventType());
    }

    @Test
    void setEventType_WithNullEventType_ShouldSetNull() {
        log.setEventType(null);
        
        assertNull(log.getEventType());
    }

    @Test
    void setEventType_WithEmptyEventType_ShouldSetEmptyString() {
        log.setEventType("");
        
        assertEquals("", log.getEventType());
    }

    @Test
    void setUserId_WithValidUserId_ShouldSetUserId() {
        String userId = "user123";
        
        log.setUserId(userId);
        
        assertEquals(userId, log.getUserId());
    }

    @Test
    void setUserId_WithNullUserId_ShouldSetNull() {
        log.setUserId(null);
        
        assertNull(log.getUserId());
    }

    @Test
    void setUserId_WithEmptyUserId_ShouldSetEmptyString() {
        log.setUserId("");
        
        assertEquals("", log.getUserId());
    }

    @Test
    void setDescription_WithValidDescription_ShouldSetDescription() {
        String description = "User logged in successfully";
        
        log.setDescription(description);
        
        assertEquals(description, log.getDescription());
    }

    @Test
    void setDescription_WithNullDescription_ShouldSetNull() {
        log.setDescription(null);
        
        assertNull(log.getDescription());
    }

    @Test
    void setDescription_WithEmptyDescription_ShouldSetEmptyString() {
        log.setDescription("");
        
        assertEquals("", log.getDescription());
    }

    @Test
    void setSeverity_WithPositiveSeverity_ShouldSetSeverity() {
        Integer severity = 1;
        
        log.setSeverity(severity);
        
        assertEquals(severity, log.getSeverity());
    }

    @Test
    void setSeverity_WithZeroSeverity_ShouldSetZero() {
        log.setSeverity(0);
        
        assertEquals(0, log.getSeverity());
    }

    @Test
    void setSeverity_WithNegativeSeverity_ShouldSetNegative() {
        log.setSeverity(-1);
        
        assertEquals(-1, log.getSeverity());
    }

    @Test
    void setSeverity_WithNullSeverity_ShouldSetNull() {
        log.setSeverity(null);
        
        assertNull(log.getSeverity());
    }

    @Test
    void setTimestamp_WithValidDateTime_ShouldSetDateTime() {
        LocalDateTime timestamp = LocalDateTime.now();
        
        log.setTimestamp(timestamp);
        
        assertEquals(timestamp, log.getTimestamp());
    }

    @Test
    void setTimestamp_WithNullDateTime_ShouldSetNull() {
        log.setTimestamp(null);
        
        assertNull(log.getTimestamp());
    }

    @Test
    void setMetadata_WithValidMetadata_ShouldSetMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        
        log.setMetadata(metadata);
        
        assertEquals(metadata, log.getMetadata());
    }

    @Test
    void setMetadata_WithNullMetadata_ShouldSetNull() {
        log.setMetadata(null);
        
        assertNull(log.getMetadata());
    }

    @Test
    void setMetadata_WithEmptyMetadata_ShouldSetEmptyMap() {
        Map<String, String> metadata = new HashMap<>();
        
        log.setMetadata(metadata);
        
        assertEquals(metadata, log.getMetadata());
        assertTrue(log.getMetadata().isEmpty());
    }

    @Test
    void logWithAllFields_ShouldMaintainAllValues() {
        String id = "507f1f77bcf86cd799439011";
        String serviceName = "auth-service";
        String eventType = "user_login";
        String userId = "user123";
        String description = "User logged in successfully";
        Integer severity = 1;
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        metadata.put("session_id", "session123");

        log.setId(id);
        log.setServiceName(serviceName);
        log.setEventType(eventType);
        log.setUserId(userId);
        log.setDescription(description);
        log.setSeverity(severity);
        log.setTimestamp(timestamp);
        log.setMetadata(metadata);

        assertEquals(id, log.getId());
        assertEquals(serviceName, log.getServiceName());
        assertEquals(eventType, log.getEventType());
        assertEquals(userId, log.getUserId());
        assertEquals(description, log.getDescription());
        assertEquals(severity, log.getSeverity());
        assertEquals(timestamp, log.getTimestamp());
        assertEquals(metadata, log.getMetadata());
        assertEquals(3, log.getMetadata().size());
        assertEquals("192.168.1.1", log.getMetadata().get("ip_address"));
        assertEquals("Mozilla/5.0", log.getMetadata().get("user_agent"));
        assertEquals("session123", log.getMetadata().get("session_id"));
    }

    @Test
    void logWithMinimalFields_ShouldWorkCorrectly() {
        String serviceName = "auth-service";
        String eventType = "user_login";

        log.setServiceName(serviceName);
        log.setEventType(eventType);

        assertEquals(serviceName, log.getServiceName());
        assertEquals(eventType, log.getEventType());
        assertNull(log.getId());
        assertNull(log.getUserId());
        assertNull(log.getDescription());
        assertNull(log.getSeverity());
        assertNotNull(log.getTimestamp());
        assertNull(log.getMetadata());
    }

    @Test
    void parameterizedConstructor_WithNullValues_ShouldSetNulls() {
        Log nullLog = new Log(null, null, null, null, null, null, null);

        assertNull(nullLog.getServiceName());
        assertNull(nullLog.getEventType());
        assertNull(nullLog.getUserId());
        assertNull(nullLog.getDescription());
        assertNull(nullLog.getSeverity());
        assertNotNull(nullLog.getTimestamp()); // Should use current time
        assertNull(nullLog.getMetadata());
    }

    @Test
    void parameterizedConstructor_WithEmptyStrings_ShouldSetEmptyStrings() {
        Log emptyLog = new Log("", "", "", "", 1, LocalDateTime.now(), new HashMap<>());

        assertEquals("", emptyLog.getServiceName());
        assertEquals("", emptyLog.getEventType());
        assertEquals("", emptyLog.getUserId());
        assertEquals("", emptyLog.getDescription());
        assertEquals(1, emptyLog.getSeverity());
        assertNotNull(emptyLog.getTimestamp());
        assertTrue(emptyLog.getMetadata().isEmpty());
    }

    @Test
    void setServiceName_WithSpecialCharacters_ShouldSetServiceName() {
        String serviceName = "auth-service@v1.0.0";
        
        log.setServiceName(serviceName);
        
        assertEquals(serviceName, log.getServiceName());
    }

    @Test
    void setEventType_WithSpecialCharacters_ShouldSetEventType() {
        String eventType = "user_login_failed";
        
        log.setEventType(eventType);
        
        assertEquals(eventType, log.getEventType());
    }

    @Test
    void setDescription_WithLongDescription_ShouldSetDescription() {
        String description = "This is a very long description that contains multiple sentences and should be handled correctly by the system without any issues.";
        
        log.setDescription(description);
        
        assertEquals(description, log.getDescription());
    }

    @Test
    void setUserId_WithLongUserId_ShouldSetUserId() {
        String userId = "user123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        
        log.setUserId(userId);
        
        assertEquals(userId, log.getUserId());
    }

    @Test
    void setServiceName_WithLongServiceName_ShouldSetServiceName() {
        String serviceName = "very-long-service-name-that-exceeds-normal-length-limits-and-should-be-handled-correctly";
        
        log.setServiceName(serviceName);
        
        assertEquals(serviceName, log.getServiceName());
    }

    @Test
    void setMetadata_WithLargeMetadata_ShouldSetMetadata() {
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            metadata.put("key" + i, "value" + i);
        }
        
        log.setMetadata(metadata);
        
        assertEquals(metadata, log.getMetadata());
        assertEquals(100, log.getMetadata().size());
    }

    @Test
    void logEquality_SameValues_ShouldBeEqual() {
        Log log1 = new Log();
        Log log2 = new Log();

        String id = "507f1f77bcf86cd799439011";
        String serviceName = "auth-service";
        String eventType = "user_login";
        String userId = "user123";
        String description = "User logged in successfully";
        Integer severity = 1;
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ip_address", "192.168.1.1");

        log1.setId(id);
        log1.setServiceName(serviceName);
        log1.setEventType(eventType);
        log1.setUserId(userId);
        log1.setDescription(description);
        log1.setSeverity(severity);
        log1.setTimestamp(timestamp);
        log1.setMetadata(metadata);

        log2.setId(id);
        log2.setServiceName(serviceName);
        log2.setEventType(eventType);
        log2.setUserId(userId);
        log2.setDescription(description);
        log2.setSeverity(severity);
        log2.setTimestamp(timestamp);
        log2.setMetadata(metadata);

        assertEquals(log1.getId(), log2.getId());
        assertEquals(log1.getServiceName(), log2.getServiceName());
        assertEquals(log1.getEventType(), log2.getEventType());
        assertEquals(log1.getUserId(), log2.getUserId());
        assertEquals(log1.getDescription(), log2.getDescription());
        assertEquals(log1.getSeverity(), log2.getSeverity());
        assertEquals(log1.getTimestamp(), log2.getTimestamp());
        assertEquals(log1.getMetadata(), log2.getMetadata());
    }

    @Test
    void toString_ShouldNotExposeSensitiveData() {
        log.setId("507f1f77bcf86cd799439011");
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        log.setUserId("user123");
        log.setDescription("User logged in successfully");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        
        String toString = log.toString();
        
        // Should not contain sensitive information like passwords
        assertFalse(toString.contains("password"));
        assertFalse(toString.contains("secret"));
        assertFalse(toString.contains("token"));
    }
} 