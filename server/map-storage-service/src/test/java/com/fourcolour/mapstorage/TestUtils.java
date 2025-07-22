package com.fourcolour.mapstorage;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.mapstorage.entity.Map;

import java.time.LocalDateTime;

public class TestUtils {

    public static MapRequest createValidMapRequest() {
        MapRequest request = new MapRequest();
        request.setUserId("user123");
        request.setName("Test Map");
        request.setWidth(10);
        request.setHeight(10);
        request.setImageData("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        request.setMatrix(new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}});
        return request;
    }

    public static MapRequest createMapRequestWithNullValues() {
        MapRequest request = new MapRequest();
        request.setUserId("user123");
        request.setName(null);
        request.setWidth(null);
        request.setHeight(null);
        request.setImageData(null);
        request.setMatrix(null);
        return request;
    }

    public static MapRequest createMapRequestWithEmptyValues() {
        MapRequest request = new MapRequest();
        request.setUserId("user123");
        request.setName("");
        request.setWidth(10);
        request.setHeight(10);
        request.setImageData("");
        request.setMatrix(new int[][]{});
        return request;
    }

    public static com.fourcolour.mapstorage.entity.Map createTestMap() {
        com.fourcolour.mapstorage.entity.Map map = new com.fourcolour.mapstorage.entity.Map();
        map.setId("507f1f77bcf86cd799439011");
        map.setUserId("user123");
        map.setName("Test Map");
        map.setWidth(10);
        map.setHeight(10);
        map.setImageData("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        map.setMatrix(new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}});
        map.setCreatedAt(LocalDateTime.now());
        map.setUpdatedAt(LocalDateTime.now());
        return map;
    }

    public static com.fourcolour.mapstorage.entity.Map createTestMapWithId(String id) {
        com.fourcolour.mapstorage.entity.Map map = createTestMap();
        map.setId(id);
        return map;
    }

    public static com.fourcolour.mapstorage.entity.Map createTestMapWithUserId(String userId) {
        com.fourcolour.mapstorage.entity.Map map = createTestMap();
        map.setUserId(userId);
        return map;
    }

    public static com.fourcolour.mapstorage.entity.Map createTestMapWithName(String name) {
        com.fourcolour.mapstorage.entity.Map map = createTestMap();
        map.setName(name);
        return map;
    }

    public static com.fourcolour.mapstorage.entity.Map createTestMapWithDimensions(int width, int height) {
        com.fourcolour.mapstorage.entity.Map map = createTestMap();
        map.setWidth(width);
        map.setHeight(height);
        return map;
    }

    public static com.fourcolour.mapstorage.entity.Map createTestMapWithMatrix(int[][] matrix) {
        com.fourcolour.mapstorage.entity.Map map = createTestMap();
        map.setMatrix(matrix);
        return map;
    }

    public static java.util.Map<String, Object> createValidMapData() {
        return java.util.Map.of(
                "userId", "user123",
                "name", "Test Map",
                "width", 10,
                "height", 10,
                "imageData", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
                "matrix", new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}}
        );
    }

    public static String createValidObjectId() {
        return "507f1f77bcf86cd799439011";
    }

    public static String createInvalidObjectId() {
        return "invalid-id";
    }

    public static String createShortObjectId() {
        return "123";
    }

    public static String createLongObjectId() {
        return "507f1f77bcf86cd799439011123456789";
    }

    public static String createObjectIdWithInvalidCharacters() {
        return "507f1f77bcf86cd79943901g";
    }
} 