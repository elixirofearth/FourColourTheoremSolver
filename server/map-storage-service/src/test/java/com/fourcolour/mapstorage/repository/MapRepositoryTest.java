package com.fourcolour.mapstorage.repository;

import com.fourcolour.mapstorage.entity.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MapRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MapRepository mapRepository;

    private Map testMap;

    @BeforeEach
    void setUp() {
        testMap = new Map();
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
    void findByUserId_WithExistingUserId_ShouldReturnMaps() {
        // Save map to database
        mongoTemplate.save(testMap);

        List<Map> found = mapRepository.findByUserId("user123");

        assertFalse(found.isEmpty());
        assertEquals("user123", found.get(0).getUserId());
        assertEquals("Test Map", found.get(0).getName());
        assertEquals(10, found.get(0).getWidth());
        assertEquals(10, found.get(0).getHeight());
    }

    @Test
    void findByUserId_WithNonExistentUserId_ShouldReturnEmpty() {
        List<Map> found = mapRepository.findByUserId("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void findByUserId_WithNullUserId_ShouldReturnEmpty() {
        List<Map> found = mapRepository.findByUserId(null);

        assertTrue(found.isEmpty());
    }

    @Test
    void findByUserId_WithEmptyUserId_ShouldReturnEmpty() {
        List<Map> found = mapRepository.findByUserId("");

        assertTrue(found.isEmpty());
    }

    @Test
    void save_WithValidMap_ShouldPersistMap() {
        Map savedMap = mapRepository.save(testMap);

        assertNotNull(savedMap.getId());
        assertEquals("user123", savedMap.getUserId());
        assertEquals("Test Map", savedMap.getName());
        assertEquals(10, savedMap.getWidth());
        assertEquals(10, savedMap.getHeight());
        assertEquals("data:image/png;base64,test", savedMap.getImageData());
        assertArrayEquals(new int[][]{{0, 1}, {1, 0}}, savedMap.getMatrix());
    }

    @Test
    void save_WithMapHavingNullName_ShouldPersistMap() {
        testMap.setName(null);
        
        Map savedMap = mapRepository.save(testMap);

        assertNotNull(savedMap.getId());
        assertNull(savedMap.getName());
    }

    @Test
    void save_WithMapHavingEmptyName_ShouldPersistMap() {
        testMap.setName("");
        
        Map savedMap = mapRepository.save(testMap);

        assertNotNull(savedMap.getId());
        assertEquals("", savedMap.getName());
    }

    @Test
    void findById_WithExistingId_ShouldReturnMap() {
        Map savedMap = mongoTemplate.save(testMap);

        Optional<Map> found = mapRepository.findById(savedMap.getId());

        assertTrue(found.isPresent());
        assertEquals(savedMap.getId(), found.get().getId());
        assertEquals("user123", found.get().getUserId());
        assertEquals("Test Map", found.get().getName());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<Map> found = mapRepository.findById("507f1f77bcf86cd799439011");

        assertFalse(found.isPresent());
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mapRepository.findById(null);
        });
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteMap() {
        Map savedMap = mongoTemplate.save(testMap);

        mapRepository.deleteById(savedMap.getId());

        Optional<Map> found = mapRepository.findById(savedMap.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithNonExistentId_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            mapRepository.deleteById("507f1f77bcf86cd799439011");
        });
    }

    @Test
    void findAll_WithMultipleMaps_ShouldReturnAllMaps() {
        Map map1 = new Map("user123", "Map 1", 10, 10, "data1", new int[][]{{0, 1}});
        Map map2 = new Map("user123", "Map 2", 20, 20, "data2", new int[][]{{1, 0}});
        Map map3 = new Map("user456", "Map 3", 30, 30, "data3", new int[][]{{0, 0}});

        mongoTemplate.save(map1);
        mongoTemplate.save(map2);
        mongoTemplate.save(map3);

        List<Map> allMaps = mapRepository.findAll();

        assertEquals(3, allMaps.size());
    }

    @Test
    void findAll_WithNoMaps_ShouldReturnEmptyList() {
        List<Map> allMaps = mapRepository.findAll();

        assertTrue(allMaps.isEmpty());
    }

    @Test
    void count_WithMultipleMaps_ShouldReturnCorrectCount() {
        Map map1 = new Map("user123", "Map 1", 10, 10, "data1", new int[][]{{0, 1}});
        Map map2 = new Map("user123", "Map 2", 20, 20, "data2", new int[][]{{1, 0}});

        mongoTemplate.save(map1);
        mongoTemplate.save(map2);

        long count = mapRepository.count();

        assertEquals(2, count);
    }

    @Test
    void count_WithNoMaps_ShouldReturnZero() {
        long count = mapRepository.count();

        assertEquals(0, count);
    }

    @Test
    void findByUserId_WithCaseInsensitiveUserId_ShouldNotMatch() {
        mongoTemplate.save(testMap);

        List<Map> found = mapRepository.findByUserId("USER123");

        assertTrue(found.isEmpty());
    }

    @Test
    void findByUserId_WithExtraWhitespace_ShouldNotMatch() {
        mongoTemplate.save(testMap);

        List<Map> found = mapRepository.findByUserId(" user123 ");

        assertTrue(found.isEmpty());
    }

    @Test
    void save_AndUpdate_ShouldPersistChanges() {
        Map savedMap = mongoTemplate.save(testMap);

        savedMap.setName("Updated Map");
        savedMap.setWidth(20);
        savedMap.setHeight(20);

        Map updatedMap = mapRepository.save(savedMap);

        assertEquals("Updated Map", updatedMap.getName());
        assertEquals(20, updatedMap.getWidth());
        assertEquals(20, updatedMap.getHeight());
    }

    @Test
    void findByUserId_WithMultipleUsers_ShouldReturnCorrectResults() {
        Map map1 = new Map("user123", "Map 1", 10, 10, "data1", new int[][]{{0, 1}});
        Map map2 = new Map("user123", "Map 2", 20, 20, "data2", new int[][]{{1, 0}});
        Map map3 = new Map("user456", "Map 3", 30, 30, "data3", new int[][]{{0, 0}});

        mongoTemplate.save(map1);
        mongoTemplate.save(map2);
        mongoTemplate.save(map3);

        List<Map> user123Maps = mapRepository.findByUserId("user123");
        List<Map> user456Maps = mapRepository.findByUserId("user456");

        assertEquals(2, user123Maps.size());
        assertEquals(1, user456Maps.size());
    }

    @Test
    void save_WithLongUserId_ShouldPersistMap() {
        StringBuilder longUserId = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longUserId.append("a");
        }
        testMap.setUserId(longUserId.toString());

        Map savedMap = mapRepository.save(testMap);

        assertEquals(longUserId.toString(), savedMap.getUserId());
    }

    @Test
    void save_WithLongName_ShouldPersistMap() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("a");
        }
        testMap.setName(longName.toString());

        Map savedMap = mapRepository.save(testMap);

        assertEquals(longName.toString(), savedMap.getName());
    }

    @Test
    void save_WithSpecialCharactersInUserId_ShouldPersistMap() {
        testMap.setUserId("user@123!#$%");

        Map savedMap = mapRepository.save(testMap);

        assertEquals("user@123!#$%", savedMap.getUserId());
    }

    @Test
    void save_WithSpecialCharactersInName_ShouldPersistMap() {
        testMap.setName("Test Map @#$%^&*()");

        Map savedMap = mapRepository.save(testMap);

        assertEquals("Test Map @#$%^&*()", savedMap.getName());
    }

    @Test
    void save_WithUnicodeCharacters_ShouldPersistMap() {
        testMap.setUserId("user\u00E9\u00F1");
        testMap.setName("Map\u00E9\u00F1");

        Map savedMap = mapRepository.save(testMap);

        assertEquals("user\u00E9\u00F1", savedMap.getUserId());
        assertEquals("Map\u00E9\u00F1", savedMap.getName());
    }

    @Test
    void findByUserId_AfterDelete_ShouldReturnEmpty() {
        Map savedMap = mongoTemplate.save(testMap);

        mapRepository.deleteById(savedMap.getId());

        List<Map> found = mapRepository.findByUserId("user123");

        assertTrue(found.isEmpty());
    }

    @Test
    void save_WithValidImageDataFormats_ShouldPersistAllMaps() {
        Map map1 = new Map("user123", "Map 1", 10, 10, "data:image/png;base64,test1", new int[][]{{0, 1}});
        Map map2 = new Map("user123", "Map 2", 20, 20, "data:image/jpeg;base64,test2", new int[][]{{1, 0}});
        Map map3 = new Map("user123", "Map 3", 30, 30, "data:image/gif;base64,test3", new int[][]{{0, 0}});

        Map savedMap1 = mapRepository.save(map1);
        Map savedMap2 = mapRepository.save(map2);
        Map savedMap3 = mapRepository.save(map3);

        assertNotNull(savedMap1.getId());
        assertNotNull(savedMap2.getId());
        assertNotNull(savedMap3.getId());
    }

    @Test
    void save_WithLargeMatrix_ShouldPersistMap() {
        int[][] largeMatrix = new int[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                largeMatrix[i][j] = i + j;
            }
        }
        testMap.setMatrix(largeMatrix);

        Map savedMap = mapRepository.save(testMap);

        assertArrayEquals(largeMatrix, savedMap.getMatrix());
    }

    @Test
    void save_WithNullMatrix_ShouldPersistMap() {
        testMap.setMatrix(null);

        Map savedMap = mapRepository.save(testMap);

        assertNull(savedMap.getMatrix());
    }

    @Test
    void save_WithEmptyMatrix_ShouldPersistMap() {
        testMap.setMatrix(new int[][]{});

        Map savedMap = mapRepository.save(testMap);

        assertArrayEquals(new int[][]{}, savedMap.getMatrix());
    }

    @Test
    void save_WithNullDimensions_ShouldPersistMap() {
        testMap.setWidth(null);
        testMap.setHeight(null);

        Map savedMap = mapRepository.save(testMap);

        assertNull(savedMap.getWidth());
        assertNull(savedMap.getHeight());
    }

    @Test
    void save_WithNegativeDimensions_ShouldPersistMap() {
        testMap.setWidth(-10);
        testMap.setHeight(-20);

        Map savedMap = mapRepository.save(testMap);

        assertEquals(-10, savedMap.getWidth());
        assertEquals(-20, savedMap.getHeight());
    }

    @Test
    void save_WithZeroDimensions_ShouldPersistMap() {
        testMap.setWidth(0);
        testMap.setHeight(0);

        Map savedMap = mapRepository.save(testMap);

        assertEquals(0, savedMap.getWidth());
        assertEquals(0, savedMap.getHeight());
    }

    @Test
    void save_WithNullImageData_ShouldPersistMap() {
        testMap.setImageData(null);

        Map savedMap = mapRepository.save(testMap);

        assertNull(savedMap.getImageData());
    }

    @Test
    void save_WithEmptyImageData_ShouldPersistMap() {
        testMap.setImageData("");

        Map savedMap = mapRepository.save(testMap);

        assertEquals("", savedMap.getImageData());
    }

    @Test
    void save_WithLongImageData_ShouldPersistMap() {
        StringBuilder longImageData = new StringBuilder("data:image/png;base64,");
        for (int i = 0; i < 10000; i++) {
            longImageData.append("A");
        }
        testMap.setImageData(longImageData.toString());

        Map savedMap = mapRepository.save(testMap);

        assertEquals(longImageData.toString(), savedMap.getImageData());
    }

    @Test
    void save_WithNullTimestamps_ShouldPersistMap() {
        testMap.setCreatedAt(null);
        testMap.setUpdatedAt(null);

        Map savedMap = mapRepository.save(testMap);

        assertNull(savedMap.getCreatedAt());
        assertNull(savedMap.getUpdatedAt());
    }

    @Test
    void save_WithCustomTimestamps_ShouldPersistMap() {
        LocalDateTime customTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        testMap.setCreatedAt(customTime);
        testMap.setUpdatedAt(customTime);

        Map savedMap = mapRepository.save(testMap);

        assertEquals(customTime, savedMap.getCreatedAt());
        assertEquals(customTime, savedMap.getUpdatedAt());
    }
} 