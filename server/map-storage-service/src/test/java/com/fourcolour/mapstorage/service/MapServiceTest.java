package com.fourcolour.mapstorage.service;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.common.service.LoggerClient;
import com.fourcolour.mapstorage.entity.Map;
import com.fourcolour.mapstorage.repository.MapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapServiceTest {

    @Mock
    private MapRepository mapRepository;

    @Mock
    private LoggerClient loggerClient;

    @InjectMocks
    private MapService mapService;

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

        // Setup common mocks
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    // ==================== SAVE MAP TESTS ====================

    @Test
    void saveMap_WithValidRequest_ShouldReturnSavedMap() {
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        assertEquals("507f1f77bcf86cd799439011", savedMap.getId());
        assertEquals("user123", savedMap.getUserId());
        assertEquals("Test Map", savedMap.getName());
        assertEquals(10, savedMap.getWidth());
        assertEquals(10, savedMap.getHeight());
        assertEquals("data:image/png;base64,test", savedMap.getImageData());
        assertArrayEquals(new int[][]{{0, 1}, {1, 0}}, savedMap.getMatrix());

        verify(mapRepository).save(any(Map.class));
        verify(loggerClient).logEvent(
                eq("map-storage-service"),
                eq("map_created"),
                eq("user123"),
                contains("Map created: Test Map"),
                eq(1),
                any()
        );
    }

    @Test
    void saveMap_WithNullUserId_ShouldThrowException() {
        mapRequest.setUserId(null);

        assertThrows(RuntimeException.class, () -> {
            mapService.saveMap(mapRequest);
        });

        verify(mapRepository, never()).save(any(Map.class));
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void saveMap_WithEmptyUserId_ShouldThrowException() {
        mapRequest.setUserId("");

        assertThrows(RuntimeException.class, () -> {
            mapService.saveMap(mapRequest);
        });

        verify(mapRepository, never()).save(any(Map.class));
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void saveMap_WithWhitespaceUserId_ShouldThrowException() {
        mapRequest.setUserId("   ");

        assertThrows(RuntimeException.class, () -> {
            mapService.saveMap(mapRequest);
        });

        verify(mapRepository, never()).save(any(Map.class));
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void saveMap_WithNullName_ShouldCreateMapWithNullName() {
        mapRequest.setName(null);
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
        verify(loggerClient).logEvent(
                eq("map-storage-service"),
                eq("map_created"),
                eq("user123"),
                contains("Map created: null"),
                eq(1),
                any()
        );
    }

    @Test
    void saveMap_WithNullDimensions_ShouldCreateMapWithNullDimensions() {
        mapRequest.setWidth(null);
        mapRequest.setHeight(null);
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithNullImageData_ShouldCreateMapWithNullImageData() {
        mapRequest.setImageData(null);
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithNullMatrix_ShouldCreateMapWithNullMatrix() {
        mapRequest.setMatrix(null);
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithEmptyName_ShouldCreateMapWithEmptyName() {
        mapRequest.setName("");
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
        verify(loggerClient).logEvent(
                eq("map-storage-service"),
                eq("map_created"),
                eq("user123"),
                contains("Map created: "),
                eq(1),
                any()
        );
    }

    @Test
    void saveMap_WithRepositoryException_ShouldPropagateException() {
        when(mapRepository.save(any(Map.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            mapService.saveMap(mapRequest);
        });

        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    // ==================== GET MAPS BY USER ID TESTS ====================

    @Test
    void getMapsByUserId_WithValidUserId_ShouldReturnMaps() {
        List<Map> expectedMaps = Arrays.asList(testMap);
        when(mapRepository.findByUserId("user123")).thenReturn(expectedMaps);

        List<Map> maps = mapService.getMapsByUserId("user123");

        assertNotNull(maps);
        assertEquals(1, maps.size());
        assertEquals("user123", maps.get(0).getUserId());
        verify(mapRepository).findByUserId("user123");
    }

    @Test
    void getMapsByUserId_WithNonExistentUserId_ShouldReturnEmptyList() {
        when(mapRepository.findByUserId("nonexistent")).thenReturn(Arrays.asList());

        List<Map> maps = mapService.getMapsByUserId("nonexistent");

        assertNotNull(maps);
        assertTrue(maps.isEmpty());
        verify(mapRepository).findByUserId("nonexistent");
    }

    @Test
    void getMapsByUserId_WithNullUserId_ShouldReturnEmptyList() {
        when(mapRepository.findByUserId(null)).thenReturn(Arrays.asList());

        List<Map> maps = mapService.getMapsByUserId(null);

        assertNotNull(maps);
        assertTrue(maps.isEmpty());
        verify(mapRepository).findByUserId(null);
    }

    @Test
    void getMapsByUserId_WithEmptyUserId_ShouldReturnEmptyList() {
        when(mapRepository.findByUserId("")).thenReturn(Arrays.asList());

        List<Map> maps = mapService.getMapsByUserId("");

        assertNotNull(maps);
        assertTrue(maps.isEmpty());
        verify(mapRepository).findByUserId("");
    }

    @Test
    void getMapsByUserId_WithMultipleMaps_ShouldReturnAllMaps() {
        Map map1 = new Map("user123", "Map 1", 10, 10, "data1", new int[][]{{0, 1}});
        Map map2 = new Map("user123", "Map 2", 20, 20, "data2", new int[][]{{1, 0}});
        List<Map> expectedMaps = Arrays.asList(map1, map2);
        when(mapRepository.findByUserId("user123")).thenReturn(expectedMaps);

        List<Map> maps = mapService.getMapsByUserId("user123");

        assertNotNull(maps);
        assertEquals(2, maps.size());
        assertEquals("Map 1", maps.get(0).getName());
        assertEquals("Map 2", maps.get(1).getName());
        verify(mapRepository).findByUserId("user123");
    }

    // ==================== GET MAP BY ID TESTS ====================

    @Test
    void getMapById_WithValidId_ShouldReturnMap() {
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMap));

        Map foundMap = mapService.getMapById("507f1f77bcf86cd799439011");

        assertNotNull(foundMap);
        assertEquals("507f1f77bcf86cd799439011", foundMap.getId());
        assertEquals("user123", foundMap.getUserId());
        assertEquals("Test Map", foundMap.getName());
        verify(mapRepository).findById("507f1f77bcf86cd799439011");
    }

    @Test
    void getMapById_WithNonExistentId_ShouldReturnNull() {
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.empty());

        Map foundMap = mapService.getMapById("507f1f77bcf86cd799439011");

        assertNull(foundMap);
        verify(mapRepository).findById("507f1f77bcf86cd799439011");
    }

    @Test
    void getMapById_WithInvalidObjectId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.getMapById("invalid-id");
        });

        verify(mapRepository, never()).findById(anyString());
    }

    @Test
    void getMapById_WithNullId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.getMapById(null);
        });

        verify(mapRepository, never()).findById(anyString());
    }

    @Test
    void getMapById_WithShortId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.getMapById("123");
        });

        verify(mapRepository, never()).findById(anyString());
    }

    @Test
    void getMapById_WithLongId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.getMapById("507f1f77bcf86cd799439011123456789");
        });

        verify(mapRepository, never()).findById(anyString());
    }

    @Test
    void getMapById_WithInvalidCharacters_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.getMapById("507f1f77bcf86cd79943901g");
        });

        verify(mapRepository, never()).findById(anyString());
    }

    // ==================== DELETE MAP TESTS ====================

    @Test
    void deleteMap_WithValidId_ShouldReturnTrue() {
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMap));
        doNothing().when(mapRepository).deleteById("507f1f77bcf86cd799439011");

        boolean deleted = mapService.deleteMap("507f1f77bcf86cd799439011");

        assertTrue(deleted);
        verify(mapRepository).findById("507f1f77bcf86cd799439011");
        verify(mapRepository).deleteById("507f1f77bcf86cd799439011");
        verify(loggerClient).logEvent(
                eq("map-storage-service"),
                eq("map_deleted"),
                eq("user123"),
                contains("Map deleted: Test Map"),
                eq(1),
                any()
        );
    }

    @Test
    void deleteMap_WithNonExistentId_ShouldReturnFalse() {
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.empty());

        boolean deleted = mapService.deleteMap("507f1f77bcf86cd799439011");

        assertFalse(deleted);
        verify(mapRepository).findById("507f1f77bcf86cd799439011");
        verify(mapRepository, never()).deleteById(anyString());
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void deleteMap_WithInvalidObjectId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.deleteMap("invalid-id");
        });

        verify(mapRepository, never()).findById(anyString());
        verify(mapRepository, never()).deleteById(anyString());
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void deleteMap_WithNullId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.deleteMap(null);
        });

        verify(mapRepository, never()).findById(anyString());
        verify(mapRepository, never()).deleteById(anyString());
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void deleteMap_WithShortId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.deleteMap("123");
        });

        verify(mapRepository, never()).findById(anyString());
        verify(mapRepository, never()).deleteById(anyString());
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void deleteMap_WithLongId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.deleteMap("507f1f77bcf86cd799439011123456789");
        });

        verify(mapRepository, never()).findById(anyString());
        verify(mapRepository, never()).deleteById(anyString());
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void deleteMap_WithInvalidCharacters_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapService.deleteMap("507f1f77bcf86cd79943901g");
        });

        verify(mapRepository, never()).findById(anyString());
        verify(mapRepository, never()).deleteById(anyString());
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void deleteMap_WithNullMapName_ShouldHandleGracefully() {
        testMap.setName(null);
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMap));
        doNothing().when(mapRepository).deleteById("507f1f77bcf86cd799439011");

        boolean deleted = mapService.deleteMap("507f1f77bcf86cd799439011");

        assertTrue(deleted);
        verify(loggerClient).logEvent(
                eq("map-storage-service"),
                eq("map_deleted"),
                eq("user123"),
                contains("Map deleted: null"),
                eq(1),
                any()
        );
    }

    @Test
    void deleteMap_WithRepositoryException_ShouldPropagateException() {
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMap));
        doThrow(new RuntimeException("Database error")).when(mapRepository).deleteById("507f1f77bcf86cd799439011");

        assertThrows(RuntimeException.class, () -> {
            mapService.deleteMap("507f1f77bcf86cd799439011");
        });

        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    // ==================== EDGE CASES ====================

    @Test
    void saveMap_WithVeryLongUserId_ShouldHandleGracefully() {
        StringBuilder longUserId = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longUserId.append("a");
        }
        mapRequest.setUserId(longUserId.toString());
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithVeryLongName_ShouldHandleGracefully() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longName.append("a");
        }
        mapRequest.setName(longName.toString());
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
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
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithSpecialCharactersInName_ShouldHandleGracefully() {
        mapRequest.setName("Test Map @#$%^&*()");
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithUnicodeCharactersInName_ShouldHandleGracefully() {
        mapRequest.setName("Test Map \u00E9\u00F1");
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
    }

    @Test
    void saveMap_WithLoggerException_ShouldNotAffectMainFlow() {
        doThrow(new RuntimeException("Logger error")).when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
        when(mapRepository.save(any(Map.class))).thenReturn(testMap);

        Map savedMap = mapService.saveMap(mapRequest);

        assertNotNull(savedMap);
        verify(mapRepository).save(any(Map.class));
        // Logger exception should not affect the main flow
    }

    @Test
    void deleteMap_WithLoggerException_ShouldNotAffectMainFlow() {
        testMap.setName(null);
        when(mapRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMap));
        doNothing().when(mapRepository).deleteById("507f1f77bcf86cd799439011");
        doThrow(new RuntimeException("Logger error")).when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());

        boolean deleted = mapService.deleteMap("507f1f77bcf86cd799439011");

        assertTrue(deleted);
        verify(mapRepository).deleteById("507f1f77bcf86cd799439011");
        // Logger exception should not affect the main flow
    }
} 