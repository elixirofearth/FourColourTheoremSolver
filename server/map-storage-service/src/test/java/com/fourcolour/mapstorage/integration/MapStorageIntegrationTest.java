package com.fourcolour.mapstorage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourcolour.common.service.LoggerClient;
import com.fourcolour.mapstorage.entity.Map;
import com.fourcolour.mapstorage.repository.MapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test for Map Storage Service
 * Tests the full map storage workflow with real database (MongoDB in-memory)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MapStorageIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MapRepository mapRepository;

    @MockBean
    private LoggerClient loggerClient;

    private ObjectMapper objectMapper;
    private String baseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/testdb");
        registry.add("spring.data.mongodb.database", () -> "testdb");
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("logging.level.org.springframework.web", () -> "WARN");
        registry.add("logging.level.org.springframework.data.mongodb", () -> "WARN");
        registry.add("server.tomcat.threads.max", () -> "200");
        registry.add("server.tomcat.threads.min-spare", () -> "10");
        registry.add("server.tomcat.accept-count", () -> "100");
    }

    @BeforeEach
    @Transactional
    void setUp() {
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:" + port;
        
        // Clear database with proper transaction handling
        mapRepository.deleteAll();
        
        // Reset mocks
        reset(loggerClient);
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
        
        // Configure TestRestTemplate with simple timeout settings
        restTemplate.getRestTemplate().setRequestFactory(
            new org.springframework.http.client.SimpleClientHttpRequestFactory()
        );
    }

    @Test
    @DisplayName("Application should start and health check should work")
    void applicationShouldStartAndHealthCheckShouldWork() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps/healthcheck", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    @DisplayName("Map creation should work with valid data")
    void mapCreation_ShouldWorkWithValidData() throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", "user123",
                "name", "Test Map",
                "width", 10,
                "height", 10,
                "imageData", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
                "matrix", new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user123"));
        assertTrue(response.getBody().contains("Test Map"));
        assertTrue(response.getBody().contains("507f1f77bcf86cd799439011") || response.getBody().contains("id"));
    }

    @Test
    @DisplayName("Map creation should fail with missing userId")
    void mapCreation_ShouldFailWithMissingUserId() throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "name", "Test Map",
                "width", 10,
                "height", 10,
                "imageData", "data:image/png;base64,test",
                "matrix", new int[][]{{0, 1}, {1, 0}}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    @Test
    @DisplayName("Map creation should fail with empty userId")
    void mapCreation_ShouldFailWithEmptyUserId() throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", "",
                "name", "Test Map",
                "width", 10,
                "height", 10,
                "imageData", "data:image/png;base64,test",
                "matrix", new int[][]{{0, 1}, {1, 0}}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    @Test
    @DisplayName("Map retrieval by userId should work")
    void mapRetrievalByUserId_ShouldWork() throws Exception {
        // First create a map
        String mapId = createMapAndGetId();
        
        // Then retrieve maps by userId
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps?userId=user123", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user123"));
        assertTrue(response.getBody().contains("Test Map"));
    }

    @Test
    @DisplayName("Map retrieval by userId should return empty for non-existent user")
    void mapRetrievalByUserId_ShouldReturnEmptyForNonExistentUser() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps?userId=nonexistent", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[]", response.getBody());
    }

    @Test
    @DisplayName("Map retrieval by userId should fail without userId parameter")
    void mapRetrievalByUserId_ShouldFailWithoutUserIdParameter() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps", String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("UserID is required"));
    }

    @Test
    @DisplayName("Map retrieval by id should work")
    void mapRetrievalById_ShouldWork() throws Exception {
        // First create a map
        String mapId = createMapAndGetId();
        
        // Then retrieve the specific map
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps/" + mapId, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user123"));
        assertTrue(response.getBody().contains("Test Map"));
        assertTrue(response.getBody().contains(mapId));
    }

    @Test
    @DisplayName("Map retrieval by id should fail with non-existent id")
    void mapRetrievalById_ShouldFailWithNonExistentId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps/507f1f77bcf86cd799439011", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Map not found"));
    }

    @Test
    @DisplayName("Map retrieval by id should fail with invalid id format")
    void mapRetrievalById_ShouldFailWithInvalidIdFormat() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps/invalid-id", String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid map ID"));
    }

    @Test
    @DisplayName("Map deletion should work")
    void mapDeletion_ShouldWork() throws Exception {
        // First create a map
        String mapId = createMapAndGetId();
        
        // Then delete the map
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps/" + mapId,
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Map deletion should fail with non-existent id")
    void mapDeletion_ShouldFailWithNonExistentId() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps/507f1f77bcf86cd799439011",
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Map not found"));
    }

    @Test
    @DisplayName("Map deletion should fail with invalid id format")
    void mapDeletion_ShouldFailWithInvalidIdFormat() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/maps/invalid-id",
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid map ID"));
    }

    @Test
    @DisplayName("Complete map workflow should work")
    void completeMapWorkflow_ShouldWork() throws Exception {
        // 1. Create a map
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", "user123",
                "name", "Workflow Test Map",
                "width", 20,
                "height", 20,
                "imageData", "data:image/png;base64,workflowtest",
                "matrix", new int[][]{{1, 0, 1}, {0, 1, 0}, {1, 0, 1}}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> createRequest = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> createResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", createRequest, String.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        String mapId = extractMapIdFromResponse(createResponse.getBody());

        // 2. Retrieve the map by id
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps/" + mapId, String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("Workflow Test Map"));

        // 3. Retrieve maps by userId
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps?userId=user123", String.class);

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertTrue(listResponse.getBody().contains("Workflow Test Map"));

        // 4. Delete the map
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                baseUrl + "/api/v1/maps/" + mapId,
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // 5. Verify map is deleted
        ResponseEntity<String> verifyResponse = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps/" + mapId, String.class);

        assertEquals(HttpStatus.NOT_FOUND, verifyResponse.getStatusCode());
    }

    @Test
    @DisplayName("Multiple maps for same user should work")
    void multipleMapsForSameUser_ShouldWork() throws Exception {
        // Create first map
        String mapId1 = createMapAndGetId("user123", "Map 1");
        
        // Create second map
        String mapId2 = createMapAndGetId("user123", "Map 2");
        
        // Create third map for different user
        String mapId3 = createMapAndGetId("user456", "Map 3");

        // Retrieve maps for user123
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/maps?userId=user123", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Map 1"));
        assertTrue(response.getBody().contains("Map 2"));
        assertFalse(response.getBody().contains("Map 3")); // Should not contain other user's map
    }

    @Test
    @DisplayName("Map creation with null values should work")
    void mapCreationWithNullValues_ShouldWork() throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", "user123",
                "name", null,
                "width", null,
                "height", null,
                "imageData", null,
                "matrix", null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user123"));
    }

    @Test
    @DisplayName("Map creation with large data should work")
    void mapCreationWithLargeData_ShouldWork() throws Exception {
        // Create large matrix
        int[][] largeMatrix = new int[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                largeMatrix[i][j] = i + j;
            }
        }

        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", "user123",
                "name", "Large Map",
                "width", 100,
                "height", 100,
                "imageData", "data:image/png;base64," + "A".repeat(10000),
                "matrix", largeMatrix
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Large Map"));
    }

    @Test
    @DisplayName("Map creation with special characters should work")
    void mapCreationWithSpecialCharacters_ShouldWork() throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", "user@123!#$%",
                "name", "Test Map @#$%^&*()",
                "width", 10,
                "height", 10,
                "imageData", "data:image/png;base64,test",
                "matrix", new int[][]{{0, 1}, {1, 0}}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user@123!#$%"));
        assertTrue(response.getBody().contains("Test Map @#$%^&*()"));
    }

    @Test
    @DisplayName("Malformed JSON should return bad request")
    void malformedJson_ShouldReturnBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{ invalid json }", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Missing required fields should return bad request")
    void missingRequiredFields_ShouldReturnBadRequest() throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "name", "Test Map",
                "width", 10,
                "height", 10
                // Missing userId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

    private String createMapAndGetId() throws Exception {
        return createMapAndGetId("user123", "Test Map");
    }

    private String createMapAndGetId(String userId, String name) throws Exception {
        java.util.Map<String, Object> mapData = java.util.Map.of(
                "userId", userId,
                "name", name,
                "width", 10,
                "height", 10,
                "imageData", "data:image/png;base64,test",
                "matrix", new int[][]{{0, 1}, {1, 0}}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(mapData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/maps", request, String.class);

        return extractMapIdFromResponse(response.getBody());
    }

    private String extractMapIdFromResponse(String responseBody) {
        // Simple extraction - in a real scenario you might use JSON parsing
        if (responseBody.contains("\"id\":")) {
            int startIndex = responseBody.indexOf("\"id\":\"") + 6;
            int endIndex = responseBody.indexOf("\"", startIndex);
            return responseBody.substring(startIndex, endIndex);
        }
        return "507f1f77bcf86cd799439011"; // Fallback for testing
    }
} 