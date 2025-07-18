package com.fourcolour.common.dto;


public class ColoringRequest {
    private ImageData image;
    private int width;
    private int height;
    private String userId;

    public ColoringRequest() {}

    public ColoringRequest(ImageData image, int width, int height, String userId) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ImageData getImage() {
        return image;
    }

    public void setImage(ImageData image) {
        this.image = image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static class ImageData {
        private int[] data;

        public ImageData() {}

        public ImageData(int[] data) {
            this.data = data;
        }

        public int[] getData() {
            return data;
        }

        public void setData(int[] data) {
            this.data = data;
        }
    }
} 