package com.fourcolour.logger.service;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.*;
import com.fourcolour.logger.proto.LoggerServiceGrpc;
import com.fourcolour.logger.repository.LogRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@GrpcService
public class LoggerGrpcService extends LoggerServiceGrpc.LoggerServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(LoggerGrpcService.class);

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private KafkaTemplate<String, Log> kafkaTemplate;

    @Override
    public void logEvent(LogRequest request, StreamObserver<LogResponse> responseObserver) {
        logger.info("Received log request from service: {} for user: {}", 
                   request.getServiceName(), request.getUserId());

        try {
            // Parse timestamp
            LocalDateTime timestamp;
            try {
                timestamp = LocalDateTime.parse(request.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                logger.warn("Failed to parse timestamp: {}, using current time", request.getTimestamp());
                timestamp = LocalDateTime.now();
            }

            // Create log entity
            Log logEntity = new Log(
                    request.getServiceName(),
                    request.getEventType(),
                    request.getUserId(),
                    request.getDescription(),
                    request.getSeverity(),
                    timestamp,
                    request.getMetadataMap()
            );

            // Publish to Kafka
            String topic = getTopicForService(request.getServiceName());
            kafkaTemplate.send(topic, logEntity);
            
            logger.info("Successfully published log to Kafka topic: {}", topic);

            // Send success response
            LogResponse response = LogResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Log published successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Error processing log event: {}", e.getMessage(), e);

            // Send error response
            LogResponse response = LogResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to publish log: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private String getTopicForService(String serviceName) {
        String lowerServiceName = serviceName.toLowerCase();
        
        // Check for auth-related services
        if (lowerServiceName.contains("auth") || lowerServiceName.contains("authentication")) {
            return "auth-logs";
        }
        
        // Check for map coloring/solver services
        if (lowerServiceName.contains("map_coloring") || lowerServiceName.contains("solver")) {
            return "map-coloring-logs";
        }
        
        // Check for map storage services
        if (lowerServiceName.contains("map_storage") || lowerServiceName.contains("map-storage")) {
            return "map-storage-logs";
        }
        
        logger.warn("Unknown service: {}, using map-coloring-logs topic", serviceName);
        return "map-coloring-logs";
    }
} 