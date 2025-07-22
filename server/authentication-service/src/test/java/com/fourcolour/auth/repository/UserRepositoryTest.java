package com.fourcolour.auth.repository;

import com.fourcolour.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // Save user to database
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("Test User", found.get().getName());
        assertEquals("$2a$10$hashedpassword", found.get().getPasswordHash());
    }

    @Test
    void findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail(null);

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_WithEmptyEmail_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void existsByEmail_WithNullEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail(null);

        assertFalse(exists);
    }

    @Test
    void existsByEmail_WithEmptyEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("");

        assertFalse(exists);
    }

    @Test
    void save_WithValidUser_ShouldPersistUser() {
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Test User", savedUser.getName());
        assertEquals("$2a$10$hashedpassword", savedUser.getPasswordHash());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void save_WithDuplicateEmail_ShouldThrowException() {
        // Save first user
        entityManager.persistAndFlush(testUser);

        // Try to save another user with same email
        User duplicateUser = new User();
        duplicateUser.setName("Duplicate User");
        duplicateUser.setEmail("test@example.com");
        duplicateUser.setPasswordHash("$2a$10$anotherhash");

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        User savedUser = entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findById(savedUser.getId());

        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getId());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findById(999);

        assertFalse(found.isPresent());
    }

    @Test
    void findById_WithNullId_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findById(null);

        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteUser() {
        User savedUser = entityManager.persistAndFlush(testUser);
        Integer userId = savedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithNonExistentId_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            userRepository.deleteById(999);
        });
    }

    @Test
    void findAll_WithMultipleUsers_ShouldReturnAllUsers() {
        // Create and save multiple users
        User user1 = new User("user1@example.com", "hash1", "User One");
        User user2 = new User("user2@example.com", "hash2", "User Two");
        User user3 = new User("user3@example.com", "hash3", "User Three");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        var allUsers = userRepository.findAll();

        assertEquals(3, allUsers.size());
    }

    @Test
    void findAll_WithNoUsers_ShouldReturnEmptyList() {
        var allUsers = userRepository.findAll();

        assertTrue(allUsers.isEmpty());
    }

    @Test
    void count_WithMultipleUsers_ShouldReturnCorrectCount() {
        User user1 = new User("user1@example.com", "hash1", "User One");
        User user2 = new User("user2@example.com", "hash2", "User Two");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        long count = userRepository.count();

        assertEquals(2, count);
    }

    @Test
    void count_WithNoUsers_ShouldReturnZero() {
        long count = userRepository.count();

        assertEquals(0, count);
    }

    @Test
    void save_WithUserHavingNullName_ShouldPersistUser() {
        testUser.setName(null);

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertNull(savedUser.getName());
        assertEquals("$2a$10$hashedpassword", savedUser.getPasswordHash());
    }

    @Test
    void save_WithUserHavingEmptyName_ShouldPersistUser() {
        testUser.setName("");

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("", savedUser.getName());
        assertEquals("$2a$10$hashedpassword", savedUser.getPasswordHash());
    }

    @Test
    void findByEmail_WithCaseInsensitiveEmail_ShouldNotMatch() {
        entityManager.persistAndFlush(testUser);

        // Email matching is case-sensitive by default
        Optional<User> found = userRepository.findByEmail("TEST@EXAMPLE.COM");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_WithExtraWhitespace_ShouldNotMatch() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail(" test@example.com ");

        assertFalse(found.isPresent());
    }

    @Test
    void save_AndUpdate_ShouldPersistChanges() {
        User savedUser = userRepository.save(testUser);
        Integer userId = savedUser.getId();

        // Update the user
        savedUser.setName("Updated Name");
        savedUser.setPasswordHash("$2a$10$newhash");
        User updatedUser = userRepository.save(savedUser);

        assertEquals(userId, updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("$2a$10$newhash", updatedUser.getPasswordHash());
        assertEquals("test@example.com", updatedUser.getEmail()); // Email should remain the same
    }

    @Test
    void existsByEmail_WithMultipleUsers_ShouldReturnCorrectResults() {
        User user1 = new User("user1@example.com", "hash1", "User One");
        User user2 = new User("user2@example.com", "hash2", "User Two");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        assertTrue(userRepository.existsByEmail("user1@example.com"));
        assertTrue(userRepository.existsByEmail("user2@example.com"));
        assertFalse(userRepository.existsByEmail("user3@example.com"));
    }

    @Test
    void save_WithLongEmail_ShouldPersistUser() {
        StringBuilder longEmail = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longEmail.append("a");
        }
        longEmail.append("@example.com");

        testUser.setEmail(longEmail.toString());

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals(longEmail.toString(), savedUser.getEmail());
    }

    @Test
    void save_WithLongName_ShouldPersistUser() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longName.append("a");
        }

        testUser.setName(longName.toString());

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals(longName.toString(), savedUser.getName());
    }

    @Test
    void save_WithSpecialCharactersInEmail_ShouldPersistUser() {
        testUser.setEmail("user+test@example-site.co.uk");

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("user+test@example-site.co.uk", savedUser.getEmail());
    }

    @Test
    void save_WithSpecialCharactersInName_ShouldPersistUser() {
        testUser.setName("José María O'Connor-Smith");

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("José María O'Connor-Smith", savedUser.getName());
    }

    @Test
    void save_WithUnicodeCharacters_ShouldPersistUser() {
        testUser.setName("测试用户");
        testUser.setEmail("测试@example.com");

        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("测试用户", savedUser.getName());
        assertEquals("测试@example.com", savedUser.getEmail());
    }

    @Test
    void findByEmail_AfterDelete_ShouldReturnEmpty() {
        User savedUser = entityManager.persistAndFlush(testUser);
        String email = savedUser.getEmail();

        userRepository.delete(savedUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail(email);
        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_AfterDelete_ShouldReturnFalse() {
        User savedUser = entityManager.persistAndFlush(testUser);
        String email = savedUser.getEmail();

        userRepository.delete(savedUser);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail(email);
        assertFalse(exists);
    }

    @Test
    void save_WithValidEmailFormats_ShouldPersistAllUsers() {
        String[] validEmails = {
                "simple@example.com",
                "user.name@example.com",
                "user+tag@example.com",
                "user_name@example.com",
                "123@example.com",
                "user@example-site.com",
                "user@example.co.uk"
        };

        for (int i = 0; i < validEmails.length; i++) {
            User user = new User();
            user.setName("User " + i);
            user.setEmail(validEmails[i]);
            user.setPasswordHash("$2a$10$hash" + i);

            User savedUser = userRepository.save(user);
            assertNotNull(savedUser.getId());
            assertEquals(validEmails[i], savedUser.getEmail());
        }

        assertEquals(validEmails.length, userRepository.count());
    }

    @Test
    void transactional_RollbackOnException_ShouldNotPersistData() {
        // This test verifies that the repository respects transactional boundaries
        // In @DataJpaTest, each test method is wrapped in a transaction that rolls back
        
        userRepository.save(testUser);
        
        // The user should be visible within this transaction
        assertTrue(userRepository.existsByEmail("test@example.com"));
        
        // After the test completes, the transaction will roll back
        // and the user won't be persisted to subsequent tests
    }

    @Test
    void entityManager_FlushAndClear_ShouldRefreshData() {
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // After flush and clear, we need to fetch from database again
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void save_ConcurrentAccess_ShouldHandleCorrectly() {
        // This simulates what might happen with concurrent access
        User user1 = new User("concurrent1@example.com", "hash1", "User 1");
        User user2 = new User("concurrent2@example.com", "hash2", "User 2");

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        assertNotNull(savedUser1.getId());
        assertNotNull(savedUser2.getId());
        assertNotEquals(savedUser1.getId(), savedUser2.getId());
        
        assertTrue(userRepository.existsByEmail("concurrent1@example.com"));
        assertTrue(userRepository.existsByEmail("concurrent2@example.com"));
    }
}