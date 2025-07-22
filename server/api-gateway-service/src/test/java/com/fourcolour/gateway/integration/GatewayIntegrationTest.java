package com.fourcolour.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.gateway.controller.GatewayController;
import com.fourcolour.gateway.service.ProxyService;
import com.fourcolour.gateway.service.TokenCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(GatewayController.class)
@ActiveProfiles("test")
class GatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProxyService proxyService;

    @MockBean
    private TokenCacheService tokenCacheService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void healthCheck_ShouldReturnOK() throws Exception {
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void register_WithValidData_ShouldReturnCreated() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("username", "testuser", "email", "test@example.com", "password", "password123")
        );

        when(tokenCacheService.isRateLimited(anyString())).thenReturn(false);
        when(proxyService.forwardRequest(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok("{\"id\":1,\"username\":\"testuser\"}"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"username\":\"testuser\"}"));
    }

    @Test
    void colorMap_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("image", Map.of("data", new int[]{1, 2, 3, 4}), "width", 800, "height", 600)
        );

        when(tokenCacheService.isRateLimited(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/v1/maps/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Authentication required\"}"));
    }

    @Test
    void optionsRequest_ShouldReturnOK() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login"))
                .andExpect(status().isOk());
    }
}
