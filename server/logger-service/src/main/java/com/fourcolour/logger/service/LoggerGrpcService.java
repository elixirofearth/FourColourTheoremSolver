package com.fourcolour.logger.service;

import com.fourcolour.logger.entity.Log;
import com.fourcolour.logger.proto.LoggerProto.*;
import com.fourcolour.logger.proto.LoggerServiceGrpc;
import com.fourcolour.logger.repository.LogRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private RabbitTemplate rabbitTemplate;

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

            // Publish to RabbitMQ
            String exchange = getExchangeForService(request.getServiceName());
            rabbitTemplate.convertAndSend(exchange, "", logEntity);
            
            logger.info("Successfully published log to RabbitMQ exchange: {}", exchange);

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

    private String getExchangeForService(String serviceName) {
        switch (serviceName.toLowerCase()) {
            case "auth":
            case "authentication":
            case "authentication-service":
                return "auth_logs";
            case "map_coloring":
            case "map_coloring_service":
            case "solver":
            case "solver-service":
                return "map_coloring_logs";
            case "map_storage":
            case "map_storage_service":
            case "map-storage":
            case "map-storage-service":
                return "map_storage_logs";
            default:
                logger.warn("Unknown service: {}, using map_coloring_logs exchange", serviceName);
                return "map_coloring_logs";
        }
    }
} 