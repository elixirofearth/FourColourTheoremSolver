package com.fourcolour.auth.service;

import com.fourcolour.auth.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupService.class);
    
    @Autowired
    private SessionRepository sessionRepository;
    
    /**
     * Clean up expired sessions every hour
     * This prevents database bloat from accumulated expired sessions
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms = 1 hour)
    public void cleanupExpiredSessions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            sessionRepository.deleteExpiredSessions(now);
            logger.info("Session cleanup completed successfully");
        } catch (Exception e) {
            logger.error("Error during session cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual cleanup method that can be called on demand
     */
    public void manualCleanup() {
        cleanupExpiredSessions();
    }
} 