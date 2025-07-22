package com.fourcolour.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@Configuration
class TestConfig {
    
    @Bean
    @Primary
    public RestTemplate restTemplateForMock() {
        return new RestTemplate();
    }
}

/**
 * Integration Test using MockRestServiceServer instead of WireMock
 * This tests the full gateway with real Spring context but mocked external services
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestTemplate restTemplateForMock;

    @Autowired
    private com.fourcolour.gateway.service.ProxyService proxyService;

    private ObjectMapper objectMapper;
    private String baseUrl;
    private MockRestServiceServer mockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("services.authentication.url", () -> "http://auth-service:8081");
        registry.add("services.map-storage.url", () -> "http://map-service:8083");
        registry.add("services.coloring.url", () -> "http://solver-service:8082");
        registry.add("spring.redis.host", () -> "localhost");
        registry.add("spring.redis.port", () -> "6379");
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:" + port;
        mockServer = MockRestServiceServer.createServer(restTemplateForMock);
        // Invalidate cached tokens to ensure fresh authentication checks
        proxyService.invalidateCachedToken("Bearer valid-token");
        proxyService.invalidateCachedToken("Bearer invalid-token");
    }

    // ==================== HEALTH CHECK TESTS ====================

    @Test
    @DisplayName("Application should start and health check should work")
    void applicationShouldStartAndHealthCheckShouldWork() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/healthcheck", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    @DisplayName("Services health check should return all service statuses")
    void servicesHealthCheck_ShouldReturnAllServiceStatuses() {
        // Mock external service responses
        mockServer.expect(requestTo("http://auth-service:8081/auth/healthcheck"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps/healthcheck"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        mockServer.expect(requestTo("http://solver-service:8082/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/healthcheck/services", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String body = response.getBody();
        assertTrue(body.contains("\"gateway\": \"OK\""));
        assertTrue(body.contains("\"authentication-service\": \"OK\""));
        assertTrue(body.contains("\"map-storage-service\": \"OK\""));
        assertTrue(body.contains("\"solver-service\": \"OK\""));

        mockServer.verify();
    }

    // ==================== AUTHENTICATION FLOW TESTS ====================

    @Test
    @DisplayName("End-to-end registration should work with mocked auth service")
    void endToEndRegistration_ShouldWorkWithMockedAuthService() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "testuser", "email", "test@example.com", "password", "password123")
        );

        // Mock auth service response
        mockServer.expect(requestTo("http://auth-service:8081/auth/register"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"testuser\"}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("testuser"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("End-to-end login should work with mocked auth service")
    void endToEndLogin_ShouldWorkWithMockedAuthService() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("email", "test@example.com", "password", "password123")
        );

        // Mock auth service response
        mockServer.expect(requestTo("http://auth-service:8081/auth/login"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"token\":\"jwt-token-here\",\"user\":{\"id\":1,\"name\":\"testuser\"}}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("jwt-token-here"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Logout should invalidate token and forward to auth service")
    void logout_ShouldInvalidateTokenAndForwardToAuthService() {
        // Mock auth service response
        mockServer.expect(requestTo("http://auth-service:8081/auth/logout"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"message\":\"Logged out successfully\"}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/logout", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Logged out successfully"));
        
        mockServer.verify();
    }

    // ==================== MAP COLORING TESTS ====================

    @Test
    @DisplayName("Color map without auth should return unauthorized")
    void colorMap_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps/color", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
    }

    @Test
    @DisplayName("Color map with valid auth should forward to solver service")
    void colorMap_WithValidAuth_ShouldForwardToSolverService() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600, "userId", "user123")
        );

        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock solver service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://solver-service:8082/api/solve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"coloredImage\":{\"data\":[1,2,3,4]},\"colors\":4,\"processingTime\":150}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps/color", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("coloredImage"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Color map with invalid auth should return unauthorized")
    void colorMap_WithInvalidAuth_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600)
        );

        // Mock auth verification failure (this should be called for invalid tokens)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer invalid-token"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"Invalid token\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer invalid-token");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps/color", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
        
        mockServer.verify();
    }

    // ==================== MAP STORAGE TESTS ====================

    @Test
    @DisplayName("Create map with valid auth should forward to map service")
    void createMap_WithValidAuth_ShouldForwardToMapService() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "Test Map", "data", "map-data", "userId", "user123")
        );

        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock map service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":\"map123\",\"name\":\"Test Map\"}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Test Map"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Get maps with valid auth should return maps list")
    void getMaps_WithValidAuth_ShouldReturnMapsList() {
        // Mock auth verification first (this is called first by the gateway)
        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock map service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":\"map1\",\"name\":\"Map 1\"},{\"id\":\"map2\",\"name\":\"Map 2\"}]", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Map 1"));
        assertTrue(response.getBody().contains("Map 2"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Get maps with query parameters should forward correctly")
    void getMaps_WithQueryParameters_ShouldForwardCorrectly() {
        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock map service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps?userId=user123&limit=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":\"map1\",\"name\":\"Map 1\"}]", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps?userId=user123&limit=10", 
                HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Get map by ID should forward to map service")
    void getMapById_ShouldForwardToMapService() {
        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock map service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps/map123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":\"map123\",\"name\":\"Test Map\",\"data\":\"map-data\"}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps/map123", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("map123"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Update map should forward to map service")
    void updateMap_ShouldForwardToMapService() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "Updated Map", "data", "updated-data")
        );

        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock map service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps/map123"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":\"map123\",\"name\":\"Updated Map\"}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps/map123", HttpMethod.PUT, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Updated Map"));
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Delete map should forward to map service")
    void deleteMap_ShouldForwardToMapService() {
        // Mock auth verification first (this is called first by the gateway)
        mockServer.expect(requestTo("http://auth-service:8081/auth/verify"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-token"))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        // Mock map service response second (this is called after auth verification)
        mockServer.expect(requestTo("http://map-service:8083/api/v1/maps/map123"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("{\"message\":\"Map deleted successfully\"}", MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps/map123", HttpMethod.DELETE, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("deleted successfully"));
        
        mockServer.verify();
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Service failure should return appropriate error")
    void serviceFailure_ShouldReturnAppropriateError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "testuser", "email", "test@example.com", "password", "password123")
        );

        // Mock auth service to return 500 error
        mockServer.expect(requestTo("http://auth-service:8081/auth/register"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withServerError()
                        .body("{\"error\":\"Internal server error\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register", request, String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Internal server error"));
        
        mockServer.verify();
    }

    // ==================== CORS TESTS ====================

    @Test
    @DisplayName("CORS should be properly configured")
    void cors_ShouldBeProperlyConfigured() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Origin", "http://localhost:3000");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/auth/login", 
                HttpMethod.OPTIONS, 
                request, 
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}