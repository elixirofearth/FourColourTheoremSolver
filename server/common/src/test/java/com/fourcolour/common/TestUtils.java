package com.fourcolour.common;

import com.fourcolour.common.dto.ColoringRequest;
import com.fourcolour.common.dto.LoginRequest;
import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.common.dto.RegisterRequest;
import com.fourcolour.common.dto.TokenResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for common test operations and data creation.
 */
public class TestUtils {

    // Test constants
    public static final String TEST_USER_ID = "test-user-123";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "test-password-123";
    public static final String TEST_NAME = "Test User";
    public static final String TEST_TOKEN = "test-jwt-token-123";
    public static final String TEST_EXPIRES_AT = "2024-12-31T23:59:59";
    public static final String TEST_SERVICE_NAME = "test-service";
    public static final String TEST_EVENT_TYPE = "TEST_EVENT";
    public static final String TEST_DESCRIPTION = "Test log description";

    /**
     * Creates a valid ColoringRequest for testing.
     */
    public static ColoringRequest createValidColoringRequest() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        ColoringRequest.ImageData imageData = new ColoringRequest.ImageData(data);
        return new ColoringRequest(imageData, 3, 3, TEST_USER_ID);
    }

    /**
     * Creates a valid LoginRequest for testing.
     */
    public static LoginRequest createValidLoginRequest() {
        return new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
    }

    /**
     * Creates a valid MapRequest for testing.
     */
    public static MapRequest createValidMapRequest() {
        int[][] matrix = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        return new MapRequest(TEST_USER_ID, "Test Map", "image-data-string", matrix, 3, 3);
    }

    /**
     * Creates a valid RegisterRequest for testing.
     */
    public static RegisterRequest createValidRegisterRequest() {
        return new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
    }

    /**
     * Creates a valid TokenResponse for testing.
     */
    public static TokenResponse createValidTokenResponse() {
        return new TokenResponse(TEST_TOKEN, TEST_NAME, 123, TEST_EMAIL, TEST_EXPIRES_AT);
    }

    /**
     * Creates test metadata for logging tests.
     */
    public static Map<String, String> createTestMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        metadata.put("test_key", "test_value");
        return metadata;
    }

    /**
     * Creates a test matrix for map operations.
     */
    public static int[][] createTestMatrix() {
        return new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
    }

    /**
     * Creates test image data for coloring operations.
     */
    public static int[] createTestImageData() {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    }

    /**
     * Creates an invalid email for validation testing.
     */
    public static String createInvalidEmail() {
        return "invalid-email-format";
    }

    /**
     * Creates a valid email for validation testing.
     */
    public static String createValidEmail() {
        return "valid@example.com";
    }

    /**
     * Creates a blank string for validation testing.
     */
    public static String createBlankString() {
        return "   ";
    }

    /**
     * Creates an empty string for validation testing.
     */
    public static String createEmptyString() {
        return "";
    }

    /**
     * Creates a null value for null testing.
     */
    public static Object createNullValue() {
        return null;
    }

    /**
     * Creates a long string for boundary testing.
     */
    public static String createLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        return sb.toString();
    }

    /**
     * Creates a string with special characters for testing.
     */
    public static String createSpecialCharacterString() {
        return "test-string-with-special-chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
    }

    /**
     * Creates a string with unicode characters for testing.
     */
    public static String createUnicodeString() {
        return "test-string-with-unicode: 你好世界";
    }

    /**
     * Creates a large metadata map for testing.
     */
    public static Map<String, String> createLargeMetadata() {
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            metadata.put("key" + i, "value" + i);
        }
        return metadata;
    }

    /**
     * Creates metadata with null values for testing.
     */
    public static Map<String, String> createMetadataWithNullValues() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", null);
        metadata.put("key2", "value2");
        metadata.put("key3", null);
        return metadata;
    }

    /**
     * Creates metadata with empty values for testing.
     */
    public static Map<String, String> createMetadataWithEmptyValues() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "");
        metadata.put("key2", "value2");
        metadata.put("key3", "");
        return metadata;
    }

    /**
     * Creates metadata with special characters for testing.
     */
    public static Map<String, String> createMetadataWithSpecialCharacters() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key with spaces", "value with spaces");
        metadata.put("key-with-special-chars", "value!@#$%^&*()");
        metadata.put("unicode-key", "unicode-value-你好");
        return metadata;
    }
} 