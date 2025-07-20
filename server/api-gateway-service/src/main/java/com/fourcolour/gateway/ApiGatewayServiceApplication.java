package com.fourcolour.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.fourcolour.gateway", "com.fourcolour.common"})
public class ApiGatewayServiceApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayServiceApplication.class);
    
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ApiGatewayServiceApplication.class, args);
        
        // Log all beans to see if Redis configuration is loaded
        logger.info("Application started successfully");
        logger.info("Total beans loaded: {}", context.getBeanDefinitionNames().length);
        
        // Check if Redis beans are present
        try {
            context.getBean("redisConnectionFactory");
            logger.info("Redis connection factory found");
        } catch (Exception e) {
            logger.warn("Redis connection factory not found: {}", e.getMessage());
        }
        
        try {
            context.getBean("redisTemplate");
            logger.info("Redis template found");
        } catch (Exception e) {
            logger.warn("Redis template not found: {}", e.getMessage());
        }
    }
} 