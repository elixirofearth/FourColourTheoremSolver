package com.fourcolour.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenCacheService tokenCacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testRedisConnection_WithSuccessfulConnection_ShouldLogSuccess() {
        when(valueOperations.get("test:connection")).thenReturn("success");

        // This method should not throw an exception
        assertDoesNotThrow(() -> tokenCacheService.testRedisConnection());

        verify(valueOperations).set("test:connection", "success", 10, TimeUnit.SECONDS);
        verify(valueOperations).get("test:connection");
    }

    @Test
    void testRedisConnection_WithFailedConnection_ShouldHandleException() {
        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // This method should not throw an exception even if Redis fails
        assertDoesNotThrow(() -> tokenCacheService.testRedisConnection());
    }

    @Test
    void cacheToken_WithValidToken_ShouldCacheSuccessfully() {
        String token = "Bearer valid-token";
        boolean isValid = true;

        tokenCacheService.cacheToken(token, isValid);

        verify(valueOperations).set("token:Bearer valid-token", true, 15L, TimeUnit.MINUTES);
    }

    @Test
    void cacheToken_WithInvalidToken_ShouldCacheSuccessfully() {
        String token = "Bearer invalid-token";
        boolean isValid = false;

        tokenCacheService.cacheToken(token, isValid);

        verify(valueOperations).set("token:Bearer invalid-token", false, 15L, TimeUnit.MINUTES);
    }

    @Test
    void cacheToken_WithRedisException_ShouldHandleGracefully() {
        String token = "Bearer token";
        doThrow(new RuntimeException("Redis error"))
                .when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // Should not throw exception
        assertDoesNotThrow(() -> tokenCacheService.cacheToken(token, true));
    }

    @Test
    void getCachedTokenValidation_WithCachedValidToken_ShouldReturnTrue() {
        String token = "Bearer valid-token";
        when(valueOperations.get("token:Bearer valid-token")).thenReturn(true);

        Boolean result = tokenCacheService.getCachedTokenValidation(token);

        assertTrue(result);
        verify(valueOperations).get("token:Bearer valid-token");
    }

    @Test
    void getCachedTokenValidation_WithCachedInvalidToken_ShouldReturnFalse() {
        String token = "Bearer invalid-token";
        when(valueOperations.get("token:Bearer invalid-token")).thenReturn(false);

        Boolean result = tokenCacheService.getCachedTokenValidation(token);

        assertFalse(result);
        verify(valueOperations).get("token:Bearer invalid-token");
    }

    @Test
    void getCachedTokenValidation_WithNoCachedToken_ShouldReturnNull() {
        String token = "Bearer uncached-token";
        when(valueOperations.get("token:Bearer uncached-token")).thenReturn(null);

        Boolean result = tokenCacheService.getCachedTokenValidation(token);

        assertNull(result);
        verify(valueOperations).get("token:Bearer uncached-token");
    }

    @Test
    void getCachedTokenValidation_WithRedisException_ShouldReturnNull() {
        String token = "Bearer token";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        Boolean result = tokenCacheService.getCachedTokenValidation(token);

        assertNull(result);
    }

    @Test
    void invalidateToken_WithValidToken_ShouldDeleteFromCache() {
        String token = "Bearer token-to-invalidate";

        tokenCacheService.invalidateToken(token);

        verify(redisTemplate).delete("token:Bearer token-to-invalidate");
    }

    @Test
    void invalidateToken_WithRedisException_ShouldHandleGracefully() {
        String token = "Bearer token";
        when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Should not throw exception
        assertDoesNotThrow(() -> tokenCacheService.invalidateToken(token));
    }

    @Test
    void isRateLimited_WithFirstRequest_ShouldReturnFalseAndSetCounter() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(null);

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertFalse(result);
        verify(valueOperations).set("rate_limit:192.168.1.1", 1L, 60L, TimeUnit.SECONDS);
    }

    @Test
    void isRateLimited_WithCountBelowLimit_ShouldReturnFalseAndIncrement() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(50);

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertFalse(result);
        verify(valueOperations).increment("rate_limit:192.168.1.1");
    }

    @Test
    void isRateLimited_WithCountAtLimit_ShouldReturnTrue() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(100);

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertTrue(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void isRateLimited_WithCountAboveLimit_ShouldReturnTrue() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(150);

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertTrue(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void isRateLimited_WithLongValue_ShouldHandleCorrectly() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(50L);

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertFalse(result);
        verify(valueOperations).increment("rate_limit:192.168.1.1");
    }

    @Test
    void isRateLimited_WithStringValue_ShouldParseAndHandle() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn("75");

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertFalse(result);
        verify(valueOperations).increment("rate_limit:192.168.1.1");
    }

    @Test
    void isRateLimited_WithRedisException_ShouldReturnFalse() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        boolean result = tokenCacheService.isRateLimited(ipAddress);

        assertFalse(result);
    }

    @Test
    void getCurrentRequestCount_WithExistingCount_ShouldReturnCount() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(42);

        int result = tokenCacheService.getCurrentRequestCount(ipAddress);

        assertEquals(42, result);
    }

    @Test
    void getCurrentRequestCount_WithNoCount_ShouldReturnZero() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(null);

        int result = tokenCacheService.getCurrentRequestCount(ipAddress);

        assertEquals(0, result);
    }

    @Test
    void getCurrentRequestCount_WithLongValue_ShouldConvertToInt() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn(42L);

        int result = tokenCacheService.getCurrentRequestCount(ipAddress);

        assertEquals(42, result);
    }

    @Test
    void getCurrentRequestCount_WithStringValue_ShouldParseToInt() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get("rate_limit:192.168.1.1")).thenReturn("42");

        int result = tokenCacheService.getCurrentRequestCount(ipAddress);

        assertEquals(42, result);
    }

    @Test
    void getCurrentRequestCount_WithRedisException_ShouldReturnZero() {
        String ipAddress = "192.168.1.1";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        int result = tokenCacheService.getCurrentRequestCount(ipAddress);

        assertEquals(0, result);
    }

    // Add setter method for testing
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
