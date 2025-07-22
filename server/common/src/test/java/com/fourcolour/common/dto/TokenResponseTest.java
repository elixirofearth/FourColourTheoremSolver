package com.fourcolour.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class TokenResponseTest {

    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        tokenResponse = new TokenResponse("test-token", "John Doe", 123, "john@example.com", "2024-12-31T23:59:59");
    }

    @Test
    void testDefaultConstructor() {
        TokenResponse response = new TokenResponse();
        assertNotNull(response);
        assertNull(response.getToken());
        assertNull(response.getName());
        assertNull(response.getUserId());
        assertNull(response.getEmail());
        assertNull(response.getExpiresAt());
    }

    @Test
    void testParameterizedConstructor() {
        assertNotNull(tokenResponse);
        assertEquals("test-token", tokenResponse.getToken());
        assertEquals("John Doe", tokenResponse.getName());
        assertEquals(123, tokenResponse.getUserId());
        assertEquals("john@example.com", tokenResponse.getEmail());
        assertEquals("2024-12-31T23:59:59", tokenResponse.getExpiresAt());
    }

    @Test
    void testGetToken() {
        assertEquals("test-token", tokenResponse.getToken());
    }

    @Test
    void testSetToken() {
        tokenResponse.setToken("new-token");
        assertEquals("new-token", tokenResponse.getToken());
    }

    @Test
    void testGetName() {
        assertEquals("John Doe", tokenResponse.getName());
    }

    @Test
    void testSetName() {
        tokenResponse.setName("Jane Smith");
        assertEquals("Jane Smith", tokenResponse.getName());
    }

    @Test
    void testGetUserId() {
        assertEquals(123, tokenResponse.getUserId());
    }

    @Test
    void testSetUserId() {
        tokenResponse.setUserId(456);
        assertEquals(456, tokenResponse.getUserId());
    }

    @Test
    void testGetEmail() {
        assertEquals("john@example.com", tokenResponse.getEmail());
    }

    @Test
    void testSetEmail() {
        tokenResponse.setEmail("jane@example.com");
        assertEquals("jane@example.com", tokenResponse.getEmail());
    }

    @Test
    void testGetExpiresAt() {
        assertEquals("2024-12-31T23:59:59", tokenResponse.getExpiresAt());
    }

    @Test
    void testSetExpiresAt() {
        tokenResponse.setExpiresAt("2025-01-01T00:00:00");
        assertEquals("2025-01-01T00:00:00", tokenResponse.getExpiresAt());
    }

    @Test
    void testTokenResponseWithNullValues() {
        TokenResponse response = new TokenResponse(null, null, null, null, null);
        assertNull(response.getToken());
        assertNull(response.getName());
        assertNull(response.getUserId());
        assertNull(response.getEmail());
        assertNull(response.getExpiresAt());
    }

    @Test
    void testTokenResponseWithEmptyValues() {
        TokenResponse response = new TokenResponse("", "", 0, "", "");
        assertEquals("", response.getToken());
        assertEquals("", response.getName());
        assertEquals(0, response.getUserId());
        assertEquals("", response.getEmail());
        assertEquals("", response.getExpiresAt());
    }

    @Test
    void testSetNullValues() {
        tokenResponse.setToken(null);
        tokenResponse.setName(null);
        tokenResponse.setUserId(null);
        tokenResponse.setEmail(null);
        tokenResponse.setExpiresAt(null);
        
        assertNull(tokenResponse.getToken());
        assertNull(tokenResponse.getName());
        assertNull(tokenResponse.getUserId());
        assertNull(tokenResponse.getEmail());
        assertNull(tokenResponse.getExpiresAt());
    }

    @Test
    void testJsonPropertyAnnotations() throws Exception {
        // Test that the JsonProperty annotations are present
        JsonProperty userIdAnnotation = TokenResponse.class.getDeclaredField("userId").getAnnotation(JsonProperty.class);
        JsonProperty expiresAtAnnotation = TokenResponse.class.getDeclaredField("expiresAt").getAnnotation(JsonProperty.class);
        
        assertNotNull(userIdAnnotation);
        assertEquals("user_id", userIdAnnotation.value());
        
        assertNotNull(expiresAtAnnotation);
        assertEquals("expires_at", expiresAtAnnotation.value());
    }
} 