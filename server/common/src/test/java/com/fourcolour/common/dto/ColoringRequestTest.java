package com.fourcolour.common.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ColoringRequestTest {

    private ColoringRequest coloringRequest;
    private ColoringRequest.ImageData imageData;

    @BeforeEach
    void setUp() {
        int[] data = {1, 2, 3, 4, 5};
        imageData = new ColoringRequest.ImageData(data);
        coloringRequest = new ColoringRequest(imageData, 10, 20, "user123");
    }

    @Test
    void testDefaultConstructor() {
        ColoringRequest request = new ColoringRequest();
        assertNotNull(request);
        assertNull(request.getImage());
        assertEquals(0, request.getWidth());
        assertEquals(0, request.getHeight());
        assertNull(request.getUserId());
    }

    @Test
    void testParameterizedConstructor() {
        assertNotNull(coloringRequest);
        assertEquals(imageData, coloringRequest.getImage());
        assertEquals(10, coloringRequest.getWidth());
        assertEquals(20, coloringRequest.getHeight());
        assertEquals("user123", coloringRequest.getUserId());
    }

    @Test
    void testGetUserId() {
        assertEquals("user123", coloringRequest.getUserId());
    }

    @Test
    void testSetUserId() {
        coloringRequest.setUserId("newUser456");
        assertEquals("newUser456", coloringRequest.getUserId());
    }

    @Test
    void testGetImage() {
        assertEquals(imageData, coloringRequest.getImage());
    }

    @Test
    void testSetImage() {
        int[] newData = {6, 7, 8, 9, 10};
        ColoringRequest.ImageData newImageData = new ColoringRequest.ImageData(newData);
        coloringRequest.setImage(newImageData);
        assertEquals(newImageData, coloringRequest.getImage());
    }

    @Test
    void testGetWidth() {
        assertEquals(10, coloringRequest.getWidth());
    }

    @Test
    void testSetWidth() {
        coloringRequest.setWidth(15);
        assertEquals(15, coloringRequest.getWidth());
    }

    @Test
    void testGetHeight() {
        assertEquals(20, coloringRequest.getHeight());
    }

    @Test
    void testSetHeight() {
        coloringRequest.setHeight(25);
        assertEquals(25, coloringRequest.getHeight());
    }

    @Test
    void testImageDataDefaultConstructor() {
        ColoringRequest.ImageData data = new ColoringRequest.ImageData();
        assertNotNull(data);
        assertNull(data.getData());
    }

    @Test
    void testImageDataParameterizedConstructor() {
        int[] data = {1, 2, 3, 4, 5};
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData(data);
        assertNotNull(imageData);
        assertArrayEquals(data, imageData.getData());
    }

    @Test
    void testImageDataGetData() {
        int[] data = {1, 2, 3, 4, 5};
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData(data);
        assertArrayEquals(data, imageData.getData());
    }

    @Test
    void testImageDataSetData() {
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData();
        int[] newData = {6, 7, 8, 9, 10};
        imageData.setData(newData);
        assertArrayEquals(newData, imageData.getData());
    }

    @Test
    void testImageDataWithNullData() {
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData(null);
        assertNull(imageData.getData());
    }

    @Test
    void testImageDataSetNullData() {
        int[] data = {1, 2, 3, 4, 5};
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData(data);
        imageData.setData(null);
        assertNull(imageData.getData());
    }

    @Test
    void testColoringRequestWithNullValues() {
        ColoringRequest request = new ColoringRequest(null, 0, 0, null);
        assertNull(request.getImage());
        assertEquals(0, request.getWidth());
        assertEquals(0, request.getHeight());
        assertNull(request.getUserId());
    }

    @Test
    void testColoringRequestWithNegativeDimensions() {
        ColoringRequest request = new ColoringRequest(imageData, -5, -10, "user123");
        assertEquals(-5, request.getWidth());
        assertEquals(-10, request.getHeight());
    }
} 