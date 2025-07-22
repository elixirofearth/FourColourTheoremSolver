package com.fourcolour.mapstorage.controller;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.mapstorage.entity.Map;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapControllerTest {

    @Mock
    private MapService mapService;

    @InjectMocks
    private MapController mapController;

    private MapRequest mapRequest;
    private Map testMap;

    @BeforeEach
    void setUp() {
        mapRequest = new MapRequest();
        mapRequest.setUserId("user123");
        mapRequest.setName("Test Map");
        mapRequest.setWidth(10);
        mapRequest.setHeight(10);
        mapRequest.setImageData("data:image/png;base64,test");
        mapRequest.setMatrix(new int[][]{{0, 1}, {1, 0}});

        testMap = new Map();
        testMap.setId("507f1f77bcf86cd799439011");
        testMap.setUserId("user123");
        testMap.setName("Test Map");
        testMap.setWidth(10);
        testMap.setHeight(10);
        testMap.setImageData("data:image/png;base64,test");
        testMap.setMatrix(new int[][]{{0, 1}, {1, 0}});
        testMap.setCreatedAt(LocalDateTime.now());
        testMap.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void healthCheck_ShouldReturnOK() {
        ResponseEntity<String> response = mapController.healthCheck();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void saveMap_WithValidRequest_ShouldReturnCreated() {
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithNullUserId_ShouldReturnBadRequest() {
        when(mapService.saveMap(any(MapRequest.class)))
                .thenThrow(new RuntimeException("UserID is required"));

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    void saveMap_WithRuntimeException_ShouldReturnBadRequest() {
        when(mapService.saveMap(any(MapRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Database connection failed"));
    }



    @Test
    void getMaps_WithValidUserId_ShouldReturnMaps() {
        List<Map> expectedMaps = Arrays.asList(testMap);
        when(mapService.getMapsByUserId("user123"))
                .thenReturn(expectedMaps);

        ResponseEntity<?> response = mapController.getMaps("user123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedMaps, response.getBody());
        verify(mapService).getMapsByUserId("user123");
    }

    @Test
    void getMaps_WithNullUserId_ShouldReturnBadRequest() {
        ResponseEntity<?> response = mapController.getMaps(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("UserID is required"));
        verify(mapService, never()).getMapsByUserId(anyString());
    }

    @Test
    void getMaps_WithEmptyUserId_ShouldReturnBadRequest() {
        ResponseEntity<?> response = mapController.getMaps("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("UserID is required"));
        verify(mapService, never()).getMapsByUserId(anyString());
    }

    @Test
    void getMaps_WithWhitespaceUserId_ShouldReturnBadRequest() {
        ResponseEntity<?> response = mapController.getMaps("   ");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("UserID is required"));
        verify(mapService, never()).getMapsByUserId(anyString());
    }

    @Test
    void getMaps_WithException_ShouldReturnInternalServerError() {
        when(mapService.getMapsByUserId("user123"))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = mapController.getMaps("user123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to fetch maps"));
    }

    @Test
    void getMap_WithValidId_ShouldReturnMap() {
        when(mapService.getMapById("507f1f77bcf86cd799439011"))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.getMap("507f1f77bcf86cd799439011");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).getMapById("507f1f77bcf86cd799439011");
    }

    @Test
    void getMap_WithNonExistentId_ShouldReturnNotFound() {
        when(mapService.getMapById("507f1f77bcf86cd799439011"))
                .thenReturn(null);

        ResponseEntity<?> response = mapController.getMap("507f1f77bcf86cd799439011");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Map not found"));
    }

    @Test
    void getMap_WithInvalidId_ShouldReturnBadRequest() {
        when(mapService.getMapById("invalid-id"))
                .thenThrow(new IllegalArgumentException("Invalid map ID format"));

        ResponseEntity<?> response = mapController.getMap("invalid-id");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid map ID"));
    }

    @Test
    void getMap_WithException_ShouldReturnInternalServerError() {
        when(mapService.getMapById("507f1f77bcf86cd799439011"))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = mapController.getMap("507f1f77bcf86cd799439011");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to retrieve map"));
    }

    @Test
    void deleteMap_WithValidId_ShouldReturnNoContent() {
        when(mapService.deleteMap("507f1f77bcf86cd799439011"))
                .thenReturn(true);

        ResponseEntity<?> response = mapController.deleteMap("507f1f77bcf86cd799439011");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(mapService).deleteMap("507f1f77bcf86cd799439011");
    }

    @Test
    void deleteMap_WithNonExistentId_ShouldReturnNotFound() {
        when(mapService.deleteMap("507f1f77bcf86cd799439011"))
                .thenReturn(false);

        ResponseEntity<?> response = mapController.deleteMap("507f1f77bcf86cd799439011");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Map not found"));
    }

    @Test
    void deleteMap_WithInvalidId_ShouldReturnBadRequest() {
        when(mapService.deleteMap("invalid-id"))
                .thenThrow(new IllegalArgumentException("Invalid map ID format"));

        ResponseEntity<?> response = mapController.deleteMap("invalid-id");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid map ID"));
    }

    @Test
    void deleteMap_WithException_ShouldReturnInternalServerError() {
        when(mapService.deleteMap("507f1f77bcf86cd799439011"))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = mapController.deleteMap("507f1f77bcf86cd799439011");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to delete map"));
    }

    @Test
    void saveMap_WithNullName_ShouldHandleGracefully() {
        mapRequest.setName(null);
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithNullDimensions_ShouldHandleGracefully() {
        mapRequest.setWidth(null);
        mapRequest.setHeight(null);
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithNullImageData_ShouldHandleGracefully() {
        mapRequest.setImageData(null);
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithNullMatrix_ShouldHandleGracefully() {
        mapRequest.setMatrix(null);
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void getMaps_WithEmptyList_ShouldReturnEmptyList() {
        when(mapService.getMapsByUserId("user123"))
                .thenReturn(Arrays.asList());

        ResponseEntity<?> response = mapController.getMaps("user123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void getMaps_WithMultipleMaps_ShouldReturnAllMaps() {
        Map map1 = new Map("user123", "Map 1", 10, 10, "data1", new int[][]{{0, 1}});
        Map map2 = new Map("user123", "Map 2", 20, 20, "data2", new int[][]{{1, 0}});
        List<Map> expectedMaps = Arrays.asList(map1, map2);
        when(mapService.getMapsByUserId("user123"))
                .thenReturn(expectedMaps);

        ResponseEntity<?> response = mapController.getMaps("user123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedMaps, response.getBody());
    }

    @Test
    void getMap_WithNullId_ShouldReturnBadRequest() {
        when(mapService.getMapById(null))
                .thenThrow(new IllegalArgumentException("Invalid map ID format"));

        ResponseEntity<?> response = mapController.getMap(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid map ID"));
    }

    @Test
    void deleteMap_WithNullId_ShouldReturnBadRequest() {
        when(mapService.deleteMap(null))
                .thenThrow(new IllegalArgumentException("Invalid map ID format"));

        ResponseEntity<?> response = mapController.deleteMap(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid map ID"));
    }

    @Test
    void saveMap_WithVeryLongUserId_ShouldHandleGracefully() {
        StringBuilder longUserId = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longUserId.append("a");
        }
        mapRequest.setUserId(longUserId.toString());
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithVeryLongName_ShouldHandleGracefully() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longName.append("a");
        }
        mapRequest.setName(longName.toString());
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithLargeMatrix_ShouldHandleGracefully() {
        int[][] largeMatrix = new int[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                largeMatrix[i][j] = i + j;
            }
        }
        mapRequest.setMatrix(largeMatrix);
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithSpecialCharactersInName_ShouldHandleGracefully() {
        mapRequest.setName("Test Map @#$%^&*()");
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void saveMap_WithUnicodeCharactersInName_ShouldHandleGracefully() {
        mapRequest.setName("Test Map \u00E9\u00F1");
        when(mapService.saveMap(any(MapRequest.class)))
                .thenReturn(testMap);

        ResponseEntity<?> response = mapController.saveMap(mapRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMap, response.getBody());
        verify(mapService).saveMap(mapRequest);
    }

    @Test
    void getMaps_WithSpecialCharactersInUserId_ShouldHandleGracefully() {
        when(mapService.getMapsByUserId("user@123!#$%"))
                .thenReturn(Arrays.asList(testMap));

        ResponseEntity<?> response = mapController.getMaps("user@123!#$%");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mapService).getMapsByUserId("user@123!#$%");
    }

    @Test
    void getMaps_WithUnicodeCharactersInUserId_ShouldHandleGracefully() {
        when(mapService.getMapsByUserId("user\u00E9\u00F1"))
                .thenReturn(Arrays.asList(testMap));

        ResponseEntity<?> response = mapController.getMaps("user\u00E9\u00F1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mapService).getMapsByUserId("user\u00E9\u00F1");
    }
} 