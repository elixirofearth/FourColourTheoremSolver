package com.fourcolour.common.service;

import com.fourcolour.logger.proto.LoggerProto;
import com.fourcolour.logger.proto.LoggerServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

@Service
public class LoggerClient {

    private static final Logger logger = LoggerFactory.getLogger(LoggerClient.class);

    @GrpcClient("logger-service")
    private LoggerServiceGrpc.LoggerServiceBlockingStub loggerServiceStub;
    
    public void logEvent(String serviceName, String eventType, String userId, String description) {
        logEvent(serviceName, eventType, userId, description, 1, null);
    }

    public void logEvent(String serviceName, String eventType, String userId, String description, 
                        int severity, Map<String, String> metadata) {
        try {
            LoggerProto.LogRequest request = LoggerProto.LogRequest.newBuilder()
                .setServiceName(serviceName)
                .setEventType(eventType)
                .setUserId(userId)
                .setDescription(description)
                .setSeverity(severity)
                .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .putAllMetadata(metadata != null ? metadata : Collections.emptyMap())
                .build();

            LoggerProto.LogResponse response = loggerServiceStub.logEvent(request);
            
            if (!response.getSuccess()) {
                logger.warn("Failed to log event remotely: {}", response.getMessage());
            } else {
                logger.debug("Successfully logged event: {}", description);
            }
            
        } catch (Exception e) {
            logger.error("Failed to log event via gRPC: {}", e.getMessage(), e);
            // Fallback to local logging
            logger.info("[{}] {} - User: {}, Event: {}, Description: {}, Metadata: {}", 
                       serviceName, eventType, userId, eventType, description, metadata);
        }
    }
} 