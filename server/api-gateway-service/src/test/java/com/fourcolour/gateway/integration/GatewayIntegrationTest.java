package com.fourcolour.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.gateway.controller.GatewayController;
import com.fourcolour.gateway.service.ProxyService;
import com.fourcolour.gateway.service.TokenCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(GatewayController.class)
@ActiveProfiles("test")
class GatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProxyService proxyService;

    @MockBean
    private TokenCacheService tokenCacheService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ==================== HEALTH CHECK TESTS ====================

    @Test
    @DisplayName("Health check should return OK")
    void healthCheck_ShouldReturnOK() throws Exception {
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("Services health check should return service status")
    void healthCheckServices_ShouldReturnServiceStatus() throws Exception {
        when(proxyService.checkAllServicesHealth())
                .thenReturn(ResponseEntity.ok("{\"gateway\":\"OK\",\"authentication-service\":\"OK\"}"));

        mockMvc.perform(get("/healthcheck/services"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"gateway\":\"OK\",\"authentication-service\":\"OK\"}"));
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @DisplayName("Register with valid data should return success")
    void register_WithValidData_ShouldReturnSuccess() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "testuser", "email", "test@example.com", "password", "password123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":1,\"username\":\"testuser\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"username\":\"testuser\"}"));
    }

    @Test
    @DisplayName("Register with invalid data should return error")
    void register_WithInvalidData_ShouldReturnError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "", "email", "invalid-email", "password", "")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Invalid data\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"Invalid data\"}"));
    }

    @Test
    @DisplayName("Login with valid credentials should return token")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("email", "test@example.com", "password", "password123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"token\":\"jwt-token-here\",\"user\":{\"id\":1}}"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"jwt-token-here\",\"user\":{\"id\":1}}"));
    }

    @Test
    @DisplayName("Login with invalid credentials should return unauthorized")
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("email", "test@example.com", "password", "wrongpassword")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid credentials\"}"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Invalid credentials\"}"));
    }

    @Test
    @DisplayName("Logout with valid token should return success")
    void logout_WithValidToken_ShouldReturnSuccess() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"message\":\"Logged out successfully\"}"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Logged out successfully\"}"));
    }

    @Test
    @DisplayName("Refresh token should return new token")
    void refreshToken_ShouldReturnNewToken() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"token\":\"new-jwt-token\"}"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer old-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"new-jwt-token\"}"));
    }

    @Test
    @DisplayName("Verify token should return validation result")
    void verifyToken_ShouldReturnValidationResult() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"valid\":true}"));

        mockMvc.perform(post("/api/v1/auth/verify")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"valid\":true}"));
    }

    // ==================== MAP COLORING TESTS ====================

    @Test
    @DisplayName("Color map without auth should return unauthorized")
    void colorMap_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600)
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/v1/maps/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Authentication required\"}"));
    }

    @Test
    @DisplayName("Color map with valid auth should return colored map")
    void colorMap_WithValidAuth_ShouldReturnColoredMap() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600)
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"coloredImage\":{\"data\":[1,2,3,4]},\"colors\":4}"));

        mockMvc.perform(post("/api/v1/maps/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"coloredImage\":{\"data\":[1,2,3,4]},\"colors\":4}"));
    }

    @Test
    @DisplayName("Color map with invalid auth should return unauthorized")
    void colorMap_WithInvalidAuth_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600)
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid token\"}"));

        mockMvc.perform(post("/api/v1/maps/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid-token")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Authentication required\"}"));
    }

    // ==================== MAP STORAGE TESTS ====================

    @Test
    @DisplayName("Create map with valid auth should return success")
    void createMap_WithValidAuth_ShouldReturnSuccess() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "Test Map", "data", "map-data", "userId", "user123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":\"map123\",\"name\":\"Test Map\"}"));

        mockMvc.perform(post("/api/v1/maps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"map123\",\"name\":\"Test Map\"}"));
    }

    @Test
    @DisplayName("Get maps with valid auth should return maps list")
    void getMaps_WithValidAuth_ShouldReturnMapsList() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("[{\"id\":\"map1\",\"name\":\"Map 1\"},{\"id\":\"map2\",\"name\":\"Map 2\"}]"));

        mockMvc.perform(get("/api/v1/maps")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":\"map1\",\"name\":\"Map 1\"},{\"id\":\"map2\",\"name\":\"Map 2\"}]"));
    }

    @Test
    @DisplayName("Get maps with query parameters should forward correctly")
    void getMaps_WithQueryParameters_ShouldForwardCorrectly() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("[{\"id\":\"map1\",\"name\":\"Map 1\"}]"));

        mockMvc.perform(get("/api/v1/maps?userId=user123&limit=10")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get map by ID with valid auth should return map")
    void getMapById_WithValidAuth_ShouldReturnMap() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":\"map123\",\"name\":\"Test Map\",\"data\":\"map-data\"}"));

        mockMvc.perform(get("/api/v1/maps/map123")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"map123\",\"name\":\"Test Map\",\"data\":\"map-data\"}"));
    }

    @Test
    @DisplayName("Update map with valid auth should return success")
    void updateMap_WithValidAuth_ShouldReturnSuccess() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "Updated Map", "data", "updated-data")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":\"map123\",\"name\":\"Updated Map\"}"));

        mockMvc.perform(put("/api/v1/maps/map123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"map123\",\"name\":\"Updated Map\"}"));
    }

    @Test
    @DisplayName("Delete map with valid auth should return success")
    void deleteMap_WithValidAuth_ShouldReturnSuccess() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"message\":\"Map deleted successfully\"}"));

        mockMvc.perform(delete("/api/v1/maps/map123")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Map deleted successfully\"}"));
    }

    // ==================== RATE LIMITING TESTS ====================

    @Test
    @DisplayName("Rate limited request should return too many requests")
    void rateLimitedRequest_ShouldReturnTooManyRequests() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "testuser", "email", "test@example.com", "password", "password123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().json("{\"error\":\"Rate limit exceeded. Please try again later.\"}"));
    }

    @Test
    @DisplayName("Rate limited map request should return too many requests")
    void rateLimitedMapRequest_ShouldReturnTooManyRequests() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("name", "Test Map", "data", "map-data")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/maps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(requestBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().json("{\"error\":\"Rate limit exceeded. Please try again later.\"}"));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Service unavailable should return internal server error")
    void serviceUnavailable_ShouldReturnInternalServerError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "testuser", "email", "test@example.com", "password", "password123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\":\"Internal Server Error\", \"Target Service\": \"http://auth-service\", \"Error Message\": \"Connection timeout\", \"Status Code\": \"500\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    // ==================== CORS TESTS ====================

    @Test
    @DisplayName("OPTIONS request should return OK")
    void optionsRequest_ShouldReturnOK() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPTIONS request for any endpoint should return OK")
    void optionsRequestForAnyEndpoint_ShouldReturnOK() throws Exception {
        mockMvc.perform(options("/api/v1/maps/color"))
                .andExpect(status().isOk());
    }

    // ==================== HEADER FORWARDING TESTS ====================

    @Test
    @DisplayName("Request with custom headers should forward headers correctly")
    void requestWithCustomHeaders_ShouldForwardHeadersCorrectly() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "testuser", "email", "test@example.com", "password", "password123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":1,\"username\":\"testuser\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Custom-Header", "custom-value")
                        .header("User-Agent", "test-agent")
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Empty request body should be handled gracefully")
    void emptyRequestBody_ShouldBeHandledGracefully() throws Exception {
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Empty request body\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Large request body should be handled")
    void largeRequestBody_ShouldBeHandled() throws Exception {
        // Create a large request body
        StringBuilder largeBody = new StringBuilder();
        largeBody.append("{\"data\":\"");
        for (int i = 0; i < 1000; i++) {
            largeBody.append("large-data-chunk-");
        }
        largeBody.append("\"}");

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(anyString())).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"status\":\"processed\"}"));

        mockMvc.perform(post("/api/v1/maps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(largeBody.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Request with special characters should be handled")
    void requestWithSpecialCharacters_ShouldBeHandled() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "test@user#123", "email", "test+user@example.com", "password", "pass@word#123")
        );

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":1,\"username\":\"test@user#123\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
