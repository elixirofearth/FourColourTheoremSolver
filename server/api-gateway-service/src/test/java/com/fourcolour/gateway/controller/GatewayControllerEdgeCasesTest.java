package com.fourcolour.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.common.dto.ColoringRequest;
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
class GatewayControllerEdgeCasesTest {

    @Mock
    private ProxyService proxyService;

    @Mock
    private ObjectMapper objectMapper;

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
    void colorMap_WithNullUserId_ShouldHandleGracefully() throws Exception {
        ColoringRequest coloringRequest = new ColoringRequest();
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData();
        imageData.setData(new int[]{1, 2, 3, 4});
        coloringRequest.setImage(imageData);
        coloringRequest.setWidth(800);
        coloringRequest.setHeight(600);
        coloringRequest.setUserId(null); // Null user ID
        
        String authHeader = "Bearer valid-token";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        
        // Should handle null userId by replacing with "unknown"
        when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> request = (java.util.Map<String, Object>) invocation.getArgument(0);
            assertEquals("unknown", request.get("userId"));
            return "{\"userId\":\"unknown\"}";
        });
        
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(ResponseEntity.ok("{\"result\":\"success\"}"));

        ResponseEntity<String> response = gatewayController.colorMap(coloringRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    void colorMap_WithJsonProcessingException_ShouldReturnInternalServerError() throws Exception {
        ColoringRequest coloringRequest = new ColoringRequest();
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData();
        imageData.setData(new int[]{1, 2, 3, 4});
        coloringRequest.setImage(imageData);
        coloringRequest.setWidth(800);
        coloringRequest.setHeight(600);
        
        String authHeader = "Bearer valid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON processing error"));

        ResponseEntity<String> response = gatewayController.colorMap(coloringRequest, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error processing request"));
    }

    @Test
    void getMaps_WithVeryLongQueryString_ShouldHandleCorrectly() {
        String authHeader = "Bearer valid-token";
        StringBuilder longQueryString = new StringBuilder();
        for (int i = 0; i < 100; i++) { // Reduced for performance
            longQueryString.append("param").append(i).append("=value").append(i);
            if (i < 99) longQueryString.append("&");
        }
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getQueryString()).thenReturn(longQueryString.toString());
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), contains(longQueryString.toString()), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("[]"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proxyService).forwardRequest(eq("maps"), contains(longQueryString.toString()), any(), any(), any());
    }

    @Test
    void logout_WithEmptyAuthHeader_ShouldNotInvalidateToken() {
        when(request.getHeader("Authorization")).thenReturn("");
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("Logged out"));

        ResponseEntity<String> response = gatewayController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proxyService, never()).invalidateCachedToken(anyString());
    }

    @Test
    void logout_WithWhitespaceOnlyAuthHeader_ShouldNotInvalidateToken() {
        when(request.getHeader("Authorization")).thenReturn("   ");
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("Logged out"));

        ResponseEntity<String> response = gatewayController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proxyService, never()).invalidateCachedToken(anyString());
    }
}