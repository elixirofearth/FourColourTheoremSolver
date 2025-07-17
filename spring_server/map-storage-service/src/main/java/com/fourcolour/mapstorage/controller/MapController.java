package com.fourcolour.mapstorage.controller;

import com.fourcolour.common.dto.MapRequest;
import com.fourcolour.mapstorage.entity.Map;
import com.fourcolour.mapstorage.service.MapService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maps")
public class MapController {

    @Autowired
    private MapService mapService;

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping
    public ResponseEntity<?> saveMap(@Valid @RequestBody MapRequest request) {
        try {
            Map savedMap = mapService.saveMap(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to save map"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMaps(@RequestParam("userId") String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of("error", "UserID is required"));
            }

            List<Map> maps = mapService.getMapsByUserId(userId);
            return ResponseEntity.ok(maps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to fetch maps"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMap(@PathVariable String id) {
        try {
            Map map = mapService.getMapById(id);
            if (map == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Map not found"));
            }
            return ResponseEntity.ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "Invalid map ID"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to retrieve map"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMap(@PathVariable String id) {
        try {
            boolean deleted = mapService.deleteMap(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Map not found"));
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "Invalid map ID"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to delete map"));
        }
    }
} 