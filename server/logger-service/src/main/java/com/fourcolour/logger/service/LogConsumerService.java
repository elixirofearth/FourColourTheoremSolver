package com.fourcolour.logger.service;

import com.fourcolour.logger.config.KafkaConfig;
import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(LogConsumerService.class);

    @Autowired
    private LogRepository logRepository;

    @KafkaListener(topics = KafkaConfig.AUTH_LOGS_TOPIC, groupId = "logger-service-group")
    public void handleAuthLog(Log logMessage) {
        logger.info("Processing auth log: {} for user: {}", logMessage.getEventType(), logMessage.getUserId());
        saveLog(logMessage);
    }

    @KafkaListener(topics = KafkaConfig.MAP_COLORING_LOGS_TOPIC, groupId = "logger-service-group")
    public void handleMapColoringLog(Log logMessage) {
        logger.info("Processing map coloring log: {} for user: {}", logMessage.getEventType(), logMessage.getUserId());
        saveLog(logMessage);
    }

    @KafkaListener(topics = KafkaConfig.MAP_STORAGE_LOGS_TOPIC, groupId = "logger-service-group")
    public void handleMapStorageLog(Log logMessage) {
        logger.info("Processing map storage log: {} for user: {}", logMessage.getEventType(), logMessage.getUserId());
        saveLog(logMessage);
    }

    private void saveLog(Log logMessage) {
        try {
            Log savedLog = logRepository.save(logMessage);
            logger.info("Successfully saved log to MongoDB with ID: {}", savedLog.getId());
        } catch (Exception e) {
            logger.error("Failed to save log to MongoDB: {}", e.getMessage(), e);
        }
    }
} 