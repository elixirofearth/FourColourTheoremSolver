package com.fourcolour.logger.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "logs")
public class Log {
    @Id
    private String id;

    @Field("service_name")
    @JsonProperty("service_name")
    private String serviceName;

    @Field("event_type")
    @JsonProperty("event_type")
    private String eventType;

    @Field("user_id")
    @JsonProperty("user_id")
    private String userId;

    private String description;

    private Integer severity;

    private LocalDateTime timestamp;

    private Map<String, String> metadata;

    public Log() {
        this.timestamp = LocalDateTime.now();
    }

    public Log(String serviceName, String eventType, String userId, String description, 
               Integer severity, LocalDateTime timestamp, Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.eventType = eventType;
        this.userId = userId;
        this.description = description;
        this.severity = severity;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.metadata = metadata;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
} 