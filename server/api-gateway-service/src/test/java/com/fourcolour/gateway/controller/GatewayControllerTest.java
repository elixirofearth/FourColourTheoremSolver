package com.fourcolour.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.common.dto.ColoringRequest;
import com.fourcolour.gateway.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GatewayControllerTest {

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
        // Set up default mocks for request headers
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        // Default IP address headers
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
    }

    @Test
    void healthCheck_ShouldReturnOK() {
        ResponseEntity<String> response = gatewayController.healthCheck();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void healthCheckServices_ShouldCallProxyService() {
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("All services OK");
        when(proxyService.checkAllServicesHealth()).thenReturn(expectedResponse);

        ResponseEntity<String> response = gatewayController.healthCheckServices();

        assertEquals(expectedResponse, response);
        verify(proxyService).checkAllServicesHealth();
    }

    @Test
    void register_WithValidRequest_ShouldForwardToAuthService() {
        String requestBody = "{\"name\":\"test\",\"email\":\"test@example.com\",\"password\":\"password123\"}";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("{\"id\":1,\"name\":\"test\"}");
        
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(eq("auth"), eq("/auth/register"), eq(HttpMethod.POST), 
                                        any(HttpHeaders.class), eq(requestBody)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = gatewayController.register(requestBody, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":1,\"name\":\"test\"}", response.getBody());
        verify(proxyService).forwardRequest(eq("auth"), eq("/auth/register"), eq(HttpMethod.POST), 
                                           any(HttpHeaders.class), eq(requestBody));
    }

    @Test
    void register_WhenRateLimited_ShouldReturnTooManyRequests() {
        String requestBody = "{\"name\":\"test\",\"email\":\"test@example.com\",\"password\":\"password123\"}";
        
        when(proxyService.isRateLimited(anyString())).thenReturn(true);

        ResponseEntity<String> response = gatewayController.register(requestBody, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().contains("Rate limit exceeded"));
        verify(proxyService, never()).forwardRequest(anyString(), anyString(), any(HttpMethod.class), 
                                                    any(HttpHeaders.class), anyString());
    }

    @Test
    void login_WithValidCredentials_ShouldForwardToAuthService() {
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("{\"token\":\"jwt-token\"}");
        
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(eq("auth"), eq("/auth/login"), eq(HttpMethod.POST), 
                                        any(HttpHeaders.class), eq(requestBody)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = gatewayController.login(requestBody, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"token\":\"jwt-token\"}", response.getBody());
    }

    @Test
    void logout_WithValidToken_ShouldInvalidateTokenAndForward() {
        String authHeader = "Bearer jwt-token";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(eq("auth"), eq("/auth/logout"), eq(HttpMethod.POST), 
                                        any(HttpHeaders.class), isNull()))
                .thenReturn(ResponseEntity.ok("Logged out"));

        ResponseEntity<String> response = gatewayController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proxyService).invalidateCachedToken(authHeader);
        verify(proxyService).forwardRequest(eq("auth"), eq("/auth/logout"), eq(HttpMethod.POST), 
                                           any(HttpHeaders.class), isNull());
    }

    @Test
    void colorMap_WithValidAuthenticationAndRequest_ShouldForwardToSolver() throws Exception {
        ColoringRequest coloringRequest = createValidColoringRequest();
        String authHeader = "Bearer valid-token";
        String expectedSolverBody = "{\"image\":\"base64data\",\"width\":800,\"height\":600,\"userId\":\"user123\"}";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedSolverBody);
        when(proxyService.forwardRequest(eq("solver"), eq("/api/solve"), eq(HttpMethod.POST), 
                                        any(HttpHeaders.class), eq(expectedSolverBody)))
                .thenReturn(ResponseEntity.ok("{\"coloredImage\":\"result\"}"));

        ResponseEntity<String> response = gatewayController.colorMap(coloringRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"coloredImage\":\"result\"}", response.getBody());
        verify(proxyService).verifyToken(authHeader);
    }

    @Test
    void colorMap_WithoutAuthentication_ShouldReturnUnauthorized() {
        ColoringRequest coloringRequest = createValidColoringRequest();
        
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);

        ResponseEntity<String> response = gatewayController.colorMap(coloringRequest, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
        verify(proxyService, never()).forwardRequest(anyString(), anyString(), any(HttpMethod.class), 
                                                    any(HttpHeaders.class), anyString());
    }

    @Test
    void colorMap_WithInvalidToken_ShouldReturnUnauthorized() {
        ColoringRequest coloringRequest = createValidColoringRequest();
        String authHeader = "Bearer invalid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid token\"}"));

        ResponseEntity<String> response = gatewayController.colorMap(coloringRequest, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication required"));
    }

    @Test
    void createMap_WithValidAuthentication_ShouldForwardToMapService() {
        String requestBody = "{\"name\":\"Test Map\",\"data\":\"map-data\"}";
        String authHeader = "Bearer valid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), eq("/api/v1/maps"), eq(HttpMethod.POST), 
                                        any(HttpHeaders.class), eq(requestBody)))
                .thenReturn(ResponseEntity.ok("{\"id\":\"map-123\",\"name\":\"Test Map\"}"));

        ResponseEntity<String> response = gatewayController.createMap(requestBody, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":\"map-123\",\"name\":\"Test Map\"}", response.getBody());
    }

    @Test
    void getMaps_WithValidAuthentication_ShouldForwardToMapService() {
        String authHeader = "Bearer valid-token";
        String queryString = "userId=123";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getQueryString()).thenReturn(queryString);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), eq("/api/v1/maps?userId=123"), eq(HttpMethod.GET), 
                                        any(HttpHeaders.class), isNull()))
                .thenReturn(ResponseEntity.ok("[{\"id\":\"map-123\",\"name\":\"Test Map\"}]"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[{\"id\":\"map-123\",\"name\":\"Test Map\"}]", response.getBody());
    }

    @Test
    void getMaps_WithoutQueryString_ShouldForwardCorrectPath() {
        String authHeader = "Bearer valid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getQueryString()).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), eq("/api/v1/maps"), eq(HttpMethod.GET), 
                                        any(HttpHeaders.class), isNull()))
                .thenReturn(ResponseEntity.ok("[]"));

        ResponseEntity<String> response = gatewayController.getMaps(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proxyService).forwardRequest(eq("maps"), eq("/api/v1/maps"), eq(HttpMethod.GET), 
                                           any(HttpHeaders.class), isNull());
    }

    @Test
    void getMap_WithValidId_ShouldForwardToMapService() {
        String mapId = "map-123";
        String authHeader = "Bearer valid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), eq("/api/v1/maps/map-123"), eq(HttpMethod.GET), 
                                        any(HttpHeaders.class), isNull()))
                .thenReturn(ResponseEntity.ok("{\"id\":\"map-123\",\"name\":\"Test Map\"}"));

        ResponseEntity<String> response = gatewayController.getMap(mapId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":\"map-123\",\"name\":\"Test Map\"}", response.getBody());
    }

    @Test
    void updateMap_WithValidData_ShouldForwardToMapService() {
        String mapId = "map-123";
        String requestBody = "{\"name\":\"Updated Map\"}";
        String authHeader = "Bearer valid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), eq("/api/v1/maps/map-123"), eq(HttpMethod.PUT), 
                                        any(HttpHeaders.class), eq(requestBody)))
                .thenReturn(ResponseEntity.ok("{\"id\":\"map-123\",\"name\":\"Updated Map\"}"));

        ResponseEntity<String> response = gatewayController.updateMap(mapId, requestBody, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":\"map-123\",\"name\":\"Updated Map\"}", response.getBody());
    }

    @Test
    void deleteMap_WithValidId_ShouldForwardToMapService() {
        String mapId = "map-123";
        String authHeader = "Bearer valid-token";
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(proxyService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.verifyToken(authHeader)).thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(proxyService.forwardRequest(eq("maps"), eq("/api/v1/maps/map-123"), eq(HttpMethod.DELETE), 
                                        any(HttpHeaders.class), isNull()))
                .thenReturn(ResponseEntity.ok("{\"message\":\"Map deleted successfully\"}"));

        ResponseEntity<String> response = gatewayController.deleteMap(mapId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"Map deleted successfully\"}", response.getBody());
    }

    @Test
    void handleOptions_ShouldReturnOK() {
        ResponseEntity<Void> response = gatewayController.handleOptions();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void extractClientIpAddress_WithXForwardedFor_ShouldReturnFirstIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        // Use reflection to test private method
        String ipAddress = ReflectionTestUtils.invokeMethod(gatewayController, "getClientIpAddress", request);
        
        assertEquals("192.168.1.1", ipAddress);
    }

    @Test
    void extractClientIpAddress_WithXRealIp_ShouldReturnRealIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        String ipAddress = ReflectionTestUtils.invokeMethod(gatewayController, "getClientIpAddress", request);
        
        assertEquals("192.168.1.1", ipAddress);
    }

    @Test
    void extractClientIpAddress_WithoutProxyHeaders_ShouldReturnRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        String ipAddress = ReflectionTestUtils.invokeMethod(gatewayController, "getClientIpAddress", request);
        
        assertEquals("127.0.0.1", ipAddress);
    }

    // Edge case tests merged from GatewayControllerEdgeCasesTest
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

    private ColoringRequest createValidColoringRequest() {
        ColoringRequest request = new ColoringRequest();
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData();
        imageData.setData(new int[]{1, 2, 3, 4}); // Sample pixel data
        request.setImage(imageData);
        request.setWidth(800);
        request.setHeight(600);
        request.setUserId("user123");
        return request;
    }
}
