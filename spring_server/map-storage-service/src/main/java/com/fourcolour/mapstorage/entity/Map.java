package com.fourcolour.mapstorage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "maps")
public class Map {
    @Id
    private String id;

    @Field("userId")
    @JsonProperty("userId")
    private String userId;

    private String name;

    private Integer width;

    private Integer height;

    @Field("imageData")
    @JsonProperty("imageData")
    private String imageData;

    private int[][] matrix;

    @Field("createdAt")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public Map() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Map(String userId, String name, Integer width, Integer height, String imageData, int[][] matrix) {
        this();
        this.userId = userId;
        this.name = name;
        this.width = width;
        this.height = height;
        this.imageData = imageData;
        this.matrix = matrix;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 