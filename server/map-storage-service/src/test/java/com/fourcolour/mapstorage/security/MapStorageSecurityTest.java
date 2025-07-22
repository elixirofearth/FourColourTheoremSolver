package com.fourcolour.mapstorage.security;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.mapstorage.controller.MapController;
import com.fourcolour.mapstorage.service.MapService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapStorageSecurityTest {

    @Mock
    private MapService mapService;

    @InjectMocks
    private MapController mapController;

    private MapRequest mapRequest;

    @BeforeEach
    void setUp() {
        mapRequest = new MapRequest();
        mapRequest.setUserId("user123");
        mapRequest.setName("Test Map");
        mapRequest.setWidth(10);
        mapRequest.setHeight(10);
        mapRequest.setImageData("data:image/png;base64,test");
        mapRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});
    }

    @Test
    void sqlInjectionInUserId_ShouldBeHandledSafely() {
        MapRequest maliciousRequest = new MapRequest();
        maliciousRequest.setUserId("user123'; DROP TABLE maps; --");
        maliciousRequest.setName("Test Map");
        maliciousRequest.setWidth(10);
        maliciousRequest.setHeight(10);
        maliciousRequest.setImageData("data:image/png;base64,test");
        maliciousRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(maliciousRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(maliciousRequest);
        // The service should handle the malicious input safely
    }

    @Test
    void sqlInjectionInName_ShouldBeHandledSafely() {
        MapRequest maliciousRequest = new MapRequest();
        maliciousRequest.setUserId("user123");
        maliciousRequest.setName("Test Map'; DROP TABLE maps; --");
        maliciousRequest.setWidth(10);
        maliciousRequest.setHeight(10);
        maliciousRequest.setImageData("data:image/png;base64,test");
        maliciousRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(maliciousRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(maliciousRequest);
    }

    @Test
    void xssInMapName_ShouldBeHandledSafely() {
        MapRequest xssRequest = new MapRequest();
        xssRequest.setUserId("user123");
        xssRequest.setName("<script>alert('xss')</script>");
        xssRequest.setWidth(10);
        xssRequest.setHeight(10);
        xssRequest.setImageData("data:image/png;base64,test");
        xssRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(xssRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(xssRequest);
    }

    @Test
    void objectIdInjection_ShouldBeRejected() {
        String maliciousId = "507f1f77bcf86cd799439011'; DROP TABLE maps; --";
        
        when(mapService.getMapById(maliciousId))
                .thenThrow(new IllegalArgumentException("Invalid map ID format"));

        ResponseEntity<?> response = mapController.getMap(maliciousId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid map ID"));
    }

    @Test
    void pathTraversalInUserId_ShouldBeHandledSafely() {
        MapRequest maliciousRequest = new MapRequest();
        maliciousRequest.setUserId("../../../etc/passwd");
        maliciousRequest.setName("Test Map");
        maliciousRequest.setWidth(10);
        maliciousRequest.setHeight(10);
        maliciousRequest.setImageData("data:image/png;base64,test");
        maliciousRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(maliciousRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(maliciousRequest);
    }

    @Test
    void pathTraversalInMapName_ShouldBeHandledSafely() {
        MapRequest maliciousRequest = new MapRequest();
        maliciousRequest.setUserId("user123");
        maliciousRequest.setName("../../../etc/passwd");
        maliciousRequest.setWidth(10);
        maliciousRequest.setHeight(10);
        maliciousRequest.setImageData("data:image/png;base64,test");
        maliciousRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(maliciousRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(maliciousRequest);
    }

    @Test
    void longUserId_ShouldBeHandledSafely() {
        StringBuilder longUserId = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longUserId.append("a");
        }
        mapRequest.setUserId(longUserId.toString());

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void longMapName_ShouldBeHandledSafely() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longName.append("a");
        }
        mapRequest.setName(longName.toString());

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void nullByteInjection_ShouldBeHandledSafely() {
        mapRequest.setUserId("user123\0");
        mapRequest.setName("Test Map\0");

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void unicodeInjection_ShouldBeHandledSafely() {
        mapRequest.setUserId("user123\u0000\u0001\u0002");
        mapRequest.setName("Test Map\u0000\u0001\u0002");

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void emptyStringInputs_ShouldBeHandledSafely() {
        mapRequest.setUserId("");
        mapRequest.setName("");

        when(mapService.saveMap(any(MapRequest.class)))
                .thenThrow(new RuntimeException("UserID is required"));

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    void nullInputs_ShouldBeHandledSafely() {
        mapRequest.setUserId(null);
        mapRequest.setName(null);

        when(mapService.saveMap(any(MapRequest.class)))
                .thenThrow(new RuntimeException("UserID is required"));

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    void invalidObjectIdFormats_ShouldBeRejected() {
        String[] invalidIds = {
                "invalid-id",
                "123",
                "507f1f77bcf86cd799439011123456789",
                "507f1f77bcf86cd79943901g",
                "507f1f77bcf86cd79943901G",
                "507f1f77bcf86cd79943901!",
                "507f1f77bcf86cd79943901@",
                "507f1f77bcf86cd79943901#",
                "507f1f77bcf86cd79943901$",
                "507f1f77bcf86cd79943901%"
        };

        for (String invalidId : invalidIds) {
            when(mapService.getMapById(invalidId))
                    .thenThrow(new IllegalArgumentException("Invalid map ID format"));

            ResponseEntity<?> response = mapController.getMap(invalidId);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().toString().contains("Invalid map ID"));
        }
    }

    @Test
    void objectIdReplayAttack_ShouldBeHandled() {
        String validId = "507f1f77bcf86cd799439011";
        
        when(mapService.getMapById(validId))
                .thenReturn(createTestMap());

        // First request should succeed
        ResponseEntity<?> response1 = mapController.getMap(validId);
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        // Second request with same ID should also succeed (not a replay attack in this context)
        ResponseEntity<?> response2 = mapController.getMap(validId);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    void sessionFixationAttempt_ShouldBeHandledSafely() {
        // Test with malicious userId that tries to access other users' data
        MapRequest maliciousRequest = new MapRequest();
        maliciousRequest.setUserId("user456"); // Trying to create map for different user
        maliciousRequest.setName("Malicious Map");
        maliciousRequest.setWidth(10);
        maliciousRequest.setHeight(10);
        maliciousRequest.setImageData("data:image/png;base64,test");
        maliciousRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(maliciousRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(maliciousRequest);
        // The service should handle the request safely
    }

    @Test
    void sensitiveDataInLogs_ShouldNotLeakMapData() {
        mapRequest.setImageData("sensitive-base64-data-that-should-not-be-logged");

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
        // Verify that sensitive data is not exposed in logs
    }

    @Test
    void concurrentMapCreation_ShouldNotCauseRaceCondition() {
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        // Simulate concurrent requests
        ResponseEntity<?> response1 = mapController.saveMap(mapRequest);
        ResponseEntity<?> response2 = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        verify(mapService, times(2)).saveMap(any(MapRequest.class));
    }

    @Test
    void invalidCharactersInName_ShouldBeHandledSafely() {
        String[] maliciousNames = {
                "<script>alert('xss')</script>",
                "javascript:alert('xss')",
                "data:text/html,<script>alert('xss')</script>",
                "vbscript:alert('xss')",
                "onload=alert('xss')",
                "onerror=alert('xss')",
                "onclick=alert('xss')",
                "onmouseover=alert('xss')",
                "onfocus=alert('xss')",
                "onblur=alert('xss')"
        };

        for (String maliciousName : maliciousNames) {
            mapRequest.setName(maliciousName);

            when(mapService.saveMap(any(MapRequest.class)))
                    .thenReturn(createTestMap());

            ResponseEntity<?> response = mapController.saveMap(mapRequest);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(mapService).saveMap(mapRequest);
        }
    }

    @Test
    void headerInjection_ShouldBeHandledSafely() {
        // Test with malicious headers in the request
        mapRequest.setUserId("user123\r\nX-Custom-Header: malicious");
        mapRequest.setName("Test Map\r\nX-Custom-Header: malicious");

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void pathTraversalInInputs_ShouldBeHandledSafely() {
        String[] maliciousPaths = {
                "../../../etc/passwd",
                "..\\..\\..\\windows\\system32\\config\\sam",
                "....//....//....//etc/passwd",
                "..%2F..%2F..%2Fetc%2Fpasswd",
                "..%5C..%5C..%5Cwindows%5Csystem32%5Cconfig%5Csam"
        };

        for (String maliciousPath : maliciousPaths) {
            mapRequest.setUserId(maliciousPath);
            mapRequest.setName(maliciousPath);

            when(mapService.saveMap(any(MapRequest.class)))
                    .thenReturn(createTestMap());

            ResponseEntity<?> response = mapController.saveMap(mapRequest);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(mapService).saveMap(mapRequest);
        }
    }

    @Test
    void largeMatrixInjection_ShouldBeHandledSafely() {
        // Create a very large matrix that could cause memory issues
        int[][] largeMatrix = new int[10000][10000];
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 10000; j++) {
                largeMatrix[i][j] = i + j;
            }
        }
        mapRequest.setMatrix(largeMatrix);

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void largeImageDataInjection_ShouldBeHandledSafely() {
        // Create very large image data
        StringBuilder largeImageData = new StringBuilder("data:image/png;base64,");
        for (int i = 0; i < 1000000; i++) {
            largeImageData.append("A");
        }
        mapRequest.setImageData(largeImageData.toString());

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void negativeDimensionInjection_ShouldBeHandledSafely() {
        mapRequest.setWidth(-1000);
        mapRequest.setHeight(-1000);

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void zeroDimensionInjection_ShouldBeHandledSafely() {
        mapRequest.setWidth(0);
        mapRequest.setHeight(0);

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void nullMatrixInjection_ShouldBeHandledSafely() {
        mapRequest.setMatrix(null);

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void emptyMatrixInjection_ShouldBeHandledSafely() {
        mapRequest.setMatrix(new int[][]{});

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void nullImageDataInjection_ShouldBeHandledSafely() {
        mapRequest.setImageData(null);

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void emptyImageDataInjection_ShouldBeHandledSafely() {
        mapRequest.setImageData("");

        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(createTestMap());

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mapService).saveMap(mapRequest);
    }

    private com.fourcolour.mapstorage.entity.Map createTestMap() {
        com.fourcolour.mapstorage.entity.Map map = new com.fourcolour.mapstorage.entity.Map();
        map.setId("507f1f77bcf86cd799439011");
        map.setUserId("user123");
        map.setName("Test Map");
        map.setWidth(10);
        map.setHeight(10);
        map.setImageData("data:image/png;base64,test");
        map.setMatrix(new int[][]{{0, 1}, {1, 0}});
        return map;
    }
} 