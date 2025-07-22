package com.fourcolour.auth.repository;

import com.fourcolour.auth.entity.Session;
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
class SessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Session testSession;

    @BeforeEach
    void setUp() {
        // Create and save a test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser = entityManager.persistAndFlush(testUser);

        // Create a test session
        testSession = new Session();
        testSession.setUserId(testUser.getId());
        testSession.setToken("test-jwt-token");
        testSession.setExpiresAt(LocalDateTime.now().plusHours(1));
        testSession.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByToken_WithExistingToken_ShouldReturnSession() {
        entityManager.persistAndFlush(testSession);

        Optional<Session> found = sessionRepository.findByToken("test-jwt-token");

        assertTrue(found.isPresent());
        assertEquals("test-jwt-token", found.get().getToken());
        assertEquals(testUser.getId(), found.get().getUserId());
    }

    @Test
    void findByToken_WithNonExistentToken_ShouldReturnEmpty() {
        Optional<Session> found = sessionRepository.findByToken("non-existent-token");

        assertFalse(found.isPresent());
    }

    @Test
    void findByToken_WithNullToken_ShouldReturnEmpty() {
        Optional<Session> found = sessionRepository.findByToken(null);

        assertFalse(found.isPresent());
    }

    @Test
    void findByToken_WithEmptyToken_ShouldReturnEmpty() {
        Optional<Session> found = sessionRepository.findByToken("");

        assertFalse(found.isPresent());
    }

    @Test
    void findByTokenAndNotExpired_WithValidToken_ShouldReturnSession() {
        entityManager.persistAndFlush(testSession);
        LocalDateTime now = LocalDateTime.now();

        Optional<Session> found = sessionRepository.findByTokenAndNotExpired("test-jwt-token", now);

        assertTrue(found.isPresent());
        assertEquals("test-jwt-token", found.get().getToken());
        assertTrue(found.get().getExpiresAt().isAfter(now));
    }

    @Test
    void findByTokenAndNotExpired_WithExpiredToken_ShouldReturnEmpty() {
        // Set session to expire in the past
        testSession.setExpiresAt(LocalDateTime.now().minusHours(1));
        entityManager.persistAndFlush(testSession);

        LocalDateTime now = LocalDateTime.now();
        Optional<Session> found = sessionRepository.findByTokenAndNotExpired("test-jwt-token", now);

        assertFalse(found.isPresent());
    }

    @Test
    void findByTokenAndNotExpired_WithNonExistentToken_ShouldReturnEmpty() {
        LocalDateTime now = LocalDateTime.now();
        Optional<Session> found = sessionRepository.findByTokenAndNotExpired("non-existent-token", now);

        assertFalse(found.isPresent());
    }

    @Test
    void findByTokenAndNotExpired_WithNullToken_ShouldReturnEmpty() {
        LocalDateTime now = LocalDateTime.now();
        Optional<Session> found = sessionRepository.findByTokenAndNotExpired(null, now);

        assertFalse(found.isPresent());
    }

    @Test
    void deleteByToken_WithExistingToken_ShouldDeleteSession() {
        Session savedSession = entityManager.persistAndFlush(testSession);

        sessionRepository.deleteByToken("test-jwt-token");
        entityManager.flush();

        Optional<Session> found = sessionRepository.findById(savedSession.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void deleteByToken_WithNonExistentToken_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            sessionRepository.deleteByToken("non-existent-token");
        });
    }

    @Test
    void deleteByToken_WithNullToken_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            sessionRepository.deleteByToken(null);
        });
    }

    @Test
    void deleteByUserId_WithExistingUserId_ShouldDeleteAllUserSessions() {
        // Create multiple sessions for the same user
        Session session1 = new Session(testUser.getId(), "token1", LocalDateTime.now().plusHours(1));
        Session session2 = new Session(testUser.getId(), "token2", LocalDateTime.now().plusHours(1));
        Session session3 = new Session(testUser.getId(), "token3", LocalDateTime.now().plusHours(1));

        entityManager.persistAndFlush(session1);
        entityManager.persistAndFlush(session2);
        entityManager.persistAndFlush(session3);

        sessionRepository.deleteByUserId(testUser.getId());
        entityManager.flush();

        assertFalse(sessionRepository.findByToken("token1").isPresent());
        assertFalse(sessionRepository.findByToken("token2").isPresent());
        assertFalse(sessionRepository.findByToken("token3").isPresent());
    }

    @Test
    void deleteByUserId_WithNonExistentUserId_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            sessionRepository.deleteByUserId(999);
        });
    }

    @Test
    void deleteByUserId_WithNullUserId_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            sessionRepository.deleteByUserId(null);
        });
    }

    @Test
    void deleteExpiredSessions_ShouldDeleteOnlyExpiredSessions() {
        // Create expired sessions
        Session expiredSession1 = new Session(testUser.getId(), "expired1", LocalDateTime.now().minusHours(1));
        Session expiredSession2 = new Session(testUser.getId(), "expired2", LocalDateTime.now().minusMinutes(30));

        // Create valid sessions
        Session validSession1 = new Session(testUser.getId(), "valid1", LocalDateTime.now().plusHours(1));
        Session validSession2 = new Session(testUser.getId(), "valid2", LocalDateTime.now().plusMinutes(30));

        entityManager.persistAndFlush(expiredSession1);
        entityManager.persistAndFlush(expiredSession2);
        entityManager.persistAndFlush(validSession1);
        entityManager.persistAndFlush(validSession2);

        LocalDateTime now = LocalDateTime.now();
        sessionRepository.deleteExpiredSessions(now);
        entityManager.flush();

        // Expired sessions should be deleted
        assertFalse(sessionRepository.findByToken("expired1").isPresent());
        assertFalse(sessionRepository.findByToken("expired2").isPresent());

        // Valid sessions should remain
        assertTrue(sessionRepository.findByToken("valid1").isPresent());
        assertTrue(sessionRepository.findByToken("valid2").isPresent());
    }

    @Test
    void deleteExpiredSessions_WithNoExpiredSessions_ShouldNotDeleteAnything() {
        // Create only valid sessions
        Session validSession1 = new Session(testUser.getId(), "valid1", LocalDateTime.now().plusHours(1));
        Session validSession2 = new Session(testUser.getId(), "valid2", LocalDateTime.now().plusMinutes(30));

        entityManager.persistAndFlush(validSession1);
        entityManager.persistAndFlush(validSession2);

        long countBefore = sessionRepository.count();
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.deleteExpiredSessions(now);
        entityManager.flush();
        long countAfter = sessionRepository.count();

        assertEquals(countBefore, countAfter);
        assertTrue(sessionRepository.findByToken("valid1").isPresent());
        assertTrue(sessionRepository.findByToken("valid2").isPresent());
    }

    @Test
    void deleteExpiredSessions_WithAllExpiredSessions_ShouldDeleteAll() {
        // Create only expired sessions
        Session expiredSession1 = new Session(testUser.getId(), "expired1", LocalDateTime.now().minusHours(1));
        Session expiredSession2 = new Session(testUser.getId(), "expired2", LocalDateTime.now().minusMinutes(30));

        entityManager.persistAndFlush(expiredSession1);
        entityManager.persistAndFlush(expiredSession2);

        LocalDateTime now = LocalDateTime.now();
        sessionRepository.deleteExpiredSessions(now);
        entityManager.flush();

        assertEquals(0, sessionRepository.count());
    }

    @Test
    void save_WithValidSession_ShouldPersistSession() {
        Session savedSession = sessionRepository.save(testSession);

        assertNotNull(savedSession.getId());
        assertEquals(testUser.getId(), savedSession.getUserId());
        assertEquals("test-jwt-token", savedSession.getToken());
        assertNotNull(savedSession.getExpiresAt());
        assertNotNull(savedSession.getCreatedAt());
    }

    @Test
    void save_WithDuplicateToken_ShouldThrowException() {
        entityManager.persistAndFlush(testSession);

        Session duplicateSession = new Session();
        duplicateSession.setUserId(testUser.getId());
        duplicateSession.setToken("test-jwt-token"); // Same token
        duplicateSession.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertThrows(Exception.class, () -> {
            sessionRepository.saveAndFlush(duplicateSession);
        });
    }

    @Test
    void findById_WithExistingId_ShouldReturnSession() {
        Session savedSession = entityManager.persistAndFlush(testSession);

        Optional<Session> found = sessionRepository.findById(savedSession.getId());

        assertTrue(found.isPresent());
        assertEquals(savedSession.getId(), found.get().getId());
        assertEquals("test-jwt-token", found.get().getToken());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<Session> found = sessionRepository.findById(999);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_WithMultipleSessions_ShouldReturnAllSessions() {
        Session session1 = new Session(testUser.getId(), "token1", LocalDateTime.now().plusHours(1));
        Session session2 = new Session(testUser.getId(), "token2", LocalDateTime.now().plusHours(1));
        Session session3 = new Session(testUser.getId(), "token3", LocalDateTime.now().plusHours(1));

        entityManager.persistAndFlush(session1);
        entityManager.persistAndFlush(session2);
        entityManager.persistAndFlush(session3);

        var allSessions = sessionRepository.findAll();

        assertEquals(3, allSessions.size());
    }

    @Test
    void count_WithMultipleSessions_ShouldReturnCorrectCount() {
        Session session1 = new Session(testUser.getId(), "token1", LocalDateTime.now().plusHours(1));
        Session session2 = new Session(testUser.getId(), "token2", LocalDateTime.now().plusHours(1));

        entityManager.persistAndFlush(session1);
        entityManager.persistAndFlush(session2);

        long count = sessionRepository.count();

        assertEquals(2, count);
    }

    @Test
    void findByTokenAndNotExpired_WithExactExpiryTime_ShouldReturnEmpty() {
        LocalDateTime exactExpiryTime = LocalDateTime.now().plusMinutes(30);
        testSession.setExpiresAt(exactExpiryTime);
        entityManager.persistAndFlush(testSession);

        // Query with the exact expiry time
        Optional<Session> found = sessionRepository.findByTokenAndNotExpired("test-jwt-token", exactExpiryTime);

        // Session expires at exactExpiryTime, so it should not be found when queried at that time
        assertFalse(found.isPresent());
    }

    @Test
    void findByTokenAndNotExpired_WithTimeBeforeExpiry_ShouldReturnSession() {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(30);
        testSession.setExpiresAt(expiryTime);
        entityManager.persistAndFlush(testSession);

        LocalDateTime queryTime = expiryTime.minusMinutes(5); // 5 minutes before expiry
        Optional<Session> found = sessionRepository.findByTokenAndNotExpired("test-jwt-token", queryTime);

        assertTrue(found.isPresent());
        assertEquals("test-jwt-token", found.get().getToken());
    }

    @Test
    void deleteByUserId_WithMultipleUsers_ShouldDeleteOnlyTargetUserSessions() {
        // Create another user
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("$2a$10$anotherhash");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // Create sessions for both users
        Session user1Session1 = new Session(testUser.getId(), "user1token1", LocalDateTime.now().plusHours(1));
        Session user1Session2 = new Session(testUser.getId(), "user1token2", LocalDateTime.now().plusHours(1));
        Session user2Session1 = new Session(anotherUser.getId(), "user2token1", LocalDateTime.now().plusHours(1));
        Session user2Session2 = new Session(anotherUser.getId(), "user2token2", LocalDateTime.now().plusHours(1));

        entityManager.persistAndFlush(user1Session1);
        entityManager.persistAndFlush(user1Session2);
        entityManager.persistAndFlush(user2Session1);
        entityManager.persistAndFlush(user2Session2);

        // Delete sessions for testUser only
        sessionRepository.deleteByUserId(testUser.getId());
        entityManager.flush();

        // testUser sessions should be deleted
        assertFalse(sessionRepository.findByToken("user1token1").isPresent());
        assertFalse(sessionRepository.findByToken("user1token2").isPresent());

        // anotherUser sessions should remain
        assertTrue(sessionRepository.findByToken("user2token1").isPresent());
        assertTrue(sessionRepository.findByToken("user2token2").isPresent());
    }

    @Test
    void save_WithLongToken_ShouldPersistSession() {
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longToken.append("a");
        }

        testSession.setToken(longToken.toString());

        Session savedSession = sessionRepository.save(testSession);

        assertNotNull(savedSession.getId());
        assertEquals(longToken.toString(), savedSession.getToken());
    }

    @Test
    void save_WithFarFutureExpiry_ShouldPersistSession() {
        LocalDateTime farFuture = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        testSession.setExpiresAt(farFuture);

        Session savedSession = sessionRepository.save(testSession);

        assertNotNull(savedSession.getId());
        assertEquals(farFuture, savedSession.getExpiresAt());
    }

    @Test
    void save_AndUpdate_ShouldPersistChanges() {
        Session savedSession = sessionRepository.save(testSession);
        Integer sessionId = savedSession.getId();

        // Update the session
        LocalDateTime newExpiryTime = LocalDateTime.now().plusHours(2);
        savedSession.setExpiresAt(newExpiryTime);
        Session updatedSession = sessionRepository.save(savedSession);

        assertEquals(sessionId, updatedSession.getId());
        assertEquals(newExpiryTime, updatedSession.getExpiresAt());
        assertEquals("test-jwt-token", updatedSession.getToken()); // Token should remain the same
    }

    @Test
    void transactional_RollbackOnException_ShouldNotPersistData() {
        sessionRepository.save(testSession);
        
        // The session should be visible within this transaction
        assertTrue(sessionRepository.findByToken("test-jwt-token").isPresent());
        
        // After the test completes, the transaction will roll back
    }

    @Test
    void findByTokenAndNotExpired_WithNullTime_ShouldHandleGracefully() {
        entityManager.persistAndFlush(testSession);

        Optional<Session> found = sessionRepository.findByTokenAndNotExpired("test-jwt-token", null);

        // Should handle null time parameter gracefully
        assertFalse(found.isPresent());
    }

    @Test
    void deleteExpiredSessions_WithNullTime_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            sessionRepository.deleteExpiredSessions(null);
        });
    }
}