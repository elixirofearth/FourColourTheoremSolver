package com.fourcolour.gateway.security;

import com.fourcolour.gateway.controller.GatewayController;
import com.fourcolour.gateway.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GatewaySecurityTest {

    @Mock
    private ProxyService proxyService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GatewayController gatewayController;

    @BeforeEach
    void setUp() {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
    }

    @Test
    void protectedEndpoint_WithMalformedToken_ShouldReturnUnauthorized() {
        String malformedToken = "malformed-token-without-bearer";
        when(request.getHeader("Authorization")).thenReturn(malformedToken);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(malformedToken))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid token\"}"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
    }

    @Test
    void protectedEndpoint_WithExpiredToken_ShouldReturnUnauthorized() {
        String expiredToken = "Bearer expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(expiredToken);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(expiredToken))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Token expired\"}"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
    }

    @Test
    void rateLimiting_WithSuspiciousActivity_ShouldBlockRequests() {
        String requestBody = "{\"test\":\"data\"}";
        when(proxyService.isRateLimited(anyString())).thenReturn(true);

        ResponseEntity<String> response = gatewayController.register(requestBody, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().contains("Rate limit exceeded"));
        verify(proxyService, never()).forwardRequest(anyString(), anyString(), any(), any(), any());
    }

    @Test
    void sqlInjectionAttempt_ShouldBeHandledSafely() {
        String maliciousInput = "{\"email\":\"test@example.com'; DROP TABLE users; --\",\"password\":\"password\"}";
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), eq(maliciousInput)))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Invalid input\"}"));

        ResponseEntity<String> response = gatewayController.login(maliciousInput, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(proxyService).forwardRequest(eq("auth"), eq("/auth/login"), any(), any(), eq(maliciousInput));
    }

    @Test
    void xssAttempt_ShouldBeHandledSafely() {
        String xssPayload = "{\"username\":\"<script>alert('xss')</script>\",\"email\":\"test@example.com\",\"password\":\"password\"}";
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), eq(xssPayload)))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Invalid characters in input\"}"));

        ResponseEntity<String> response = gatewayController.register(xssPayload, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(proxyService).forwardRequest(eq("auth"), eq("/auth/register"), any(), any(), eq(xssPayload));
    }
}
