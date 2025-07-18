package com.fourcolour.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

    private final RestTemplate restTemplate;

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
            return response;
            
        } catch (Exception e) {
            logger.error("Error forwarding request to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"error\":\"Service unavailable\"}");
        }
    }

    public ResponseEntity<String> verifyToken(String token) {
        String url = authServiceUrl + "/auth/verify";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Invalid token\"}");
        }
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
        result.append("  \"solver-service\": \"").append(solverStatus).append("\"\n");
        result.append("  \"URL of solver-service\": \"").append(coloringServiceUrl).append("\",\n");
        
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