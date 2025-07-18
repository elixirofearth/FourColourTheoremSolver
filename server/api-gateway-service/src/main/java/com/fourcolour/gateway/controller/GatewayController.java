package com.fourcolour.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.common.dto.ColoringRequest;
import com.fourcolour.gateway.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

@RestController
public class GatewayController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/healthcheck/services")
    public ResponseEntity<String> healthCheckServices() {
        return proxyService.checkAllServicesHealth();
    }

    // Auth routes (unprotected)
    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<String> register(@RequestBody String body, HttpServletRequest request) {
        return forwardToService("auth", "/auth/register", HttpMethod.POST, body, request);
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<String> login(@RequestBody String body, HttpServletRequest request) {
        return forwardToService("auth", "/auth/login", HttpMethod.POST, body, request);
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return forwardToService("auth", "/auth/logout", HttpMethod.POST, null, request);
    }

    // Map coloring route (protected)
    @PostMapping("/api/v1/maps/color")
    public ResponseEntity<String> colorMap(@RequestBody ColoringRequest coloringRequest, 
                                          HttpServletRequest request) {
        // Check authentication
        String authHeader = request.getHeader("Authorization");
        if (!isAuthenticated(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Authentication required\"}");
        }

        try {
            // Transform the request for the solver service
            Map<String, Object> solverRequest = Map.of(
                "image", coloringRequest.getImage().getData(),
                "width", coloringRequest.getWidth(),
                "height", coloringRequest.getHeight(),
                "userId", coloringRequest.getUserId() != null ? coloringRequest.getUserId() : "unknown"
            );

            String solverBody = objectMapper.writeValueAsString(solverRequest);
            
            HttpHeaders headers = extractHeaders(request);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
            return proxyService.forwardRequest("solver", "/api/solve", HttpMethod.POST, headers, solverBody);
            
        } catch (Exception e) {
            logger.error("Error processing coloring request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error processing request\"}");
        }
    }

    // Map storage routes (protected)
    @PostMapping("/api/v1/maps")
    public ResponseEntity<String> createMap(@RequestBody String body, HttpServletRequest request) {
        if (!isAuthenticated(request.getHeader("Authorization"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Authentication required\"}");
        }
        return forwardToService("maps", "/api/v1/maps", HttpMethod.POST, body, request);
    }

    @GetMapping("/api/v1/maps")
    public ResponseEntity<String> getMaps(HttpServletRequest request) {
        if (!isAuthenticated(request.getHeader("Authorization"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Authentication required\"}");
        }
        
        String queryString = request.getQueryString();
        String path = "/api/v1/maps" + (queryString != null ? "?" + queryString : "");
        return forwardToService("maps", path, HttpMethod.GET, null, request);
    }

    @GetMapping("/api/v1/maps/{id}")
    public ResponseEntity<String> getMap(@PathVariable String id, HttpServletRequest request) {
        if (!isAuthenticated(request.getHeader("Authorization"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Authentication required\"}");
        }
        return forwardToService("maps", "/api/v1/maps/" + id, HttpMethod.GET, null, request);
    }

    @PutMapping("/api/v1/maps/{id}")
    public ResponseEntity<String> updateMap(@PathVariable String id, @RequestBody String body, 
                                           HttpServletRequest request) {
        if (!isAuthenticated(request.getHeader("Authorization"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Authentication required\"}");
        }
        return forwardToService("maps", "/api/v1/maps/" + id, HttpMethod.PUT, body, request);
    }

    @DeleteMapping("/api/v1/maps/{id}")
    public ResponseEntity<String> deleteMap(@PathVariable String id, HttpServletRequest request) {
        if (!isAuthenticated(request.getHeader("Authorization"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Authentication required\"}");
        }
        return forwardToService("maps", "/api/v1/maps/" + id, HttpMethod.DELETE, null, request);
    }

    // Handle OPTIONS requests for CORS
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/**")
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<String> forwardToService(String serviceName, String path, HttpMethod method, 
                                                   String body, HttpServletRequest request) {
        HttpHeaders headers = extractHeaders(request);
        return proxyService.forwardRequest(serviceName, path, method, headers, body);
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            for (String headerName : Collections.list(headerNames)) {
                String headerValue = request.getHeader(headerName);
                headers.add(headerName, headerValue);
            }
        }
        
        // Ensure content type is set for POST requests
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        
        return headers;
    }

    private boolean isAuthenticated(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return false;
        }

        try {
            ResponseEntity<String> response = proxyService.verifyToken(authHeader);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("Authentication check failed: {}", e.getMessage());
            return false;
        }
    }
} 