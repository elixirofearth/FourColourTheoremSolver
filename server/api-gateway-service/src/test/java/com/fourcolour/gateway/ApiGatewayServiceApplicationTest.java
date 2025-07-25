package com.fourcolour.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
@ActiveProfiles("test")
class ApiGatewayServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring Boot application context loads successfully
        // If the context fails to load, this test will fail
    }
}
