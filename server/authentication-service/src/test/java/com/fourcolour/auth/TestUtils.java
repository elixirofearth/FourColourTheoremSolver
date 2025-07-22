package com.fourcolour.auth;

import com.fourcolour.auth.entity.User;
import com.fourcolour.auth.entity.Session;
import com.fourcolour.common.dto.LoginRequest;
import com.fourcolour.common.dto.RegisterRequest;

import java.time.LocalDateTime;
import java.util.Map;

public class TestUtils {

    public static RegisterRequest createValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    public static LoginRequest createValidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    public static User createTestUser() {
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public static Session createTestSession(Integer userId, String token) {
        Session session = new Session();
        session.setId(1);
        session.setUserId(userId);
        session.setToken(token);
        session.setExpiresAt(LocalDateTime.now().plusHours(24));
        session.setCreatedAt(LocalDateTime.now());
        return session;
    }

    public static Map<String, Object> createValidUserData() {
        return Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "password", "password123"
        );
    }

    public static Map<String, Object> createValidLoginData() {
        return Map.of(
                "email", "test@example.com",
                "password", "password123"
        );
    }

    public static String createTestJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjE2MjM5MDIyLCJleHAiOjE2MTYzMjU0MjJ9.test";
    }

    public static String createExpiredJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjE2MjM5MDIyLCJleHAiOjE2MTYyMzkwMjJ9.expired";
    }
}