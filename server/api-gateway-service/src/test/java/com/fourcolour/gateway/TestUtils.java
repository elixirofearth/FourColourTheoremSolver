package com.fourcolour.gateway;

import com.fourcolour.common.dto.ColoringRequest;

import java.util.Map;

public class TestUtils {

    public static ColoringRequest createValidColoringRequest() {
        ColoringRequest request = new ColoringRequest();
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData();
        imageData.setData(new int[]{1, 2, 3, 4}); // Sample pixel data
        request.setImage(imageData);
        request.setWidth(800);
        request.setHeight(600);
        request.setUserId("test-user-123");
        return request;
    }

    public static Map<String, Object> createValidMapData() {
        return Map.of(
                "name", "Test Map",
                "description", "A test map for unit testing",
                "userId", "test-user-123",
                "coloredImage", "base64-colored-image-data"
        );
    }

    public static Map<String, Object> createValidUserRegistration() {
        return Map.of(
                "name", "testuser",
                "email", "test@example.com",
                "password", "password123"
        );
    }

    public static Map<String, Object> createValidUserLogin() {
        return Map.of(
                "email", "test@example.com",
                "password", "password123"
        );
    }
}
