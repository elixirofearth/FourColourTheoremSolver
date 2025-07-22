package com.fourcolour.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.auth.entity.User;
import com.fourcolour.auth.repository.SessionRepository;
import com.fourcolour.auth.repository.UserRepository;
import com.fourcolour.common.service.LoggerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test for Authentication Service
 * Tests the full authentication workflow with real database (H2 in-memory)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private LoggerClient loggerClient;

    private ObjectMapper objectMapper;
    private String baseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.jwt.secret", () -> "testSecretKeyForIntegrationTests123456789");
        registry.add("app.jwt.expiration", () -> "3600");
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:" + port;
        
        // Clear database
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        
        // Reset mocks
        reset(loggerClient);
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    // ==================== HEALTH CHECK TESTS ====================

    @Test
    @DisplayName("Application should start and health check should work")
    void applicationShouldStartAndHealthCheckShouldWork() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/auth/healthcheck", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("User registration should work with valid data")
    void userRegistration_ShouldWorkWithValidData() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("token"));
        assertTrue(responseBody.contains("Test User"));
        assertTrue(responseBody.contains("test@example.com"));

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail("test@example.com"));
        
        // Verify logging was called
        verify(loggerClient).logEvent(eq("authentication-service"), eq("user_registered"), 
                anyString(), anyString(), eq(1), any());
    }

    @Test
    @DisplayName("Registration should fail with duplicate email")
    void registration_ShouldFailWithDuplicateEmail() throws Exception {
        // Create user first
        User existingUser = new User("test@example.com", passwordEncoder.encode("password"), "Existing User");
        userRepository.save(existingUser);

        Map<String, Object> requestBody = Map.of(
                "name", "New User",
                "email", "test@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", request, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("already exists"));
    }

    @Test
    @DisplayName("Registration should fail with invalid email format")
    void registration_ShouldFailWithInvalidEmail() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", "Test User",
                "email", "invalid-email-format",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("User login should work with valid credentials")
    void userLogin_ShouldWorkWithValidCredentials() throws Exception {
        // Create user first
        User user = new User("test@example.com", passwordEncoder.encode("password123"), "Test User");
        userRepository.save(user);

        Map<String, Object> requestBody = Map.of(
                "email", "test@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("token"));
        assertTrue(responseBody.contains("Test User"));
        assertTrue(responseBody.contains("test@example.com"));

        // Verify session was created
        assertTrue(sessionRepository.findAll().size() > 0);
        
        // Verify logging was called
        verify(loggerClient).logEvent(eq("authentication-service"), eq("user_login"), 
                anyString(), anyString(), eq(1), any());
    }

    @Test
    @DisplayName("Login should fail with incorrect password")
    void login_ShouldFailWithIncorrectPassword() throws Exception {
        // Create user first
        User user = new User("test@example.com", passwordEncoder.encode("correctpassword"), "Test User");
        userRepository.save(user);

        Map<String, Object> requestBody = Map.of(
                "email", "test@example.com",
                "password", "wrongpassword"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid credentials"));
        
        // Verify failed login was logged
        verify(loggerClient).logEvent(eq("authentication-service"), eq("login_failed"), 
                anyString(), anyString(), eq(2), any());
    }

    @Test
    @DisplayName("Login should fail with non-existent user")
    void login_ShouldFailWithNonExistentUser() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "email", "nonexistent@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid credentials"));
        
        // Verify failed login was logged
        verify(loggerClient).logEvent(eq("authentication-service"), eq("login_failed"), 
                eq("unknown"), anyString(), eq(2), any());
    }

    // ==================== TOKEN VERIFICATION TESTS ====================

    @Test
    @DisplayName("Token verification should work with valid token")
    void tokenVerification_ShouldWorkWithValidToken() throws Exception {
        // Register user to get valid token
        String token = registerUserAndGetToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/verify", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"valid\":true"));
        assertTrue(response.getBody().contains("\"user_id\""));
    }

    @Test
    @DisplayName("Token verification should fail with invalid token")
    void tokenVerification_ShouldFailWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid.jwt.token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/verify", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid token"));
    }

    @Test
    @DisplayName("Token verification should fail without Authorization header")
    void tokenVerification_ShouldFailWithoutAuthHeader() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/verify", null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("Logout should work with valid token")
    void logout_ShouldWorkWithValidToken() throws Exception {
        // Register user and get token
        String token = registerUserAndGetToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/logout", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Logged out successfully"));

        // Verify session was deleted
        assertTrue(sessionRepository.findByToken(token).isEmpty());
        
        // Verify logout was logged
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("user_logout"), 
                anyString(), anyString(), eq(1), any());
    }

    @Test
    @DisplayName("Logout should handle invalid token gracefully")
    void logout_ShouldHandleInvalidTokenGracefully() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid.token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/logout", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Logged out successfully"));
    }

    // ==================== TOKEN REFRESH TESTS ====================

    @Test
    @DisplayName("Token refresh should work with valid token")
    void tokenRefresh_ShouldWorkWithValidToken() throws Exception {
        // Register user and get token
        String originalToken = registerUserAndGetToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + originalToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/refresh", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("token"));
        assertTrue(responseBody.contains("Test User"));

        // Verify new token is different from original
        String newToken = extractTokenFromResponse(responseBody);
        assertNotEquals(originalToken, newToken);

        // Verify original session was deleted
        assertTrue(sessionRepository.findByToken(originalToken).isEmpty());
        
        // Verify new session was created
        assertTrue(sessionRepository.findByToken(newToken).isPresent());
        
        // Verify token refresh was logged
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("token_refreshed"), 
                anyString(), anyString(), eq(1), any());
    }

    @Test
    @DisplayName("Token refresh should fail with invalid token")
    void tokenRefresh_ShouldFailWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid.token");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/refresh", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid or expired token"));
    }

    // ==================== END-TO-END WORKFLOW TESTS ====================

    @Test
    @DisplayName("Complete authentication workflow should work")
    void completeAuthenticationWorkflow_ShouldWork() throws Exception {
        // 1. Register user
        Map<String, Object> registerBody = Map.of(
                "name", "Workflow User",
                "email", "workflow@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> registerRequest = new HttpEntity<>(
                objectMapper.writeValueAsString(registerBody), headers);

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register", registerRequest, String.class);

        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        String initialToken = extractTokenFromResponse(registerResponse.getBody());

        // 2. Verify token
        HttpHeaders verifyHeaders = new HttpHeaders();
        verifyHeaders.set("Authorization", "Bearer " + initialToken);
        HttpEntity<String> verifyRequest = new HttpEntity<>(verifyHeaders);

        ResponseEntity<String> verifyResponse = restTemplate.postForEntity(
                baseUrl + "/auth/verify", verifyRequest, String.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertTrue(verifyResponse.getBody().contains("\"valid\":true"));

        // 3. Refresh token
        ResponseEntity<String> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/auth/refresh", verifyRequest, String.class);

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        String newToken = extractTokenFromResponse(refreshResponse.getBody());

        // 4. Logout with new token
        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.set("Authorization", "Bearer " + newToken);
        HttpEntity<String> logoutRequest = new HttpEntity<>(logoutHeaders);

        ResponseEntity<String> logoutResponse = restTemplate.postForEntity(
                baseUrl + "/auth/logout", logoutRequest, String.class);

        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        assertTrue(logoutResponse.getBody().contains("Logged out successfully"));

        // 5. Verify token is now invalid
        ResponseEntity<String> finalVerifyResponse = restTemplate.postForEntity(
                baseUrl + "/auth/verify", logoutRequest, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, finalVerifyResponse.getStatusCode());

        // Verify all expected log events were called
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("user_registered"), 
                anyString(), anyString(), eq(1), any());
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("session_created"), 
                anyString(), anyString(), eq(1), any());
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("token_refreshed"), 
                anyString(), anyString(), eq(1), any());
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("user_logout"), 
                anyString(), anyString(), eq(1), any());
    }

    @Test
    @DisplayName("Login after registration should work")
    void loginAfterRegistration_ShouldWork() throws Exception {
        // 1. Register user
        Map<String, Object> registerBody = Map.of(
                "name", "Login Test User",
                "email", "logintest@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> registerRequest = new HttpEntity<>(
                objectMapper.writeValueAsString(registerBody), headers);

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register", registerRequest, String.class);

        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // 2. Login with same credentials
        Map<String, Object> loginBody = Map.of(
                "email", "logintest@example.com",
                "password", "password123"
        );

        HttpEntity<String> loginRequest = new HttpEntity<>(
                objectMapper.writeValueAsString(loginBody), headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login", loginRequest, String.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertTrue(loginResponse.getBody().contains("token"));
        assertTrue(loginResponse.getBody().contains("Login Test User"));

        // Verify both registration and login were logged
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("user_registered"), 
                anyString(), anyString(), eq(1), any());
        verify(loggerClient, atLeastOnce()).logEvent(eq("authentication-service"), eq("user_login"), 
                anyString(), anyString(), eq(1), any());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Malformed JSON should return bad request")
    void malformedJson_ShouldReturnBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{invalid json}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Missing required fields should return bad request")
    void missingRequiredFields_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> incompleteBody = Map.of(
                "email", "incomplete@example.com"
                // Missing password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(incompleteBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== HELPER METHODS ====================

    private String registerUserAndGetToken() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "password", "password123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return extractTokenFromResponse(response.getBody());
    }

    private String extractTokenFromResponse(String responseBody) {
        try {
            Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
            return (String) responseMap.get("token");
        } catch (Exception e) {
            fail("Failed to extract token from response: " + responseBody);
            return null;
        }
    }
}