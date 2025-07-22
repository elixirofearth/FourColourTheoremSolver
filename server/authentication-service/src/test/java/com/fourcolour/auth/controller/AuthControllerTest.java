package com.fourcolour.auth.controller;

import com.fourcolour.auth.service.AuthenticationService;
import com.fourcolour.common.dto.LoginRequest;
import com.fourcolour.common.dto.RegisterRequest;
import com.fourcolour.common.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        tokenResponse = new TokenResponse(
                "jwt-token-here",
                "Test User",
                1,
                "test@example.com",
                "2024-12-31T23:59:59"
        );
    }

    @Test
    void healthCheck_ShouldReturnOK() {
        ResponseEntity<String> response = authController.healthCheck();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void register_WithValidRequest_ShouldReturnCreated() {
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(tokenResponse, response.getBody());
        verify(authenticationService).register(registerRequest);
    }

    @Test
    void register_WithExistingEmail_ShouldReturnConflict() {
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("User with this email already exists"));

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    void register_WithGenericException_ShouldReturnInternalServerError() {
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to create user"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokenResponse() {
        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tokenResponse, response.getBody());
        verify(authenticationService).login(loginRequest);
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() {
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid credentials"));
    }

    @Test
    void logout_WithValidToken_ShouldReturnSuccessMessage() {
        String token = "Bearer jwt-token";
        doNothing().when(authenticationService).logout(token);

        ResponseEntity<?> response = authController.logout(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Logged out successfully"));
        verify(authenticationService).logout(token);
    }

    @Test
    void logout_WithException_ShouldReturnInternalServerError() {
        String token = "Bearer jwt-token";
        doThrow(new RuntimeException("Database error"))
                .when(authenticationService).logout(token);

        ResponseEntity<?> response = authController.logout(token);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error processing logout"));
    }

    @Test
    void verifyToken_WithValidToken_ShouldReturnValidResponse() {
        String token = "Bearer jwt-token";
        when(authenticationService.verifyToken(token)).thenReturn(true);
        when(authenticationService.getUserIdFromToken(token)).thenReturn(1);

        ResponseEntity<?> response = authController.verifyToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("\"valid\":true"));
        assertTrue(response.getBody().toString().contains("\"user_id\":1"));
    }

    @Test
    void verifyToken_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalid-token";
        when(authenticationService.verifyToken(token)).thenReturn(false);

        ResponseEntity<?> response = authController.verifyToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void verifyToken_WithException_ShouldReturnUnauthorized() {
        String token = "Bearer jwt-token";
        when(authenticationService.verifyToken(token))
                .thenThrow(new RuntimeException("Token parsing error"));

        ResponseEntity<?> response = authController.verifyToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() {
        String token = "Bearer jwt-token";
        when(authenticationService.refreshToken(token)).thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.refreshToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tokenResponse, response.getBody());
        verify(authenticationService).refreshToken(token);
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldReturnUnauthorized() {
        String token = "Bearer expired-token";
        when(authenticationService.refreshToken(token))
                .thenThrow(new RuntimeException("Token expired"));

        ResponseEntity<?> response = authController.refreshToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid or expired token"));
    }

    @Test
    void register_WithNullName_ShouldHandleGracefully() {
        RegisterRequest requestWithNullName = new RegisterRequest();
        requestWithNullName.setName(null);
        requestWithNullName.setEmail("test@example.com");
        requestWithNullName.setPassword("password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(requestWithNullName);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(requestWithNullName);
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnUnauthorized() {
        LoginRequest emptyPasswordRequest = new LoginRequest();
        emptyPasswordRequest.setEmail("test@example.com");
        emptyPasswordRequest.setPassword("");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.login(emptyPasswordRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid credentials"));
    }

    @Test
    void verifyToken_WithMalformedToken_ShouldReturnUnauthorized() {
        String malformedToken = "malformed-token";
        when(authenticationService.verifyToken(malformedToken))
                .thenThrow(new RuntimeException("Malformed JWT"));

        ResponseEntity<?> response = authController.verifyToken(malformedToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void logout_WithEmptyToken_ShouldStillCallService() {
        String emptyToken = "";
        doNothing().when(authenticationService).logout(emptyToken);

        ResponseEntity<?> response = authController.logout(emptyToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).logout(emptyToken);
    }

    @Test
    void refreshToken_WithNullTokenService_ShouldReturnUnauthorized() {
        String token = "Bearer null-service-token";
        when(authenticationService.refreshToken(token))
                .thenThrow(new RuntimeException("Service unavailable"));

        ResponseEntity<?> response = authController.refreshToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid or expired token"));
    }
}