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

public class RegisterRequestTest {

    private RegisterRequest registerRequest;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "password123", "John Doe");
    }

    @Test
    void testDefaultConstructor() {
        RegisterRequest request = new RegisterRequest();
        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getName());
    }

    @Test
    void testParameterizedConstructor() {
        assertNotNull(registerRequest);
        assertEquals("test@example.com", registerRequest.getEmail());
        assertEquals("password123", registerRequest.getPassword());
        assertEquals("John Doe", registerRequest.getName());
    }

    @Test
    void testGetEmail() {
        assertEquals("test@example.com", registerRequest.getEmail());
    }

    @Test
    void testSetEmail() {
        registerRequest.setEmail("new@example.com");
        assertEquals("new@example.com", registerRequest.getEmail());
    }

    @Test
    void testGetPassword() {
        assertEquals("password123", registerRequest.getPassword());
    }

    @Test
    void testSetPassword() {
        registerRequest.setPassword("newpassword456");
        assertEquals("newpassword456", registerRequest.getPassword());
    }

    @Test
    void testGetName() {
        assertEquals("John Doe", registerRequest.getName());
    }

    @Test
    void testSetName() {
        registerRequest.setName("Jane Smith");
        assertEquals("Jane Smith", registerRequest.getName());
    }

    @Test
    void testRegisterRequestWithNullValues() {
        RegisterRequest request = new RegisterRequest(null, null, null);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getName());
    }

    @Test
    void testRegisterRequestWithEmptyValues() {
        RegisterRequest request = new RegisterRequest("", "", "");
        assertEquals("", request.getEmail());
        assertEquals("", request.getPassword());
        assertEquals("", request.getName());
    }

    @Test
    void testSetNullValues() {
        registerRequest.setEmail(null);
        registerRequest.setPassword(null);
        registerRequest.setName(null);
        
        assertNull(registerRequest.getEmail());
        assertNull(registerRequest.getPassword());
        assertNull(registerRequest.getName());
    }

    @Test
    void testValidationAnnotations() throws Exception {
        // Test that the validation annotations are present
        NotBlank emailNotBlankAnnotation = RegisterRequest.class.getDeclaredField("email").getAnnotation(NotBlank.class);
        Email emailAnnotation = RegisterRequest.class.getDeclaredField("email").getAnnotation(Email.class);
        NotBlank passwordNotBlankAnnotation = RegisterRequest.class.getDeclaredField("password").getAnnotation(NotBlank.class);
        
        assertNotNull(emailNotBlankAnnotation);
        assertEquals("Email is required", emailNotBlankAnnotation.message());
        
        assertNotNull(emailAnnotation);
        assertEquals("Email should be valid", emailAnnotation.message());
        
        assertNotNull(passwordNotBlankAnnotation);
        assertEquals("Password is required", passwordNotBlankAnnotation.message());
    }

    @Test
    void testValidationWithValidData() {
        RegisterRequest validRequest = new RegisterRequest("valid@example.com", "password123", "John Doe");
        var violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testValidationWithNullEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setName("John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null email should have violations");
    }

    @Test
    void testValidationWithEmptyEmail() {
        RegisterRequest request = new RegisterRequest("", "password123", "John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with empty email should have violations");
    }

    @Test
    void testValidationWithInvalidEmail() {
        RegisterRequest request = new RegisterRequest("invalid-email", "password123", "John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with invalid email should have violations");
    }

    @Test
    void testValidationWithNullPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setName("John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with null password should have violations");
    }

    @Test
    void testValidationWithEmptyPassword() {
        RegisterRequest request = new RegisterRequest("test@example.com", "", "John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with empty password should have violations");
    }

    @Test
    void testValidationWithBlankEmail() {
        RegisterRequest request = new RegisterRequest("   ", "password123", "John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with blank email should have violations");
    }

    @Test
    void testValidationWithBlankPassword() {
        RegisterRequest request = new RegisterRequest("test@example.com", "   ", "John Doe");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with blank password should have violations");
    }

    @Test
    void testValidationWithValidEmailFormats() {
        // Test various valid email formats
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@example.com",
            "user@subdomain.example.com"
        };

        for (String email : validEmails) {
            RegisterRequest request = new RegisterRequest(email, "password123", "John Doe");
            var violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Valid email format should pass validation: " + email);
        }
    }

    @Test
    void testValidationWithInvalidEmailFormats() {
        // Test various invalid email formats
        String[] invalidEmails = {
            "invalid-email",
            "@example.com",
            "user@",
            "user@.com",
            "user..name@example.com",
            "user@example..com"
        };

        for (String email : invalidEmails) {
            RegisterRequest request = new RegisterRequest(email, "password123", "John Doe");
            var violations = validator.validate(request);
            assertFalse(violations.isEmpty(), "Invalid email format should fail validation: " + email);
        }
    }

    @Test
    void testNameFieldIsOptional() {
        // The name field doesn't have validation annotations, so it should be optional
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null);
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Request with null name should have no violations");
    }

    @Test
    void testNameFieldWithEmptyString() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "");
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Request with empty name should have no violations");
    }

    @Test
    void testNameFieldWithWhitespace() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "   ");
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Request with whitespace name should have no violations");
    }
} 