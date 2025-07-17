package com.fourcolour.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.fourcolour.logger", "com.fourcolour.common"})
@EnableMongoRepositories(basePackages = "com.fourcolour.logger.repository")
public class LoggerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggerServiceApplication.class, args);
    }
} 