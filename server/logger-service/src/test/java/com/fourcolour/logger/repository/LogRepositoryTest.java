package com.fourcolour.logger.repository;

import com.fourcolour.logger.entity.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DataMongoTest
@ActiveProfiles("test")
@Testcontainers
class LogRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private LogRepository logRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
    }

    @Test
    void save_WithValidLog_ShouldSaveSuccessfully() {
        Log log = new Log();
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        log.setUserId("user123");
        log.setDescription("User logged in successfully");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertEquals("auth-service", savedLog.getServiceName());
        assertEquals("user_login", savedLog.getEventType());
        assertEquals("user123", savedLog.getUserId());
        assertEquals("User logged in successfully", savedLog.getDescription());
        assertEquals(1, savedLog.getSeverity());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    void findById_WithValidId_ShouldReturnLog() {
        Log log = new Log();
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        log.setUserId("user123");
        log.setDescription("User logged in successfully");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());

        Log savedLog = logRepository.save(log);
        Optional<Log> foundLog = logRepository.findById(savedLog.getId());

        assertTrue(foundLog.isPresent());
        assertEquals(savedLog.getId(), foundLog.get().getId());
        assertEquals("auth-service", foundLog.get().getServiceName());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<Log> foundLog = logRepository.findById("507f1f77bcf86cd799439011");

        assertFalse(foundLog.isPresent());
    }

    @Test
    void findByServiceName_WithValidServiceName_ShouldReturnLogs() {
        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(LocalDateTime.now());

        Log log2 = new Log();
        log2.setServiceName("auth-service");
        log2.setEventType("user_logout");
        log2.setUserId("user123");
        log2.setDescription("User logged out successfully");
        log2.setSeverity(1);
        log2.setTimestamp(LocalDateTime.now());

        logRepository.save(log1);
        logRepository.save(log2);

        List<Log> foundLogs = logRepository.findByServiceName("auth-service");

        assertEquals(2, foundLogs.size());
        assertTrue(foundLogs.stream().allMatch(log -> "auth-service".equals(log.getServiceName())));
    }

    @Test
    void findByServiceName_WithNonExistentServiceName_ShouldReturnEmptyList() {
        List<Log> foundLogs = logRepository.findByServiceName("non-existent-service");

        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void findByUserId_WithValidUserId_ShouldReturnLogs() {
        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(LocalDateTime.now());

        Log log2 = new Log();
        log2.setServiceName("map-storage-service");
        log2.setEventType("map_created");
        log2.setUserId("user123");
        log2.setDescription("Map created successfully");
        log2.setSeverity(1);
        log2.setTimestamp(LocalDateTime.now());

        logRepository.save(log1);
        logRepository.save(log2);

        List<Log> foundLogs = logRepository.findByUserId("user123");

        assertEquals(2, foundLogs.size());
        assertTrue(foundLogs.stream().allMatch(log -> "user123".equals(log.getUserId())));
    }

    @Test
    void findByUserId_WithNonExistentUserId_ShouldReturnEmptyList() {
        List<Log> foundLogs = logRepository.findByUserId("non-existent-user");

        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void findByEventType_WithValidEventType_ShouldReturnLogs() {
        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(LocalDateTime.now());

        Log log2 = new Log();
        log2.setServiceName("map-storage-service");
        log2.setEventType("user_login");
        log2.setUserId("user456");
        log2.setDescription("User logged in successfully");
        log2.setSeverity(1);
        log2.setTimestamp(LocalDateTime.now());

        logRepository.save(log1);
        logRepository.save(log2);

        List<Log> foundLogs = logRepository.findByEventType("user_login");

        assertEquals(2, foundLogs.size());
        assertTrue(foundLogs.stream().allMatch(log -> "user_login".equals(log.getEventType())));
    }

    @Test
    void findByEventType_WithNonExistentEventType_ShouldReturnEmptyList() {
        List<Log> foundLogs = logRepository.findByEventType("non-existent-event");

        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void findByTimestampBetween_WithValidRange_ShouldReturnLogs() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);

        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(now);

        Log log2 = new Log();
        log2.setServiceName("map-storage-service");
        log2.setEventType("map_created");
        log2.setUserId("user456");
        log2.setDescription("Map created successfully");
        log2.setSeverity(1);
        log2.setTimestamp(now.plusMinutes(30));

        logRepository.save(log1);
        logRepository.save(log2);

        List<Log> foundLogs = logRepository.findByTimestampBetween(start, end);

        assertEquals(2, foundLogs.size());
        assertTrue(foundLogs.stream().allMatch(log -> 
            log.getTimestamp().isAfter(start) && log.getTimestamp().isBefore(end)));
    }

    @Test
    void findByTimestampBetween_WithNoLogsInRange_ShouldReturnEmptyList() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().minusMinutes(30);

        List<Log> foundLogs = logRepository.findByTimestampBetween(start, end);

        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void findByServiceNameAndEventType_WithValidCombination_ShouldReturnLogs() {
        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(LocalDateTime.now());

        Log log2 = new Log();
        log2.setServiceName("auth-service");
        log2.setEventType("user_logout");
        log2.setUserId("user123");
        log2.setDescription("User logged out successfully");
        log2.setSeverity(1);
        log2.setTimestamp(LocalDateTime.now());

        logRepository.save(log1);
        logRepository.save(log2);

        List<Log> foundLogs = logRepository.findByServiceNameAndEventType("auth-service", "user_login");

        assertEquals(1, foundLogs.size());
        assertEquals("auth-service", foundLogs.get(0).getServiceName());
        assertEquals("user_login", foundLogs.get(0).getEventType());
    }

    @Test
    void findByServiceNameAndEventType_WithNonExistentCombination_ShouldReturnEmptyList() {
        List<Log> foundLogs = logRepository.findByServiceNameAndEventType("non-existent-service", "non-existent-event");

        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void delete_WithValidLog_ShouldDeleteSuccessfully() {
        Log log = new Log();
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        log.setUserId("user123");
        log.setDescription("User logged in successfully");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());

        Log savedLog = logRepository.save(log);
        logRepository.delete(savedLog);

        Optional<Log> foundLog = logRepository.findById(savedLog.getId());
        assertFalse(foundLog.isPresent());
    }

    @Test
    void save_WithLogContainingMetadata_ShouldSaveMetadata() {
        Log log = new Log();
        log.setServiceName("auth-service");
        log.setEventType("user_login");
        log.setUserId("user123");
        log.setDescription("User logged in successfully");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        metadata.put("ip_address", "192.168.1.1");
        metadata.put("user_agent", "Mozilla/5.0");
        log.setMetadata(metadata);

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog.getMetadata());
        assertEquals("192.168.1.1", savedLog.getMetadata().get("ip_address"));
        assertEquals("Mozilla/5.0", savedLog.getMetadata().get("user_agent"));
    }

    @Test
    void save_WithLogContainingNullValues_ShouldSaveSuccessfully() {
        Log log = new Log();
        log.setServiceName(null);
        log.setEventType(null);
        log.setUserId(null);
        log.setDescription(null);
        log.setSeverity(null);
        log.setTimestamp(LocalDateTime.now());
        log.setMetadata(null);

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertNull(savedLog.getServiceName());
        assertNull(savedLog.getEventType());
        assertNull(savedLog.getUserId());
        assertNull(savedLog.getDescription());
        assertNull(savedLog.getSeverity());
        assertNotNull(savedLog.getTimestamp());
        assertNull(savedLog.getMetadata());
    }

    @Test
    void save_WithLogContainingEmptyValues_ShouldSaveSuccessfully() {
        Log log = new Log();
        log.setServiceName("");
        log.setEventType("");
        log.setUserId("");
        log.setDescription("");
        log.setSeverity(1);
        log.setTimestamp(LocalDateTime.now());
        log.setMetadata(new java.util.HashMap<>());

        Log savedLog = logRepository.save(log);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());
        assertEquals("", savedLog.getServiceName());
        assertEquals("", savedLog.getEventType());
        assertEquals("", savedLog.getUserId());
        assertEquals("", savedLog.getDescription());
        assertEquals(1, savedLog.getSeverity());
        assertNotNull(savedLog.getTimestamp());
        assertTrue(savedLog.getMetadata().isEmpty());
    }

    @Test
    void findAll_WithMultipleLogs_ShouldReturnAllLogs() {
        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(LocalDateTime.now());

        Log log2 = new Log();
        log2.setServiceName("map-storage-service");
        log2.setEventType("map_created");
        log2.setUserId("user456");
        log2.setDescription("Map created successfully");
        log2.setSeverity(1);
        log2.setTimestamp(LocalDateTime.now());

        logRepository.save(log1);
        logRepository.save(log2);

        List<Log> allLogs = logRepository.findAll();

        assertEquals(2, allLogs.size());
    }

    @Test
    void count_WithMultipleLogs_ShouldReturnCorrectCount() {
        Log log1 = new Log();
        log1.setServiceName("auth-service");
        log1.setEventType("user_login");
        log1.setUserId("user123");
        log1.setDescription("User logged in successfully");
        log1.setSeverity(1);
        log1.setTimestamp(LocalDateTime.now());

        Log log2 = new Log();
        log2.setServiceName("map-storage-service");
        log2.setEventType("map_created");
        log2.setUserId("user456");
        log2.setDescription("Map created successfully");
        log2.setSeverity(1);
        log2.setTimestamp(LocalDateTime.now());

        logRepository.save(log1);
        logRepository.save(log2);

        long count = logRepository.count();

        assertEquals(2, count);
    }
} 