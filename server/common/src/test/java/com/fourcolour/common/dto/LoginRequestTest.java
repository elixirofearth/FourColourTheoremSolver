package com.fourcolour.common.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    private LoginRequest loginRequest;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    void testDefaultConstructor() {
        LoginRequest request = new LoginRequest();
        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testParameterizedConstructor() {
        assertNotNull(loginRequest);
        assertEquals("test@example.com", loginRequest.getEmail());
        assertEquals("password123", loginRequest.getPassword());
    }

    @Test
    void testGetEmail() {
        assertEquals("test@example.com", loginRequest.getEmail());
    }

    @Test
    void testSetEmail() {
        loginRequest.setEmail("new@example.com");
        assertEquals("new@example.com", loginRequest.getEmail());
    }

    @Test
    void testGetPassword() {
        assertEquals("password123", loginRequest.getPassword());
    }

    @Test
    void testSetPassword() {
        loginRequest.setPassword("newpassword456");
        assertEquals("newpassword456", loginRequest.getPassword());
    }

    @Test
    void testLoginRequestWithNullValues() {
        LoginRequest request = new LoginRequest(null, null);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testLoginRequestWithEmptyValues() {
        LoginRequest request = new LoginRequest("", "");
        assertEquals("", request.getEmail());
        assertEquals("", request.getPassword());
    }

    @Test
    void testSetNullValues() {
        loginRequest.setEmail(null);
        loginRequest.setPassword(null);
        
        assertNull(loginRequest.getEmail());
        assertNull(loginRequest.getPassword());
    }

    @Test
    void testValidationAnnotations() throws Exception {
        // Test that the validation annotations are present
        NotBlank emailNotBlankAnnotation = LoginRequest.class.getDeclaredField("email").getAnnotation(NotBlank.class);
        Email emailAnnotation = LoginRequest.class.getDeclaredField("email").getAnnotation(Email.class);
        NotBlank passwordNotBlankAnnotation = LoginRequest.class.getDeclaredField("password").getAnnotation(NotBlank.class);
        
        assertNotNull(emailNotBlankAnnotation);
        assertEquals("Email is required", emailNotBlankAnnotation.message());
        
        assertNotNull(emailAnnotation);
        assertEquals("Email should be valid", emailAnnotation.message());
        
        assertNotNull(passwordNotBlankAnnotation);
        assertEquals("Password is required", passwordNotBlankAnnotation.message());
    }

    @Test
    void testValidationWithValidData() {
        LoginRequest validRequest = new LoginRequest("valid@example.com", "password123");
        var violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testValidationWithNullEmail() {
        LoginRequest request = new LoginRequest();
        request.setPassword("password123");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null email should have violations");
    }

    @Test
    void testValidationWithEmptyEmail() {
        LoginRequest request = new LoginRequest("", "password123");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with empty email should have violations");
    }

    @Test
    void testValidationWithInvalidEmail() {
        LoginRequest request = new LoginRequest("invalid-email", "password123");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with invalid email should have violations");
    }

    @Test
    void testValidationWithNullPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null password should have violations");
    }

    @Test
    void testValidationWithEmptyPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with empty password should have violations");
    }

    @Test
    void testValidationWithBlankEmail() {
        LoginRequest request = new LoginRequest("   ", "password123");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with blank email should have violations");
    }

    @Test
    void testValidationWithBlankPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "   ");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with blank password should have violations");
    }
} 