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
class GatewaySecurityAdvancedTest {

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
    void jwtTokenInjection_ShouldBeHandledSafely() {
        String maliciousToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        when(request.getHeader("Authorization")).thenReturn(maliciousToken);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(maliciousToken))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid token\"}"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
    }

    @Test
    void pathTraversalAttempt_ShouldBeHandledSafely() {
        String maliciousPath = "../../../etc/passwd";
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken("Bearer valid-token"))
                .thenReturn(ResponseEntity.ok("{\"valid\":true}"));

        // This should not cause any security issues
        assertDoesNotThrow(() -> gatewayController.getMap(maliciousPath, request));
    }

    @Test
    void headerInjection_ShouldBeHandledSafely() {
        String maliciousHeader = "Authorization: Bearer valid-token\r\nX-Forwarded-For: 127.0.0.1\r\n\r\nGET /admin HTTP/1.1";
        when(request.getHeader("Authorization")).thenReturn(maliciousHeader);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(maliciousHeader))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid token\"}"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void contentTypeInjection_ShouldBeHandledSafely() {
        String maliciousContentType = "application/json; charset=utf-8\r\n\r\nGET /admin HTTP/1.1";
        when(request.getHeader("Content-Type")).thenReturn(maliciousContentType);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"success\":true}"));

        ResponseEntity<String> response = gatewayController.register("{\"test\":\"data\"}", request);

        // Should handle gracefully without security issues
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void largePayload_ShouldBeHandledSafely() {
        StringBuilder largePayload = new StringBuilder();
        largePayload.append("{\"data\":\"");
        for (int i = 0; i < 10000; i++) {
            largePayload.append("A");
        }
        largePayload.append("\"}");

        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), eq(largePayload.toString())))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Payload too large\"}"));

        ResponseEntity<String> response = gatewayController.register(largePayload.toString(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void nullByteInjection_ShouldBeHandledSafely() {
        String maliciousInput = "{\"email\":\"test@example.com\\0\",\"password\":\"password\"}";
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), eq(maliciousInput)))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Invalid input\"}"));

        ResponseEntity<String> response = gatewayController.login(maliciousInput, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void unicodeInjection_ShouldBeHandledSafely() {
        String unicodeInput = "{\"username\":\"\\u003Cscript\\u003Ealert('xss')\\u003C/script\\u003E\",\"email\":\"test@example.com\",\"password\":\"password\"}";
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), eq(unicodeInput)))
                .thenReturn(ResponseEntity.badRequest().body("{\"error\":\"Invalid input\"}"));

        ResponseEntity<String> response = gatewayController.register(unicodeInput, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void rateLimitingBypassAttempt_ShouldBeBlocked() {
        // Simulate attempts to bypass rate limiting
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("172.16.0.1");
        when(proxyService.isRateLimited("10.0.0.1")).thenReturn(true);

        ResponseEntity<String> response = gatewayController.register("{\"test\":\"data\"}", request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().contains("Rate limit exceeded"));
    }

    @Test
    void tokenReplayAttack_ShouldBeDetected() {
        String reusedToken = "Bearer reused.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(reusedToken);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(reusedToken))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Token already used\"}"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
    }

    @Test
    void sessionFixationAttempt_ShouldBeHandledSafely() {
        String maliciousSessionId = "session_id=malicious_session; Path=/; HttpOnly";
        when(request.getHeader("Cookie")).thenReturn(maliciousSessionId);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"success\":true}"));

        ResponseEntity<String> response = gatewayController.register("{\"test\":\"data\"}", request);

        // Should handle gracefully
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
} 