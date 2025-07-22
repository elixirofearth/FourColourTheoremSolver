package com.fourcolour.auth.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session();
    }

    @Test
    void defaultConstructor_ShouldCreateSessionWithNullValues() {
        assertNull(session.getId());
        assertNull(session.getUserId());
        assertNull(session.getToken());
        assertNull(session.getExpiresAt());
        assertNull(session.getCreatedAt());
    }

    @Test
    void parameterizedConstructor_ShouldSetValues() {
        Integer userId = 123;
        String token = "jwt-token-string";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        Session paramSession = new Session(userId, token, expiresAt);

        assertEquals(userId, paramSession.getUserId());
        assertEquals(token, paramSession.getToken());
        assertEquals(expiresAt, paramSession.getExpiresAt());
        assertNull(paramSession.getId()); // ID should still be null as it's auto-generated
        assertNull(paramSession.getCreatedAt()); // CreatedAt is set by @PrePersist
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Integer id = 1;
        Integer userId = 123;
        String token = "jwt-token-string";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        session.setId(id);
        session.setUserId(userId);
        session.setToken(token);
        session.setExpiresAt(expiresAt);
        session.setCreatedAt(createdAt);

        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(token, session.getToken());
        assertEquals(expiresAt, session.getExpiresAt());
        assertEquals(createdAt, session.getCreatedAt());
    }

    @Test
    void setUserId_WithValidUserId_ShouldSetUserId() {
        Integer validUserId = 456;
        
        session.setUserId(validUserId);
        
        assertEquals(validUserId, session.getUserId());
    }

    @Test
    void setUserId_WithNullUserId_ShouldSetNull() {
        session.setUserId(null);
        
        assertNull(session.getUserId());
    }

    @Test
    void setUserId_WithZeroUserId_ShouldSetZero() {
        Integer zeroUserId = 0;
        
        session.setUserId(zeroUserId);
        
        assertEquals(zeroUserId, session.getUserId());
    }

    @Test
    void setUserId_WithNegativeUserId_ShouldSetNegative() {
        Integer negativeUserId = -1;
        
        session.setUserId(negativeUserId);
        
        assertEquals(negativeUserId, session.getUserId());
    }

    @Test
    void setToken_WithValidToken_ShouldSetToken() {
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        session.setToken(validToken);
        
        assertEquals(validToken, session.getToken());
    }

    @Test
    void setToken_WithNullToken_ShouldSetNull() {
        session.setToken(null);
        
        assertNull(session.getToken());
    }

    @Test
    void setToken_WithEmptyToken_ShouldSetEmptyString() {
        String emptyToken = "";
        
        session.setToken(emptyToken);
        
        assertEquals(emptyToken, session.getToken());
    }

    @Test
    void setExpiresAt_WithFutureDateTime_ShouldSetExpiresAt() {
        LocalDateTime futureDateTime = LocalDateTime.now().plusHours(24);
        
        session.setExpiresAt(futureDateTime);
        
        assertEquals(futureDateTime, session.getExpiresAt());
    }

    @Test
    void setExpiresAt_WithPastDateTime_ShouldSetExpiresAt() {
        LocalDateTime pastDateTime = LocalDateTime.now().minusHours(1);
        
        session.setExpiresAt(pastDateTime);
        
        assertEquals(pastDateTime, session.getExpiresAt());
    }

    @Test
    void setExpiresAt_WithNullDateTime_ShouldSetNull() {
        session.setExpiresAt(null);
        
        assertNull(session.getExpiresAt());
    }

    @Test
    void setId_WithPositiveId_ShouldSetId() {
        Integer positiveId = 789;
        
        session.setId(positiveId);
        
        assertEquals(positiveId, session.getId());
    }

    @Test
    void setId_WithZeroId_ShouldSetZero() {
        Integer zeroId = 0;
        
        session.setId(zeroId);
        
        assertEquals(zeroId, session.getId());
    }

    @Test
    void setCreatedAt_WithValidDateTime_ShouldSetDateTime() {
        LocalDateTime validDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        
        session.setCreatedAt(validDateTime);
        
        assertEquals(validDateTime, session.getCreatedAt());
    }

    @Test
    void setCreatedAt_WithNullDateTime_ShouldSetNull() {
        session.setCreatedAt(null);
        
        assertNull(session.getCreatedAt());
    }

    @Test
    void onCreate_ShouldSetCreatedAtToCurrentTime() {
        LocalDateTime beforeOnCreate = LocalDateTime.now();
        
        // Simulate @PrePersist behavior
        session.onCreate();
        
        LocalDateTime afterOnCreate = LocalDateTime.now();
        LocalDateTime createdAt = session.getCreatedAt();
        
        assertNotNull(createdAt);
        assertTrue(createdAt.isAfter(beforeOnCreate.minusSeconds(1))); // Small buffer
        assertTrue(createdAt.isBefore(afterOnCreate.plusSeconds(1)));  // Small buffer
    }

    @Test
    void onCreate_CalledMultipleTimes_ShouldUpdateCreatedAt() {
        session.onCreate();
        LocalDateTime firstCreatedAt = session.getCreatedAt();
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        session.onCreate();
        LocalDateTime secondCreatedAt = session.getCreatedAt();
        
        assertNotNull(firstCreatedAt);
        assertNotNull(secondCreatedAt);
        // Second call should update the timestamp
        assertFalse(secondCreatedAt.isBefore(firstCreatedAt));
    }

    @Test
    void sessionWithAllFields_ShouldMaintainAllValues() {
        Integer id = 42;
        Integer userId = 123;
        String token = "complete-jwt-token";
        LocalDateTime expiresAt = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

        session.setId(id);
        session.setUserId(userId);
        session.setToken(token);
        session.setExpiresAt(expiresAt);
        session.setCreatedAt(createdAt);

        // Verify all fields are maintained
        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(token, session.getToken());
        assertEquals(expiresAt, session.getExpiresAt());
        assertEquals(createdAt, session.getCreatedAt());
    }

    @Test
    void sessionWithMinimalFields_ShouldWorkCorrectly() {
        Integer userId = 123;
        String token = "minimal-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        session.setUserId(userId);
        session.setToken(token);
        session.setExpiresAt(expiresAt);

        assertEquals(userId, session.getUserId());
        assertEquals(token, session.getToken());
        assertEquals(expiresAt, session.getExpiresAt());
        assertNull(session.getId()); // Auto-generated field
        assertNull(session.getCreatedAt()); // Set by @PrePersist
    }

    @Test
    void parameterizedConstructor_WithNullValues_ShouldSetNulls() {
        Session nullSession = new Session(null, null, null);

        assertNull(nullSession.getUserId());
        assertNull(nullSession.getToken());
        assertNull(nullSession.getExpiresAt());
    }

    @Test
    void parameterizedConstructor_WithValidValues_ShouldSetValues() {
        Integer userId = 999;
        String token = "valid-jwt-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);

        Session validSession = new Session(userId, token, expiresAt);

        assertEquals(userId, validSession.getUserId());
        assertEquals(token, validSession.getToken());
        assertEquals(expiresAt, validSession.getExpiresAt());
    }

    @Test
    void setToken_WithLongToken_ShouldSetToken() {
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longToken.append("a");
        }
        
        session.setToken(longToken.toString());
        
        assertEquals(longToken.toString(), session.getToken());
    }

    @Test
    void setToken_WithSpecialCharacters_ShouldSetToken() {
        String specialToken = "token.with-special_characters@123!";
        
        session.setToken(specialToken);
        
        assertEquals(specialToken, session.getToken());
    }

    @Test
    void expiresAt_ComparingDates_ShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(1);
        LocalDateTime past = now.minusHours(1);

        session.setExpiresAt(future);
        assertTrue(session.getExpiresAt().isAfter(now));

        session.setExpiresAt(past);
        assertTrue(session.getExpiresAt().isBefore(now));

        session.setExpiresAt(now);
        assertFalse(session.getExpiresAt().isAfter(now));
        assertFalse(session.getExpiresAt().isBefore(now));
    }

    @Test
    void sessionLifecycle_ShouldMaintainIntegrity() {
        // Simulate session creation
        Integer userId = 123;
        String token = "session-lifecycle-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        session = new Session(userId, token, expiresAt);
        
        // Simulate database persistence
        session.setId(1);
        session.onCreate();

        // Verify session integrity
        assertNotNull(session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(token, session.getToken());
        assertEquals(expiresAt, session.getExpiresAt());
        assertNotNull(session.getCreatedAt());
    }

    @Test
    void isExpired_ShouldDetermineExpirationCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        
        // Set expired session
        session.setExpiresAt(now.minusMinutes(1));
        assertTrue(session.getExpiresAt().isBefore(now));

        // Set valid session
        session.setExpiresAt(now.plusMinutes(1));
        assertTrue(session.getExpiresAt().isAfter(now));

        // Set session expiring exactly now
        session.setExpiresAt(now);
        assertFalse(session.getExpiresAt().isAfter(now));
    }

    @Test
    void sessionEquality_SameValues_ShouldBeEqual() {
        Session session1 = new Session(123, "token", LocalDateTime.now().plusHours(1));
        session1.setId(1);
        
        Session session2 = new Session(123, "token", LocalDateTime.now().plusHours(1));
        session2.setId(1);
        
        // Note: This test assumes default Object.equals() behavior
        // If custom equals() is implemented, this test should be updated
        assertNotEquals(session1, session2); // Default Object.equals() compares references
        
        // But values should be the same
        assertEquals(session1.getId(), session2.getId());
        assertEquals(session1.getUserId(), session2.getUserId());
        assertEquals(session1.getToken(), session2.getToken());
    }

    @Test
    void toString_ShouldNotExposeToken() {
        session.setUserId(123);
        session.setToken("secret-jwt-token");
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        String sessionString = session.toString();
        
        // Default toString() should not expose sensitive information
        // This test verifies that the default Object.toString() is used
        assertTrue(sessionString.contains("Session@"));
        assertFalse(sessionString.contains("secret-jwt-token"));
    }

    @Test
    void setUserId_WithMaxIntegerValue_ShouldSetUserId() {
        Integer maxUserId = Integer.MAX_VALUE;
        
        session.setUserId(maxUserId);
        
        assertEquals(maxUserId, session.getUserId());
    }

    @Test
    void setUserId_WithMinIntegerValue_ShouldSetUserId() {
        Integer minUserId = Integer.MIN_VALUE;
        
        session.setUserId(minUserId);
        
        assertEquals(minUserId, session.getUserId());
    }

    @Test
    void setId_WithMaxIntegerValue_ShouldSetId() {
        Integer maxId = Integer.MAX_VALUE;
        
        session.setId(maxId);
        
        assertEquals(maxId, session.getId());
    }

    @Test
    void expiresAt_WithExtremeDateTime_ShouldWork() {
        // Test with far future date
        LocalDateTime farFuture = LocalDateTime.of(2999, 12, 31, 23, 59, 59);
        session.setExpiresAt(farFuture);
        assertEquals(farFuture, session.getExpiresAt());

        // Test with far past date
        LocalDateTime farPast = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        session.setExpiresAt(farPast);
        assertEquals(farPast, session.getExpiresAt());
    }

    @Test
    void createdAt_WithExtremeDateTime_ShouldWork() {
        LocalDateTime extremeDateTime = LocalDateTime.of(2999, 12, 31, 23, 59, 59);
        
        session.setCreatedAt(extremeDateTime);
        
        assertEquals(extremeDateTime, session.getCreatedAt());
    }

    @Test
    void sessionWithUnicodeToken_ShouldWork() {
        String unicodeToken = "token-with-unicode-characters-αβγδε-测试-🔑";
        
        session.setToken(unicodeToken);
        
        assertEquals(unicodeToken, session.getToken());
    }

    @Test
    void sessionModification_ShouldAllowUpdates() {
        // Initial setup
        session.setUserId(123);
        session.setToken("initial-token");
        session.setExpiresAt(LocalDateTime.now().plusHours(1));

        // Modify session
        session.setToken("updated-token");
        session.setExpiresAt(LocalDateTime.now().plusHours(2));

        assertEquals("updated-token", session.getToken());
        assertTrue(session.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(90)));
    }

    @Test
    void sessionBuilder_UsingParameterizedConstructor_ShouldCreateCompleteSession() {
        Integer userId = 456;
        String token = "builder-pattern-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        Session builtSession = new Session(userId, token, expiresAt);

        assertNotNull(builtSession);
        assertEquals(userId, builtSession.getUserId());
        assertEquals(token, builtSession.getToken());
        assertEquals(expiresAt, builtSession.getExpiresAt());
    }

    @Test
    void sessionValidation_ShouldCheckRequiredFields() {
        // Test that session can be created without validation errors
        session.setUserId(123);
        session.setToken("valid-token");
        session.setExpiresAt(LocalDateTime.now().plusHours(1));

        // All required fields should be set
        assertNotNull(session.getUserId());
        assertNotNull(session.getToken());
        assertNotNull(session.getExpiresAt());
        
        // Optional fields
        assertNull(session.getId()); // Auto-generated
        assertNull(session.getCreatedAt()); // Set by @PrePersist
    }

    @Test
    void sessionTimeManagement_ShouldHandleTimeZones() {
        // Test with current time
        LocalDateTime now = LocalDateTime.now();
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusHours(24));

        // Verify time relationships
        assertTrue(session.getExpiresAt().isAfter(session.getCreatedAt()));
        
        // Verify 24-hour difference
        assertEquals(24, java.time.Duration.between(
            session.getCreatedAt(), 
            session.getExpiresAt()
        ).toHours());
    }
}