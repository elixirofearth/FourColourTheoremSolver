package com.fourcolour.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class TokenCacheService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCacheService.class);
    
    private static final String TOKEN_CACHE_PREFIX = "token:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final Duration TOKEN_CACHE_TTL = Duration.ofMinutes(15); // Cache tokens for 15 minutes
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1); // 1 minute window for rate limiting
    private static final int MAX_REQUESTS_PER_MINUTE = 100; // Max requests per minute per IP

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void testRedisConnection() {
        try {
            logger.info("Testing Redis connection...");
            redisTemplate.opsForValue().set("test:connection", "success", 10, TimeUnit.SECONDS);
            Object result = redisTemplate.opsForValue().get("test:connection");
            logger.info("Redis connection test successful: {}", result);
        } catch (Exception e) {
            logger.error("Redis connection test failed: {}", e.getMessage());
        }
    }

    /**
     * Cache a validated token
     */
    public void cacheToken(String token, boolean isValid) {
        try {
            String key = TOKEN_CACHE_PREFIX + token;
            redisTemplate.opsForValue().set(key, isValid, TOKEN_CACHE_TTL.toMinutes(), TimeUnit.MINUTES);
            logger.debug("Cached token validation result: {}", isValid);
        } catch (Exception e) {
            logger.warn("Failed to cache token: {}", e.getMessage());
        }
    }

    /**
     * Get cached token validation result
     */
    public Boolean getCachedTokenValidation(String token) {
        try {
            String key = TOKEN_CACHE_PREFIX + token;
            Object result = redisTemplate.opsForValue().get(key);
            if (result != null) {
                logger.debug("Token validation result found in cache");
                return (Boolean) result;
            }
        } catch (Exception e) {
            logger.warn("Failed to get cached token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Invalidate a cached token (e.g., on logout)
     */
    public void invalidateToken(String token) {
        try {
            String key = TOKEN_CACHE_PREFIX + token;
            redisTemplate.delete(key);
            logger.debug("Invalidated cached token");
        } catch (Exception e) {
            logger.warn("Failed to invalidate cached token: {}", e.getMessage());
        }
    }

    /**
     * Check rate limiting for an IP address
     */
    public boolean isRateLimited(String ipAddress) {
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            Object currentCount = redisTemplate.opsForValue().get(key);
            
            if (currentCount == null) {
                // First request in this window
                redisTemplate.opsForValue().set(key, 1L, RATE_LIMIT_WINDOW.toSeconds(), TimeUnit.SECONDS);
                logger.debug("First request for IP: {}, setting counter to 1", ipAddress);
                return false;
            }
            
            Long count = (Long) currentCount;
            logger.debug("Current request count for IP {}: {}", ipAddress, count);
            
            if (count >= MAX_REQUESTS_PER_MINUTE) {
                logger.warn("Rate limit exceeded for IP: {}", ipAddress);
                return true;
            }
            
            // Increment counter
            redisTemplate.opsForValue().increment(key);
            logger.debug("Incremented counter for IP: {}, new count: {}", ipAddress, count + 1);
            return false;
            
        } catch (Exception e) {
            logger.warn("Failed to check rate limit: {}", e.getMessage());
            return false; // Allow request if Redis is down
        }
    }

    /**
     * Get current request count for an IP address
     */
    public int getCurrentRequestCount(String ipAddress) {
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? ((Long) count).intValue() : 0;
        } catch (Exception e) {
            logger.warn("Failed to get request count: {}", e.getMessage());
            return 0;
        }
    }
} 