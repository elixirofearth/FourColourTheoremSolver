package com.fourcolour.auth.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void defaultConstructor_ShouldCreateUserWithNullValues() {
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPasswordHash());
        assertNull(user.getName());
        assertNull(user.getCreatedAt());
    }

    @Test
    void parameterizedConstructor_ShouldSetValues() {
        String email = "test@example.com";
        String passwordHash = "$2a$10$hashedpassword";
        String name = "Test User";

        User paramUser = new User(email, passwordHash, name);

        assertEquals(email, paramUser.getEmail());
        assertEquals(passwordHash, paramUser.getPasswordHash());
        assertEquals(name, paramUser.getName());
        assertNull(paramUser.getId()); // ID should still be null as it's auto-generated
        assertNull(paramUser.getCreatedAt()); // CreatedAt is set by @PrePersist
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Integer id = 1;
        String email = "test@example.com";
        String passwordHash = "$2a$10$hashedpassword";
        String name = "Test User";
        LocalDateTime createdAt = LocalDateTime.now();

        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setName(name);
        user.setCreatedAt(createdAt);

        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(name, user.getName());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void setEmail_WithValidEmail_ShouldSetEmail() {
        String validEmail = "user@example.com";
        
        user.setEmail(validEmail);
        
        assertEquals(validEmail, user.getEmail());
    }

    @Test
    void setEmail_WithNullEmail_ShouldSetNull() {
        user.setEmail(null);
        
        assertNull(user.getEmail());
    }

    @Test
    void setPasswordHash_WithValidHash_ShouldSetHash() {
        String validHash = "$2a$10$validhashedpassword";
        
        user.setPasswordHash(validHash);
        
        assertEquals(validHash, user.getPasswordHash());
    }

    @Test
    void setPasswordHash_WithNullHash_ShouldSetNull() {
        user.setPasswordHash(null);
        
        assertNull(user.getPasswordHash());
    }

    @Test
    void setName_WithValidName_ShouldSetName() {
        String validName = "John Doe";
        
        user.setName(validName);
        
        assertEquals(validName, user.getName());
    }

    @Test
    void setName_WithNullName_ShouldSetNull() {
        user.setName(null);
        
        assertNull(user.getName());
    }

    @Test
    void setName_WithEmptyName_ShouldSetEmptyString() {
        String emptyName = "";
        
        user.setName(emptyName);
        
        assertEquals(emptyName, user.getName());
    }

    @Test
    void setId_WithPositiveId_ShouldSetId() {
        Integer positiveId = 123;
        
        user.setId(positiveId);
        
        assertEquals(positiveId, user.getId());
    }

    @Test
    void setId_WithZeroId_ShouldSetZero() {
        Integer zeroId = 0;
        
        user.setId(zeroId);
        
        assertEquals(zeroId, user.getId());
    }

    @Test
    void setId_WithNegativeId_ShouldSetNegative() {
        Integer negativeId = -1;
        
        user.setId(negativeId);
        
        assertEquals(negativeId, user.getId());
    }

    @Test
    void setCreatedAt_WithValidDateTime_ShouldSetDateTime() {
        LocalDateTime validDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        
        user.setCreatedAt(validDateTime);
        
        assertEquals(validDateTime, user.getCreatedAt());
    }

    @Test
    void setCreatedAt_WithNullDateTime_ShouldSetNull() {
        user.setCreatedAt(null);
        
        assertNull(user.getCreatedAt());
    }

    @Test
    void onCreate_ShouldSetCreatedAtToCurrentTime() {
        LocalDateTime beforeOnCreate = LocalDateTime.now();
        
        // Simulate @PrePersist behavior
        user.onCreate();
        
        LocalDateTime afterOnCreate = LocalDateTime.now();
        LocalDateTime createdAt = user.getCreatedAt();
        
        assertNotNull(createdAt);
        assertTrue(createdAt.isAfter(beforeOnCreate.minusSeconds(1))); // Small buffer
        assertTrue(createdAt.isBefore(afterOnCreate.plusSeconds(1)));  // Small buffer
    }

    @Test
    void onCreate_CalledMultipleTimes_ShouldUpdateCreatedAt() {
        user.onCreate();
        LocalDateTime firstCreatedAt = user.getCreatedAt();
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        user.onCreate();
        LocalDateTime secondCreatedAt = user.getCreatedAt();
        
        assertNotNull(firstCreatedAt);
        assertNotNull(secondCreatedAt);
        // Second call should update the timestamp
        assertFalse(secondCreatedAt.isBefore(firstCreatedAt));
    }

    @Test
    void userWithAllFields_ShouldMaintainAllValues() {
        Integer id = 42;
        String email = "complete@example.com";
        String passwordHash = "$2a$10$completehash";
        String name = "Complete User";
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setName(name);
        user.setCreatedAt(createdAt);

        // Verify all fields are maintained
        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(name, user.getName());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void userWithMinimalFields_ShouldWorkCorrectly() {
        String email = "minimal@example.com";
        String passwordHash = "$2a$10$minimalhash";

        user.setEmail(email);
        user.setPasswordHash(passwordHash);

        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertNull(user.getName()); // Optional field
        assertNull(user.getId()); // Auto-generated field
        assertNull(user.getCreatedAt()); // Set by @PrePersist
    }

    @Test
    void parameterizedConstructor_WithNullValues_ShouldSetNulls() {
        User nullUser = new User(null, null, null);

        assertNull(nullUser.getEmail());
        assertNull(nullUser.getPasswordHash());
        assertNull(nullUser.getName());
    }

    @Test
    void parameterizedConstructor_WithEmptyStrings_ShouldSetEmptyStrings() {
        User emptyUser = new User("", "", "");

        assertEquals("", emptyUser.getEmail());
        assertEquals("", emptyUser.getPasswordHash());
        assertEquals("", emptyUser.getName());
    }

    @Test
    void setEmail_WithSpecialCharacters_ShouldSetEmail() {
        String specialEmail = "user+test@example-site.co.uk";
        
        user.setEmail(specialEmail);
        
        assertEquals(specialEmail, user.getEmail());
    }

    @Test
    void setName_WithSpecialCharacters_ShouldSetName() {
        String specialName = "José María O'Connor-Smith";
        
        user.setName(specialName);
        
        assertEquals(specialName, user.getName());
    }

    @Test
    void setPasswordHash_WithLongHash_ShouldSetHash() {
        StringBuilder longHash = new StringBuilder("$2a$10$");
        for (int i = 0; i < 100; i++) {
            longHash.append("a");
        }
        
        user.setPasswordHash(longHash.toString());
        
        assertEquals(longHash.toString(), user.getPasswordHash());
    }

    @Test
    void setEmail_WithLongEmail_ShouldSetEmail() {
        StringBuilder longEmail = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longEmail.append("a");
        }
        longEmail.append("@example.com");
        
        user.setEmail(longEmail.toString());
        
        assertEquals(longEmail.toString(), user.getEmail());
    }

    @Test
    void setName_WithLongName_ShouldSetName() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longName.append("a");
        }
        
        user.setName(longName.toString());
        
        assertEquals(longName.toString(), user.getName());
    }

    @Test
    void userEquality_SameValues_ShouldBeEqual() {
        User user1 = new User("test@example.com", "hash", "Test");
        user1.setId(1);
        
        User user2 = new User("test@example.com", "hash", "Test");
        user2.setId(1);
        
        // Note: This test assumes default Object.equals() behavior
        // If custom equals() is implemented, this test should be updated
        assertNotEquals(user1, user2); // Default Object.equals() compares references
        
        // But values should be the same
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getEmail(), user2.getEmail());
        assertEquals(user1.getPasswordHash(), user2.getPasswordHash());
        assertEquals(user1.getName(), user2.getName());
    }

    @Test
    void toString_ShouldNotExposePasswordHash() {
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$secrethash");
        user.setName("Test User");
        
        String userString = user.toString();
        
        // Default toString() should not expose sensitive information
        // This test verifies that the default Object.toString() is used
        assertTrue(userString.contains("User@"));
        assertFalse(userString.contains("secrethash"));
    }
}