package com.fourcolour.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MapRequest {
    @NotBlank(message = "User ID is required")
    @JsonProperty("userId")
    private String userId;
    
    private String name;
    
    @JsonProperty("imageData")
    private String imageData;
    
    private int[][] matrix;
    
    @NotNull(message = "Width is required")
    private Integer width;
    
    @NotNull(message = "Height is required")
    private Integer height;

    public MapRequest() {}

    public MapRequest(String userId, String name, String imageData, int[][] matrix, Integer width, Integer height) {
        this.userId = userId;
        this.name = name;
        this.imageData = imageData;
        this.matrix = matrix;
        this.width = width;
        this.height = height;
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
} 