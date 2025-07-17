package com.fourcolour.logger.repository;

import com.fourcolour.logger.entity.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<Log, String> {
    List<Log> findByServiceName(String serviceName);
    List<Log> findByUserId(String userId);
    List<Log> findByEventType(String eventType);
    List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Log> findByServiceNameAndEventType(String serviceName, String eventType);
} 