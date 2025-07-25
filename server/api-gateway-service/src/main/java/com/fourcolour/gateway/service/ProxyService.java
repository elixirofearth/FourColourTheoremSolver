package com.fourcolour.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

    private final RestTemplate restTemplate;

    @Autowired
    private TokenCacheService tokenCacheService;

    @Value("${services.coloring.url:http://solver-service}")
    private String coloringServiceUrl;

    @Value("${services.authentication.url:http://authentication-service}")
    private String authServiceUrl;

    @Value("${services.map-storage.url:http://map-storage-service}")
    private String mapStorageServiceUrl;

    public ProxyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> forwardRequest(String serviceName, String path, HttpMethod method, 
                                               HttpHeaders headers, Object body) {
        String targetUrl = getServiceUrl(serviceName) + path;
        
        logger.info("Forwarding {} request to: {}", method, targetUrl);

        try {
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl, method, entity, String.class);
            
            logger.info("Received response from {}: {}", targetUrl, response.getStatusCode());
            
            // Create clean headers by filtering out problematic headers
            HttpHeaders cleanHeaders = new HttpHeaders();
            if (response.getHeaders() != null) {
                response.getHeaders().forEach((name, values) -> {
                    // Filter out headers that shouldn't be forwarded
                    if (!shouldFilterHeader(name)) {
                        cleanHeaders.put(name, values);
                    }
                });
            }
            
            // Return a new ResponseEntity with clean headers
            return ResponseEntity.status(response.getStatusCode())
                    .headers(cleanHeaders)
                    .body(response.getBody());
            
        } catch (Exception e) {
            logger.error("Error forwarding request to {}: {}", targetUrl, e.getMessage());
            
            // Handle specific HTTP status codes
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpError = 
                    (org.springframework.web.client.HttpClientErrorException) e;
                
                String responseBody = httpError.getResponseBodyAsString();
                if (responseBody == null || responseBody.isEmpty()) {
                    if (httpError.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        responseBody = "{\"error\":\"Invalid credentials\"}";
                    } else if (httpError.getStatusCode() == HttpStatus.CONFLICT) {
                        responseBody = "{\"error\":\"User with this email already exists\"}";
                    } else {
                        responseBody = "{\"error\":\"Request failed\"}";
                    }
                }
                
                return ResponseEntity.status(httpError.getStatusCode())
                        .body(responseBody);
            }
            
            // Handle other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal Server Error\", \"Target Service\": \""+ targetUrl + "\", \"Error Message\": \""+ e.getMessage() + "\", \"Status Code\": \""+ HttpStatus.INTERNAL_SERVER_ERROR + "\"}");
        }
    }

    private boolean shouldFilterHeader(String headerName) {
        if (headerName == null) {
            return true;
        }
        
        String lowerName = headerName.toLowerCase();
        // Filter out headers that can cause issues when forwarding responses
        return lowerName.equals("transfer-encoding") ||
               lowerName.equals("content-encoding") ||
               lowerName.equals("content-length") ||
               lowerName.equals("connection") ||
               lowerName.equals("keep-alive") ||
               lowerName.equals("proxy-authenticate") ||
               lowerName.equals("proxy-authorization") ||
               lowerName.equals("te") ||
               lowerName.equals("trailers") ||
               lowerName.equals("upgrade") ||
               // Filter out CORS headers since the gateway handles CORS
               lowerName.equals("access-control-allow-origin") ||
               lowerName.equals("access-control-allow-methods") ||
               lowerName.equals("access-control-allow-headers") ||
               lowerName.equals("access-control-allow-credentials") ||
               lowerName.equals("access-control-expose-headers") ||
               lowerName.equals("access-control-max-age");
    }

    public ResponseEntity<String> verifyToken(String token) {
        // First, check if we have a cached result
        Boolean cachedResult = tokenCacheService.getCachedTokenValidation(token);
        if (cachedResult != null) {
            logger.debug("Using cached token validation result");
            if (cachedResult) {
                return ResponseEntity.ok("{\"valid\":true}");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"Invalid token\"}");
            }
        }

        // If not cached, verify with authentication service
        String url = authServiceUrl + "/auth/verify";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            // Cache the result
            boolean isValid = response.getStatusCode() == HttpStatus.OK;
            tokenCacheService.cacheToken(token, isValid);
            
            return response;
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            
            // Cache the negative result
            tokenCacheService.cacheToken(token, false);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Invalid token\"}");
        }
    }

    public void invalidateCachedToken(String token) {
        tokenCacheService.invalidateToken(token);
    }

    public boolean isRateLimited(String ipAddress) {
        return tokenCacheService.isRateLimited(ipAddress);
    }

    private String getServiceUrl(String serviceName) {
        switch (serviceName.toLowerCase()) {
            case "auth":
            case "authentication":
                return authServiceUrl;
            case "maps":
            case "map-storage":
                return mapStorageServiceUrl;
            case "solver":
            case "coloring":
                return coloringServiceUrl;
            default:
                throw new IllegalArgumentException("Unknown service: " + serviceName);
        }
    }

    public ResponseEntity<String> checkAllServicesHealth() {
        StringBuilder result = new StringBuilder();
        result.append("{\n");
        result.append("  \"gateway\": \"OK\",\n");
        
        // Check authentication service
        String authStatus = checkServiceHealth(authServiceUrl + "/auth/healthcheck");
        result.append("  \"authentication-service\": \"").append(authStatus).append("\",\n");
        result.append("  \"URL of authentication-service\": \"").append(authServiceUrl).append("\",\n");
        
        // Check map storage service
        String mapStorageStatus = checkServiceHealth(mapStorageServiceUrl + "/api/v1/maps/healthcheck");
        result.append("  \"map-storage-service\": \"").append(mapStorageStatus).append("\",\n");
        result.append("  \"URL of map-storage-service\": \"").append(mapStorageServiceUrl).append("\",\n");
        
        // Check solver service
        String solverStatus = checkServiceHealth(coloringServiceUrl + "/health");
        result.append("  \"solver-service\": \"").append(solverStatus).append("\",\n");
        result.append("  \"URL of solver-service\": \"").append(coloringServiceUrl).append("\"\n");
        
        result.append("}");
        
        // If any service is down, return 503
        if (authStatus.contains("FAIL") || mapStorageStatus.contains("FAIL") || solverStatus.contains("FAIL")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result.toString());
        }
        
        return ResponseEntity.ok(result.toString());
    }
    
    private String checkServiceHealth(String healthUrl) {
        try {
            logger.info("Checking health of: {}", healthUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return "OK";
            } else {
                return "FAIL - Status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            logger.error("Health check failed for {}: {}", healthUrl, e.getMessage());
            return "FAIL - " + e.getMessage();
        }
    }
} 