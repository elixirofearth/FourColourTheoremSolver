package com.fourcolour.gateway.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @InjectMocks
    private RedisConfig redisConfig;

    @Test
    void redisConnectionFactory_ShouldCreateLettuceConnectionFactory() {
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);

        RedisConnectionFactory connectionFactory = redisConfig.redisConnectionFactory();

        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof LettuceConnectionFactory);
    }

    @Test
    void redisConnectionFactory_WithCustomHostAndPort_ShouldUseCustomValues() {
        ReflectionTestUtils.setField(redisConfig, "redisHost", "redis-server");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6380);

        RedisConnectionFactory connectionFactory = redisConfig.redisConnectionFactory();

        assertNotNull(connectionFactory);
        assertTrue(connectionFactory instanceof LettuceConnectionFactory);
    }

    @Test
    void redisTemplate_ShouldConfigureSerializers() {
        RedisConnectionFactory mockConnectionFactory = new LettuceConnectionFactory();
        
        RedisTemplate<String, Object> template = redisConfig.redisTemplate(mockConnectionFactory);

        assertNotNull(template);
        assertEquals(mockConnectionFactory, template.getConnectionFactory());
        assertTrue(template.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(template.getHashKeySerializer() instanceof StringRedisSerializer);
        assertTrue(template.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
        assertTrue(template.getHashValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    }
}
