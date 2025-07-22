package com.fourcolour.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

public class MapRequestTest {

    private MapRequest mapRequest;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        int[][] matrix = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        mapRequest = new MapRequest("user123", "Test Map", "image-data-string", matrix, 3, 3);
    }

    @Test
    void testDefaultConstructor() {
        MapRequest request = new MapRequest();
        assertNotNull(request);
        assertNull(request.getUserId());
        assertNull(request.getName());
        assertNull(request.getImageData());
        assertNull(request.getMatrix());
        assertNull(request.getWidth());
        assertNull(request.getHeight());
    }

    @Test
    void testParameterizedConstructor() {
        assertNotNull(mapRequest);
        assertEquals("user123", mapRequest.getUserId());
        assertEquals("Test Map", mapRequest.getName());
        assertEquals("image-data-string", mapRequest.getImageData());
        assertArrayEquals(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}, mapRequest.getMatrix());
        assertEquals(3, mapRequest.getWidth());
        assertEquals(3, mapRequest.getHeight());
    }

    @Test
    void testGetUserId() {
        assertEquals("user123", mapRequest.getUserId());
    }

    @Test
    void testSetUserId() {
        mapRequest.setUserId("newUser456");
        assertEquals("newUser456", mapRequest.getUserId());
    }

    @Test
    void testGetName() {
        assertEquals("Test Map", mapRequest.getName());
    }

    @Test
    void testSetName() {
        mapRequest.setName("New Map Name");
        assertEquals("New Map Name", mapRequest.getName());
    }

    @Test
    void testGetImageData() {
        assertEquals("image-data-string", mapRequest.getImageData());
    }

    @Test
    void testSetImageData() {
        mapRequest.setImageData("new-image-data");
        assertEquals("new-image-data", mapRequest.getImageData());
    }

    @Test
    void testGetMatrix() {
        assertArrayEquals(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}, mapRequest.getMatrix());
    }

    @Test
    void testSetMatrix() {
        int[][] newMatrix = {{10, 11, 12}, {13, 14, 15}};
        mapRequest.setMatrix(newMatrix);
        assertArrayEquals(newMatrix, mapRequest.getMatrix());
    }

    @Test
    void testGetWidth() {
        assertEquals(3, mapRequest.getWidth());
    }

    @Test
    void testSetWidth() {
        mapRequest.setWidth(5);
        assertEquals(5, mapRequest.getWidth());
    }

    @Test
    void testGetHeight() {
        assertEquals(3, mapRequest.getHeight());
    }

    @Test
    void testSetHeight() {
        mapRequest.setHeight(7);
        assertEquals(7, mapRequest.getHeight());
    }

    @Test
    void testMapRequestWithNullValues() {
        MapRequest request = new MapRequest(null, null, null, null, null, null);
        assertNull(request.getUserId());
        assertNull(request.getName());
        assertNull(request.getImageData());
        assertNull(request.getMatrix());
        assertNull(request.getWidth());
        assertNull(request.getHeight());
    }

    @Test
    void testMapRequestWithEmptyValues() {
        MapRequest request = new MapRequest("", "", "", new int[0][0], 0, 0);
        assertEquals("", request.getUserId());
        assertEquals("", request.getName());
        assertEquals("", request.getImageData());
        assertArrayEquals(new int[0][0], request.getMatrix());
        assertEquals(0, request.getWidth());
        assertEquals(0, request.getHeight());
    }

    @Test
    void testSetNullValues() {
        mapRequest.setUserId(null);
        mapRequest.setName(null);
        mapRequest.setImageData(null);
        mapRequest.setMatrix(null);
        mapRequest.setWidth(null);
        mapRequest.setHeight(null);
        
        assertNull(mapRequest.getUserId());
        assertNull(mapRequest.getName());
        assertNull(mapRequest.getImageData());
        assertNull(mapRequest.getMatrix());
        assertNull(mapRequest.getWidth());
        assertNull(mapRequest.getHeight());
    }

    @Test
    void testValidationAnnotations() throws Exception {
        // Test that the validation annotations are present
        NotBlank userIdNotBlankAnnotation = MapRequest.class.getDeclaredField("userId").getAnnotation(NotBlank.class);
        NotNull widthNotNullAnnotation = MapRequest.class.getDeclaredField("width").getAnnotation(NotNull.class);
        NotNull heightNotNullAnnotation = MapRequest.class.getDeclaredField("height").getAnnotation(NotNull.class);
        
        assertNotNull(userIdNotBlankAnnotation);
        assertEquals("User ID is required", userIdNotBlankAnnotation.message());
        
        assertNotNull(widthNotNullAnnotation);
        assertEquals("Width is required", widthNotNullAnnotation.message());
        
        assertNotNull(heightNotNullAnnotation);
        assertEquals("Height is required", heightNotNullAnnotation.message());
    }

    @Test
    void testJsonPropertyAnnotations() throws Exception {
        // Test that the JsonProperty annotations are present
        JsonProperty userIdJsonProperty = MapRequest.class.getDeclaredField("userId").getAnnotation(JsonProperty.class);
        JsonProperty imageDataJsonProperty = MapRequest.class.getDeclaredField("imageData").getAnnotation(JsonProperty.class);
        
        assertNotNull(userIdJsonProperty);
        assertEquals("userId", userIdJsonProperty.value());
        
        assertNotNull(imageDataJsonProperty);
        assertEquals("imageData", imageDataJsonProperty.value());
    }

    @Test
    void testValidationWithValidData() {
        MapRequest validRequest = new MapRequest("user123", "Test Map", "image-data", new int[][]{{1, 2}, {3, 4}}, 2, 2);
        var violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testValidationWithNullUserId() {
        MapRequest request = new MapRequest();
        request.setName("Test Map");
        request.setImageData("image-data");
        request.setMatrix(new int[][]{{1, 2}, {3, 4}});
        request.setWidth(2);
        request.setHeight(2);
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null userId should have violations");
    }

    @Test
    void testValidationWithEmptyUserId() {
        MapRequest request = new MapRequest("", "Test Map", "image-data", new int[][]{{1, 2}, {3, 4}}, 2, 2);
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with empty userId should have violations");
    }

    @Test
    void testValidationWithBlankUserId() {
        MapRequest request = new MapRequest("   ", "Test Map", "image-data", new int[][]{{1, 2}, {3, 4}}, 2, 2);
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with blank userId should have violations");
    }

    @Test
    void testValidationWithNullWidth() {
        MapRequest request = new MapRequest("user123", "Test Map", "image-data", new int[][]{{1, 2}, {3, 4}}, null, 2);
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null width should have violations");
    }

    @Test
    void testValidationWithNullHeight() {
        MapRequest request = new MapRequest("user123", "Test Map", "image-data", new int[][]{{1, 2}, {3, 4}}, 2, null);
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null height should have violations");
    }

    @Test
    void testMatrixOperations() {
        int[][] originalMatrix = {{1, 2, 3}, {4, 5, 6}};
        mapRequest.setMatrix(originalMatrix);
        
        // Test that the matrix is correctly stored
        assertArrayEquals(originalMatrix, mapRequest.getMatrix());
        
        // Test that modifying the original array affects the stored matrix (arrays are reference types)
        originalMatrix[0][0] = 999;
        assertEquals(999, mapRequest.getMatrix()[0][0]);
    }

    @Test
    void testMatrixWithNull() {
        mapRequest.setMatrix(null);
        assertNull(mapRequest.getMatrix());
    }

    @Test
    void testMatrixWithEmptyArray() {
        int[][] emptyMatrix = {};
        mapRequest.setMatrix(emptyMatrix);
        assertArrayEquals(emptyMatrix, mapRequest.getMatrix());
    }

    @Test
    void testMatrixWithJaggedArray() {
        int[][] jaggedMatrix = {{1, 2, 3}, {4, 5}};
        mapRequest.setMatrix(jaggedMatrix);
        assertArrayEquals(jaggedMatrix, mapRequest.getMatrix());
    }
} 