package com.fourcolour.mapstorage.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MapTest {

    private Map map;

    @BeforeEach
    void setUp() {
        map = new Map();
    }

    @Test
    void defaultConstructor_ShouldCreateMapWithNullValues() {
        assertNull(map.getId());
        assertNull(map.getUserId());
        assertNull(map.getName());
        assertNull(map.getWidth());
        assertNull(map.getHeight());
        assertNull(map.getImageData());
        assertNull(map.getMatrix());
        assertNotNull(map.getCreatedAt());
        assertNotNull(map.getUpdatedAt());
    }

    @Test
    void parameterizedConstructor_ShouldSetValues() {
        String userId = "user123";
        String name = "Test Map";
        Integer width = 10;
        Integer height = 10;
        String imageData = "data:image/png;base64,test";
        int[][] matrix = {{0, 1}, {1, 0}};

        Map paramMap = new Map(userId, name, width, height, imageData, matrix);

        assertEquals(userId, paramMap.getUserId());
        assertEquals(name, paramMap.getName());
        assertEquals(width, paramMap.getWidth());
        assertEquals(height, paramMap.getHeight());
        assertEquals(imageData, paramMap.getImageData());
        assertArrayEquals(matrix, paramMap.getMatrix());
        assertNull(paramMap.getId()); // ID should still be null as it's auto-generated
        assertNotNull(paramMap.getCreatedAt()); // CreatedAt is set by constructor
        assertNotNull(paramMap.getUpdatedAt()); // UpdatedAt is set by constructor
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        String id = "507f1f77bcf86cd799439011";
        String userId = "user123";
        String name = "Test Map";
        Integer width = 10;
        Integer height = 10;
        String imageData = "data:image/png;base64,test";
        int[][] matrix = {{0, 1}, {1, 0}};
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        map.setId(id);
        map.setUserId(userId);
        map.setName(name);
        map.setWidth(width);
        map.setHeight(height);
        map.setImageData(imageData);
        map.setMatrix(matrix);
        map.setCreatedAt(createdAt);
        map.setUpdatedAt(updatedAt);

        assertEquals(id, map.getId());
        assertEquals(userId, map.getUserId());
        assertEquals(name, map.getName());
        assertEquals(width, map.getWidth());
        assertEquals(height, map.getHeight());
        assertEquals(imageData, map.getImageData());
        assertArrayEquals(matrix, map.getMatrix());
        assertEquals(createdAt, map.getCreatedAt());
        assertEquals(updatedAt, map.getUpdatedAt());
    }

    @Test
    void setId_WithValidId_ShouldSetId() {
        String validId = "507f1f77bcf86cd799439011";
        
        map.setId(validId);
        
        assertEquals(validId, map.getId());
    }

    @Test
    void setId_WithNullId_ShouldSetNull() {
        map.setId(null);
        
        assertNull(map.getId());
    }

    @Test
    void setUserId_WithValidUserId_ShouldSetUserId() {
        String validUserId = "user123";
        
        map.setUserId(validUserId);
        
        assertEquals(validUserId, map.getUserId());
    }

    @Test
    void setUserId_WithNullUserId_ShouldSetNull() {
        map.setUserId(null);
        
        assertNull(map.getUserId());
    }

    @Test
    void setUserId_WithEmptyUserId_ShouldSetEmptyString() {
        map.setUserId("");
        
        assertEquals("", map.getUserId());
    }

    @Test
    void setName_WithValidName_ShouldSetName() {
        String validName = "Test Map";
        
        map.setName(validName);
        
        assertEquals(validName, map.getName());
    }

    @Test
    void setName_WithNullName_ShouldSetNull() {
        map.setName(null);
        
        assertNull(map.getName());
    }

    @Test
    void setName_WithEmptyName_ShouldSetEmptyString() {
        map.setName("");
        
        assertEquals("", map.getName());
    }

    @Test
    void setWidth_WithPositiveWidth_ShouldSetWidth() {
        Integer width = 10;
        
        map.setWidth(width);
        
        assertEquals(width, map.getWidth());
    }

    @Test
    void setWidth_WithZeroWidth_ShouldSetZero() {
        map.setWidth(0);
        
        assertEquals(0, map.getWidth());
    }

    @Test
    void setWidth_WithNegativeWidth_ShouldSetNegative() {
        map.setWidth(-10);
        
        assertEquals(-10, map.getWidth());
    }

    @Test
    void setWidth_WithNullWidth_ShouldSetNull() {
        map.setWidth(null);
        
        assertNull(map.getWidth());
    }

    @Test
    void setHeight_WithPositiveHeight_ShouldSetHeight() {
        Integer height = 10;
        
        map.setHeight(height);
        
        assertEquals(height, map.getHeight());
    }

    @Test
    void setHeight_WithZeroHeight_ShouldSetZero() {
        map.setHeight(0);
        
        assertEquals(0, map.getHeight());
    }

    @Test
    void setHeight_WithNegativeHeight_ShouldSetNegative() {
        map.setHeight(-10);
        
        assertEquals(-10, map.getHeight());
    }

    @Test
    void setHeight_WithNullHeight_ShouldSetNull() {
        map.setHeight(null);
        
        assertNull(map.getHeight());
    }

    @Test
    void setImageData_WithValidImageData_ShouldSetImageData() {
        String validImageData = "data:image/png;base64,test";
        
        map.setImageData(validImageData);
        
        assertEquals(validImageData, map.getImageData());
    }

    @Test
    void setImageData_WithNullImageData_ShouldSetNull() {
        map.setImageData(null);
        
        assertNull(map.getImageData());
    }

    @Test
    void setImageData_WithEmptyImageData_ShouldSetEmptyString() {
        map.setImageData("");
        
        assertEquals("", map.getImageData());
    }

    @Test
    void setMatrix_WithValidMatrix_ShouldSetMatrix() {
        int[][] matrix = {{0, 1}, {1, 0}};
        
        map.setMatrix(matrix);
        
        assertArrayEquals(matrix, map.getMatrix());
    }

    @Test
    void setMatrix_WithNullMatrix_ShouldSetNull() {
        map.setMatrix(null);
        
        assertNull(map.getMatrix());
    }

    @Test
    void setMatrix_WithEmptyMatrix_ShouldSetEmptyMatrix() {
        int[][] matrix = {};
        
        map.setMatrix(matrix);
        
        assertArrayEquals(matrix, map.getMatrix());
    }

    @Test
    void setCreatedAt_WithValidDateTime_ShouldSetDateTime() {
        LocalDateTime createdAt = LocalDateTime.now();
        
        map.setCreatedAt(createdAt);
        
        assertEquals(createdAt, map.getCreatedAt());
    }

    @Test
    void setCreatedAt_WithNullDateTime_ShouldSetNull() {
        map.setCreatedAt(null);
        
        assertNull(map.getCreatedAt());
    }

    @Test
    void setUpdatedAt_WithValidDateTime_ShouldSetDateTime() {
        LocalDateTime updatedAt = LocalDateTime.now();
        
        map.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, map.getUpdatedAt());
    }

    @Test
    void setUpdatedAt_WithNullDateTime_ShouldSetNull() {
        map.setUpdatedAt(null);
        
        assertNull(map.getUpdatedAt());
    }

    @Test
    void updateTimestamp_ShouldUpdateUpdatedAt() {
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusHours(1);
        map.setUpdatedAt(originalUpdatedAt);
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        map.updateTimestamp();
        
        assertTrue(map.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void mapWithAllFields_ShouldMaintainAllValues() {
        String id = "507f1f77bcf86cd799439011";
        String userId = "user123";
        String name = "Test Map";
        Integer width = 10;
        Integer height = 10;
        String imageData = "data:image/png;base64,test";
        int[][] matrix = {{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        map.setId(id);
        map.setUserId(userId);
        map.setName(name);
        map.setWidth(width);
        map.setHeight(height);
        map.setImageData(imageData);
        map.setMatrix(matrix);
        map.setCreatedAt(createdAt);
        map.setUpdatedAt(updatedAt);

        assertEquals(id, map.getId());
        assertEquals(userId, map.getUserId());
        assertEquals(name, map.getName());
        assertEquals(width, map.getWidth());
        assertEquals(height, map.getHeight());
        assertEquals(imageData, map.getImageData());
        assertArrayEquals(matrix, map.getMatrix());
        assertEquals(createdAt, map.getCreatedAt());
        assertEquals(updatedAt, map.getUpdatedAt());
    }

    @Test
    void mapWithMinimalFields_ShouldWorkCorrectly() {
        String userId = "user123";
        String name = "Test Map";

        map.setUserId(userId);
        map.setName(name);

        assertEquals(userId, map.getUserId());
        assertEquals(name, map.getName());
        assertNull(map.getId());
        assertNull(map.getWidth());
        assertNull(map.getHeight());
        assertNull(map.getImageData());
        assertNull(map.getMatrix());
        assertNotNull(map.getCreatedAt());
        assertNotNull(map.getUpdatedAt());
    }

    @Test
    void parameterizedConstructor_WithNullValues_ShouldSetNulls() {
        Map paramMap = new Map(null, null, null, null, null, null);

        assertNull(paramMap.getUserId());
        assertNull(paramMap.getName());
        assertNull(paramMap.getWidth());
        assertNull(paramMap.getHeight());
        assertNull(paramMap.getImageData());
        assertNull(paramMap.getMatrix());
        assertNotNull(paramMap.getCreatedAt());
        assertNotNull(paramMap.getUpdatedAt());
    }

    @Test
    void parameterizedConstructor_WithEmptyStrings_ShouldSetEmptyStrings() {
        Map paramMap = new Map("", "", 0, 0, "", new int[][]{});

        assertEquals("", paramMap.getUserId());
        assertEquals("", paramMap.getName());
        assertEquals(0, paramMap.getWidth());
        assertEquals(0, paramMap.getHeight());
        assertEquals("", paramMap.getImageData());
        assertArrayEquals(new int[][]{}, paramMap.getMatrix());
    }

    @Test
    void setUserId_WithSpecialCharacters_ShouldSetUserId() {
        String specialUserId = "user@123!#$%";
        
        map.setUserId(specialUserId);
        
        assertEquals(specialUserId, map.getUserId());
    }

    @Test
    void setName_WithSpecialCharacters_ShouldSetName() {
        String specialName = "Test Map @#$%^&*()";
        
        map.setName(specialName);
        
        assertEquals(specialName, map.getName());
    }

    @Test
    void setImageData_WithLongImageData_ShouldSetImageData() {
        StringBuilder longImageData = new StringBuilder("data:image/png;base64,");
        for (int i = 0; i < 1000; i++) {
            longImageData.append("A");
        }
        
        map.setImageData(longImageData.toString());
        
        assertEquals(longImageData.toString(), map.getImageData());
    }

    @Test
    void setUserId_WithLongUserId_ShouldSetUserId() {
        StringBuilder longUserId = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longUserId.append("a");
        }
        
        map.setUserId(longUserId.toString());
        
        assertEquals(longUserId.toString(), map.getUserId());
    }

    @Test
    void setName_WithLongName_ShouldSetName() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("a");
        }
        
        map.setName(longName.toString());
        
        assertEquals(longName.toString(), map.getName());
    }

    @Test
    void setMatrix_WithLargeMatrix_ShouldSetMatrix() {
        int[][] largeMatrix = new int[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                largeMatrix[i][j] = i + j;
            }
        }
        
        map.setMatrix(largeMatrix);
        
        assertArrayEquals(largeMatrix, map.getMatrix());
    }

    @Test
    void mapEquality_SameValues_ShouldBeEqual() {
        Map map1 = new Map();
        Map map2 = new Map();

        map1.setId("507f1f77bcf86cd799439011");
        map1.setUserId("user123");
        map1.setName("Test Map");
        map1.setWidth(10);
        map1.setHeight(10);
        map1.setImageData("data:image/png;base64,test");
        map1.setMatrix(new int[][]{{0, 1}, {1, 0}});

        map2.setId("507f1f77bcf86cd799439011");
        map2.setUserId("user123");
        map2.setName("Test Map");
        map2.setWidth(10);
        map2.setHeight(10);
        map2.setImageData("data:image/png;base64,test");
        map2.setMatrix(new int[][]{{0, 1}, {1, 0}});

        assertEquals(map1.getId(), map2.getId());
        assertEquals(map1.getUserId(), map2.getUserId());
        assertEquals(map1.getName(), map2.getName());
        assertEquals(map1.getWidth(), map2.getWidth());
        assertEquals(map1.getHeight(), map2.getHeight());
        assertEquals(map1.getImageData(), map2.getImageData());
        assertArrayEquals(map1.getMatrix(), map2.getMatrix());
    }

    @Test
    void toString_ShouldNotExposeSensitiveData() {
        map.setId("507f1f77bcf86cd799439011");
        map.setUserId("user123");
        map.setName("Test Map");
        map.setImageData("sensitive-data");

        String toString = map.toString();

        // toString should not expose sensitive image data
        assertFalse(toString.contains("sensitive-data"));
    }
} 