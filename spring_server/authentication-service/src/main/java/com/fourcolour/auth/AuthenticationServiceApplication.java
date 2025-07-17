package com.fourcolour.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.fourcolour.auth", "com.fourcolour.common"})
@EntityScan(basePackages = "com.fourcolour.auth.entity")
@EnableJpaRepositories(basePackages = "com.fourcolour.auth.repository")
public class AuthenticationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
} 