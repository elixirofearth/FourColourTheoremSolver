package com.fourcolour.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class TokenResponse {
    private String token;
    private String name;
    
    @JsonProperty("user_id")
    private Integer userId;
    
    private String email;
    
    @JsonProperty("expires_at")
    private String expiresAt;

    public TokenResponse() {}

    public TokenResponse(String token, String name, Integer userId, String email, String expiresAt) {
        this.token = token;
        this.name = name;
        this.userId = userId;
        this.email = email;
        this.expiresAt = expiresAt;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
} 