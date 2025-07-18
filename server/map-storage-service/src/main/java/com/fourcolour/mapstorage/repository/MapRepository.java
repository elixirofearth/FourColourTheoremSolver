package com.fourcolour.mapstorage.repository;

import com.fourcolour.mapstorage.entity.Map;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapRepository extends MongoRepository<Map, String> {
    List<Map> findByUserId(String userId);
} 