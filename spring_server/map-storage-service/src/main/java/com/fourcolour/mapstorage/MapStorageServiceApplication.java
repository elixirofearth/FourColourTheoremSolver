package com.fourcolour.mapstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.fourcolour.mapstorage", "com.fourcolour.common"})
@EnableMongoRepositories(basePackages = "com.fourcolour.mapstorage.repository")
public class MapStorageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapStorageServiceApplication.class, args);
    }
} 