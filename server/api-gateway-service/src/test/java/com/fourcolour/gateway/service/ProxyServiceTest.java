package com.fourcolour.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxyServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TokenCacheService tokenCacheService;

    @InjectMocks
    private ProxyService proxyService;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // Set service URLs using reflection
        ReflectionTestUtils.setField(proxyService, "authServiceUrl", "http://auth-service:8081");
        ReflectionTestUtils.setField(proxyService, "mapStorageServiceUrl", "http://map-service:8083");
        ReflectionTestUtils.setField(proxyService, "coloringServiceUrl", "http://solver-service:8082");
        
        // Inject the TokenCacheService mock
        ReflectionTestUtils.setField(proxyService, "tokenCacheService", tokenCacheService);
        
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void forwardRequest_WithValidRequest_ShouldReturnResponse() {
        String requestBody = "{\"test\":\"data\"}";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("{\"result\":\"success\"}", 
                                                                      responseHeaders, HttpStatus.OK);
        
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/login"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = proxyService.forwardRequest("auth", "/auth/login", 
                                                                     HttpMethod.POST, headers, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"result\":\"success\"}", response.getBody());
        verify(restTemplate).exchange(eq("http://auth-service:8081/auth/login"), eq(HttpMethod.POST), 
                                     any(HttpEntity.class), eq(String.class));
    }

    @Test
    void forwardRequest_WithHttpClientError_ShouldReturnErrorResponse() {
        String requestBody = "{\"test\":\"data\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, 
                "Bad Request", 
                HttpHeaders.EMPTY,
                "{\"error\":\"Invalid data\"}".getBytes(), 
                null);
        
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/login"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        ResponseEntity<String> response = proxyService.forwardRequest("auth", "/auth/login", 
                                                                     HttpMethod.POST, headers, requestBody);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"error\":\"Invalid data\"}", response.getBody());
    }

    @Test
    void forwardRequest_WithHttpClientErrorAndEmptyBody_ShouldReturnDefaultError() {
        String requestBody = "{\"test\":\"data\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, 
                "Unauthorized", 
                HttpHeaders.EMPTY,
                "".getBytes(), 
                null);
        
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/login"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        ResponseEntity<String> response = proxyService.forwardRequest("auth", "/auth/login", 
                                                                     HttpMethod.POST, headers, requestBody);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{\"error\":\"Invalid credentials\"}", response.getBody());
    }

    @Test
    void forwardRequest_WithConflictError_ShouldReturnUserExistsError() {
        String requestBody = "{\"test\":\"data\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.CONFLICT, 
                "Conflict", 
                HttpHeaders.EMPTY,
                "".getBytes(), 
                null);
        
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/register"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        ResponseEntity<String> response = proxyService.forwardRequest("auth", "/auth/register", 
                                                                     HttpMethod.POST, headers, requestBody);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("{\"error\":\"User with this email already exists\"}", response.getBody());
    }

    @Test
    void forwardRequest_WithGenericException_ShouldReturnInternalServerError() {
        String requestBody = "{\"test\":\"data\"}";
        RuntimeException exception = new RuntimeException("Connection timeout");
        
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/login"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        ResponseEntity<String> response = proxyService.forwardRequest("auth", "/auth/login", 
                                                                     HttpMethod.POST, headers, requestBody);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Internal Server Error"));
        assertTrue(response.getBody().contains("Connection timeout"));
    }

    @Test
    void forwardRequest_ShouldFilterProblematicHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json");
        responseHeaders.add("Transfer-Encoding", "chunked");
        responseHeaders.add("Content-Length", "123");
        responseHeaders.add("Connection", "keep-alive");
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Custom-Header", "value");
        
        ResponseEntity<String> originalResponse = new ResponseEntity<>("{\"result\":\"success\"}", 
                                                                      responseHeaders, HttpStatus.OK);
        
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/login"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenReturn(originalResponse);

        ResponseEntity<String> response = proxyService.forwardRequest("auth", "/auth/login", 
                                                                     HttpMethod.POST, headers, "{}");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertTrue(response.getHeaders().containsKey("Content-Type"));
        assertTrue(response.getHeaders().containsKey("Custom-Header"));
        assertFalse(response.getHeaders().containsKey("Transfer-Encoding"));
        assertFalse(response.getHeaders().containsKey("Content-Length"));
        assertFalse(response.getHeaders().containsKey("Connection"));
        assertFalse(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
    }

    @Test
    void verifyToken_WithCachedValidToken_ShouldReturnCachedResult() {
        String token = "Bearer valid-token";
        
        when(tokenCacheService.getCachedTokenValidation(token)).thenReturn(true);

        ResponseEntity<String> response = proxyService.verifyToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"valid\":true}", response.getBody());
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void verifyToken_WithCachedInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalid-token";
        
        when(tokenCacheService.getCachedTokenValidation(token)).thenReturn(false);

        ResponseEntity<String> response = proxyService.verifyToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{\"error\":\"Invalid token\"}", response.getBody());
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void verifyToken_WithUncachedValidToken_ShouldVerifyAndCache() {
        String token = "Bearer valid-token";
        ResponseEntity<String> authResponse = ResponseEntity.ok("{\"valid\":true}");
        
        when(tokenCacheService.getCachedTokenValidation(token)).thenReturn(null);
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/verify"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenReturn(authResponse);

        ResponseEntity<String> response = proxyService.verifyToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"valid\":true}", response.getBody());
        verify(tokenCacheService).cacheToken(token, true);
    }

    @Test
    void verifyToken_WithUncachedInvalidToken_ShouldVerifyAndCacheNegative() {
        String token = "Bearer invalid-token";
        
        when(tokenCacheService.getCachedTokenValidation(token)).thenReturn(null);
        when(restTemplate.exchange(eq("http://auth-service:8081/auth/verify"), eq(HttpMethod.POST), 
                                  any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", 
                                                          HttpHeaders.EMPTY, "".getBytes(), null));

        ResponseEntity<String> response = proxyService.verifyToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{\"error\":\"Invalid token\"}", response.getBody());
        verify(tokenCacheService).cacheToken(token, false);
    }

    @Test
    void invalidateCachedToken_ShouldCallTokenCacheService() {
        String token = "Bearer token";
        
        proxyService.invalidateCachedToken(token);
        
        verify(tokenCacheService).invalidateToken(token);
    }

    @Test
    void isRateLimited_ShouldCallTokenCacheService() {
        String ipAddress = "192.168.1.1";
        
        when(tokenCacheService.isRateLimited(ipAddress)).thenReturn(true);
        
        boolean result = proxyService.isRateLimited(ipAddress);
        
        assertTrue(result);
        verify(tokenCacheService).isRateLimited(ipAddress);
    }

    @Test
    void getServiceUrl_WithAuthService_ShouldReturnAuthUrl() {
        String url = ReflectionTestUtils.invokeMethod(proxyService, "getServiceUrl", "auth");
        assertEquals("http://auth-service:8081", url);
    }

    @Test
    void getServiceUrl_WithMapService_ShouldReturnMapUrl() {
        String url = ReflectionTestUtils.invokeMethod(proxyService, "getServiceUrl", "maps");
        assertEquals("http://map-service:8083", url);
    }

    @Test
    void getServiceUrl_WithSolverService_ShouldReturnSolverUrl() {
        String url = ReflectionTestUtils.invokeMethod(proxyService, "getServiceUrl", "solver");
        assertEquals("http://solver-service:8082", url);
    }

    @Test
    void getServiceUrl_WithUnknownService_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyService, "getServiceUrl", "unknown");
        });
    }

    @Test
    void checkAllServicesHealth_WithAllServicesUp_ShouldReturnOK() {
        when(restTemplate.getForEntity("http://auth-service:8081/auth/healthcheck", String.class))
                .thenReturn(ResponseEntity.ok("OK"));
        when(restTemplate.getForEntity("http://map-service:8083/api/v1/maps/healthcheck", String.class))
                .thenReturn(ResponseEntity.ok("OK"));
        when(restTemplate.getForEntity("http://solver-service:8082/health", String.class))
                .thenReturn(ResponseEntity.ok("OK"));

        ResponseEntity<String> response = proxyService.checkAllServicesHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"gateway\": \"OK\""));
        assertTrue(response.getBody().contains("\"authentication-service\": \"OK\""));
        assertTrue(response.getBody().contains("\"map-storage-service\": \"OK\""));
        assertTrue(response.getBody().contains("\"solver-service\": \"OK\""));
    }

    @Test
    void checkAllServicesHealth_WithSomeServicesDown_ShouldReturnServiceUnavailable() {
        when(restTemplate.getForEntity("http://auth-service:8081/auth/healthcheck", String.class))
                .thenReturn(ResponseEntity.ok("OK"));
        when(restTemplate.getForEntity("http://map-service:8083/api/v1/maps/healthcheck", String.class))
                .thenThrow(new RuntimeException("Connection refused"));
        when(restTemplate.getForEntity("http://solver-service:8082/health", String.class))
                .thenReturn(ResponseEntity.ok("OK"));

        ResponseEntity<String> response = proxyService.checkAllServicesHealth();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().contains("\"authentication-service\": \"OK\""));
        assertTrue(response.getBody().contains("\"map-storage-service\": \"FAIL"));
        assertTrue(response.getBody().contains("\"solver-service\": \"OK\""));
    }

    @Test
    void checkServiceHealth_WithSuccessfulResponse_ShouldReturnOK() {
        when(restTemplate.getForEntity("http://test-service/health", String.class))
                .thenReturn(ResponseEntity.ok("Service is healthy"));

        String result = ReflectionTestUtils.invokeMethod(proxyService, "checkServiceHealth", "http://test-service/health");

        assertEquals("OK", result);
    }

    @Test
    void checkServiceHealth_WithNon2xxResponse_ShouldReturnFailWithStatus() {
        when(restTemplate.getForEntity("http://test-service/health", String.class))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error"));

        String result = ReflectionTestUtils.invokeMethod(proxyService, "checkServiceHealth", "http://test-service/health");

        assertEquals("FAIL - Status: 500 INTERNAL_SERVER_ERROR", result);
    }

    @Test
    void checkServiceHealth_WithException_ShouldReturnFailWithMessage() {
        when(restTemplate.getForEntity("http://test-service/health", String.class))
                .thenThrow(new RuntimeException("Connection timeout"));

        String result = ReflectionTestUtils.invokeMethod(proxyService, "checkServiceHealth", "http://test-service/health");

        assertEquals("FAIL - Connection timeout", result);
    }
}
