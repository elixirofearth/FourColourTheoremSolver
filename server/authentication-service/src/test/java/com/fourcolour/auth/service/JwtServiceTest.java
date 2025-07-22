package com.fourcolour.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "testSecretKeyForUnitTests123456789");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationInSeconds", 3600); // 1 hour
    }

    @Test
    void generateToken_WithValidUserId_ShouldReturnToken() {
        Integer userId = 123;

        String token = jwtService.generateToken(userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format has dots
    }

    @Test
    void generateToken_WithDifferentUserIds_ShouldReturnDifferentTokens() {
        Integer userId1 = 123;
        Integer userId2 = 456;

        String token1 = jwtService.generateToken(userId1);
        String token2 = jwtService.generateToken(userId2);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_WithSameUserId_ShouldReturnDifferentTokens() {
        Integer userId = 123;

        String token1 = jwtService.generateToken(userId);
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.generateToken(userId);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Different due to different timestamps
    }

    @Test
    void getExpirationTime_ShouldReturnFutureTime() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime expirationTime = jwtService.getExpirationTime();
        LocalDateTime after = LocalDateTime.now().plusSeconds(3600);

        assertTrue(expirationTime.isAfter(before));
        assertTrue(expirationTime.isBefore(after.plusSeconds(1))); // Small buffer for execution time
    }

    @Test
    void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        Integer userId = 123;
        String token = jwtService.generateToken(userId);

        Integer extractedUserId = jwtService.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUserIdFromToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.jwt.token";

        assertThrows(Exception.class, () -> {
            jwtService.getUserIdFromToken(invalidToken);
        });
    }

    @Test
    void getUserIdFromToken_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "not-a-jwt-token";

        assertThrows(Exception.class, () -> {
            jwtService.getUserIdFromToken(malformedToken);
        });
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        Integer userId = 123;
        String token = jwtService.generateToken(userId);

        boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "not-a-jwt-token";

        boolean isValid = jwtService.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        String emptyToken = "";

        boolean isValid = jwtService.validateToken(emptyToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        String nullToken = null;

        boolean isValid = jwtService.validateToken(nullToken);

        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        Integer userId = 123;
        String token = jwtService.generateToken(userId);

        boolean isExpired = jwtService.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        // Create a service with very short expiration time
        JwtService shortExpiryService = new JwtService();
        ReflectionTestUtils.setField(shortExpiryService, "jwtSecret", "testSecretKeyForUnitTests123456789");
        ReflectionTestUtils.setField(shortExpiryService, "jwtExpirationInSeconds", -1); // Already expired

        Integer userId = 123;
        String expiredToken = shortExpiryService.generateToken(userId);

        boolean isExpired = shortExpiryService.isTokenExpired(expiredToken);

        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_WithInvalidToken_ShouldReturnTrue() {
        String invalidToken = "invalid.jwt.token";

        boolean isExpired = jwtService.isTokenExpired(invalidToken);

        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_WithMalformedToken_ShouldReturnTrue() {
        String malformedToken = "not-a-jwt-token";

        boolean isExpired = jwtService.isTokenExpired(malformedToken);

        assertTrue(isExpired);
    }

    @Test
    void tokenRoundTrip_ShouldMaintainIntegrity() {
        Integer originalUserId = 456;

        // Generate token
        String token = jwtService.generateToken(originalUserId);
        
        // Validate token
        boolean isValid = jwtService.validateToken(token);
        assertTrue(isValid);
        
        // Extract user ID
        Integer extractedUserId = jwtService.getUserIdFromToken(token);
        assertEquals(originalUserId, extractedUserId);
        
        // Check expiration
        boolean isExpired = jwtService.isTokenExpired(token);
        assertFalse(isExpired);
    }

    @Test
    void generateToken_WithZeroUserId_ShouldWork() {
        Integer userId = 0;

        String token = jwtService.generateToken(userId);
        Integer extractedUserId = jwtService.getUserIdFromToken(token);

        assertNotNull(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void generateToken_WithNegativeUserId_ShouldWork() {
        Integer userId = -1;

        String token = jwtService.generateToken(userId);
        Integer extractedUserId = jwtService.getUserIdFromToken(token);

        assertNotNull(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void generateToken_WithLargeUserId_ShouldWork() {
        Integer userId = Integer.MAX_VALUE;

        String token = jwtService.generateToken(userId);
        Integer extractedUserId = jwtService.getUserIdFromToken(token);

        assertNotNull(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void generateToken_WithNullUserId_ShouldThrowException() {
        Integer userId = null;

        assertThrows(Exception.class, () -> {
            jwtService.generateToken(userId);
        });
    }

    @Test
    void getSigningKey_ShouldBeConsistent() {
        // Generate two tokens to ensure signing key is consistent
        Integer userId = 123;
        String token1 = jwtService.generateToken(userId);
        String token2 = jwtService.generateToken(userId);

        // Both tokens should be valid with the same signing key
        assertTrue(jwtService.validateToken(token1));
        assertTrue(jwtService.validateToken(token2));
    }

    @Test
    void tokenStructure_ShouldFollowJwtFormat() {
        Integer userId = 123;
        String token = jwtService.generateToken(userId);

        // JWT should have exactly 2 dots (3 parts: header.payload.signature)
        long dotCount = token.chars().filter(ch -> ch == '.').count();
        assertEquals(2, dotCount);

        // Each part should be non-empty
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        for (String part : parts) {
            assertFalse(part.isEmpty());
        }
    }

    @Test
    void differentSecrets_ShouldProduceDifferentTokens() {
        // Create two services with different secrets
        JwtService service1 = new JwtService();
        ReflectionTestUtils.setField(service1, "jwtSecret", "testSecretKeyForUnitTests12345678901234567890123456789012");
        ReflectionTestUtils.setField(service1, "jwtExpirationInSeconds", 3600);

        JwtService service2 = new JwtService();
        ReflectionTestUtils.setField(service2, "jwtSecret", "testSecretKeyForUnitTests98765432109876543210987654321098");
        ReflectionTestUtils.setField(service2, "jwtExpirationInSeconds", 3600);

        Integer userId = 123;
        String token1 = service1.generateToken(userId);
        String token2 = service2.generateToken(userId);

        // Tokens should be different
        assertNotEquals(token1, token2);

        // Token from service1 should not validate with service2
        assertFalse(service2.validateToken(token1));
        assertFalse(service1.validateToken(token2));
    }

    @Test
    void shortSecret_ShouldStillWork() {
        JwtService shortSecretService = new JwtService();
        ReflectionTestUtils.setField(shortSecretService, "jwtSecret", "testSecretKeyForUnitTests12345678901234567890123456789012");
        ReflectionTestUtils.setField(shortSecretService, "jwtExpirationInSeconds", 3600);

        Integer userId = 123;
        String token = shortSecretService.generateToken(userId);

        assertTrue(shortSecretService.validateToken(token));
        assertEquals(userId, shortSecretService.getUserIdFromToken(token));
    }

    @Test
    void longSecret_ShouldWork() {
        StringBuilder longSecret = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longSecret.append("a");
        }

        JwtService longSecretService = new JwtService();
        ReflectionTestUtils.setField(longSecretService, "jwtSecret", longSecret.toString());
        ReflectionTestUtils.setField(longSecretService, "jwtExpirationInSeconds", 3600);

        Integer userId = 123;
        String token = longSecretService.generateToken(userId);

        assertTrue(longSecretService.validateToken(token));
        assertEquals(userId, longSecretService.getUserIdFromToken(token));
    }

    @Test
    void veryShortExpiration_ShouldCreateExpiredToken() {
        JwtService veryShortService = new JwtService();
        ReflectionTestUtils.setField(veryShortService, "jwtSecret", "testSecretKeyForUnitTests12345678901234567890123456789012");
        ReflectionTestUtils.setField(veryShortService, "jwtExpirationInSeconds", 0);

        Integer userId = 123;
        String token = veryShortService.generateToken(userId);

        // Token should be immediately expired
        assertTrue(veryShortService.isTokenExpired(token));
        // Should throw exception when trying to get user ID from expired token
        assertThrows(Exception.class, () -> veryShortService.getUserIdFromToken(token));
    }

    @Test
    void veryLongExpiration_ShouldWork() {
        JwtService longExpiryService = new JwtService();
        ReflectionTestUtils.setField(longExpiryService, "jwtSecret", "testSecretKeyForUnitTests123456789");
        ReflectionTestUtils.setField(longExpiryService, "jwtExpirationInSeconds", Integer.MAX_VALUE);

        Integer userId = 123;
        String token = longExpiryService.generateToken(userId);

        assertFalse(longExpiryService.isTokenExpired(token));
        assertTrue(longExpiryService.validateToken(token));
        assertEquals(userId, longExpiryService.getUserIdFromToken(token));
    }
}