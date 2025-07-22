package com.fourcolour.mapstorage.service;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.common.service.LoggerClient;
import com.fourcolour.mapstorage.entity.Map;
import com.fourcolour.mapstorage.repository.MapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MapService {

    private static final Logger logger = LoggerFactory.getLogger(MapService.class);

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private LoggerClient loggerClient;

    public Map saveMap(MapRequest request) {
        logger.info("Saving map for user: {}", request.getUserId());

        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new RuntimeException("UserID is required");
        }

        Map map = new Map();
        map.setUserId(request.getUserId());
        map.setName(request.getName());
        map.setWidth(request.getWidth());
        map.setHeight(request.getHeight());
        map.setImageData(request.getImageData());
        map.setMatrix(request.getMatrix());

        Map savedMap = mapRepository.save(map);

        logger.info("Map created successfully with ID: {} for user: {}", 
                   savedMap.getId(), savedMap.getUserId());

        // Log to logger service
        try {
            loggerClient.logEvent(
                    "map-storage-service",
                    "map_created",
                    request.getUserId(),
                    "Map created: " + (request.getName() != null ? request.getName() : "null"),
                    1,
                    java.util.Map.of(
                            "map_id", savedMap.getId(),
                            "map_name", savedMap.getName() != null ? savedMap.getName() : "",
                            "width", String.valueOf(savedMap.getWidth()),
                            "height", String.valueOf(savedMap.getHeight())
                    )
            );
        } catch (Exception e) {
            logger.warn("Failed to log map creation event: {}", e.getMessage());
        }

        return savedMap;
    }

    public List<Map> getMapsByUserId(String userId) {
        logger.info("Fetching maps for user: {}", userId);
        return mapRepository.findByUserId(userId);
    }

    public Map getMapById(String id) {
        logger.info("Fetching map with ID: {}", id);
        
        if (!isValidObjectId(id)) {
            throw new IllegalArgumentException("Invalid map ID format");
        }

        Optional<Map> mapOpt = mapRepository.findById(id);
        return mapOpt.orElse(null);
    }

    public boolean deleteMap(String id) {
        logger.info("Deleting map with ID: {}", id);
        
        if (!isValidObjectId(id)) {
            throw new IllegalArgumentException("Invalid map ID format");
        }

        Optional<Map> mapOpt = mapRepository.findById(id);
        if (!mapOpt.isPresent()) {
            return false;
        }

        Map map = mapOpt.get();
        mapRepository.deleteById(id);

        logger.info("Map deleted successfully: {} for user: {}", 
                   map.getName(), map.getUserId());

        // Log to logger service
        try {
            loggerClient.logEvent(
                    "map-storage-service",
                    "map_deleted",
                    map.getUserId(),
                    "Map deleted: " + map.getName(),
                    1,
                    java.util.Map.of(
                            "map_id", id,
                            "map_name", map.getName() != null ? map.getName() : ""
                    )
            );
        } catch (Exception e) {
            logger.warn("Failed to log map deletion event: {}", e.getMessage());
        }

        return true;
    }

    private boolean isValidObjectId(String id) {
        // Basic validation for MongoDB ObjectId format (24 character hex string)
        if (id == null || id.length() != 24) {
            return false;
        }
        return id.matches("^[0-9a-fA-F]{24}$");
    }
} 