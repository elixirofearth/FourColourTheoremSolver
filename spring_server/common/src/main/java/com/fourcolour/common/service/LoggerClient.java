package com.fourcolour.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LoggerClient {

    private static final Logger logger = LoggerFactory.getLogger(LoggerClient.class);

    // TODO: Implement gRPC client when proto files are available
    // For now, provide a simple logging interface
    
    public void logEvent(String serviceName, String eventType, String userId, String description) {
        logEvent(serviceName, eventType, userId, description, 1, null);
    }

    public void logEvent(String serviceName, String eventType, String userId, String description, 
                        int severity, Map<String, String> metadata) {
        try {
            logger.info("[{}] {} - User: {}, Event: {}, Description: {}, Metadata: {}", 
                       serviceName, eventType, userId, eventType, description, metadata);
            
            // TODO: Replace with actual gRPC call
            // LogRequest request = LogRequest.newBuilder()
            //     .setServiceName(serviceName)
            //     .setEventType(eventType)
            //     .setUserId(userId)
            //     .setDescription(description)
            //     .setSeverity(severity)
            //     .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            //     .putAllMetadata(metadata != null ? metadata : Collections.emptyMap())
            //     .build();
            //
            // LogResponse response = loggerServiceStub.logEvent(request);
            
        } catch (Exception e) {
            logger.error("Failed to log event: {}", e.getMessage(), e);
        }
    }
} 