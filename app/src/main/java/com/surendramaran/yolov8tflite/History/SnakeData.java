package com.surendramaran.yolov8tflite.History;

public class SnakeData {
    private String title;
    private String confidence;
    private String location;
    private String imageUrl;

    public SnakeData() {
    }

    public SnakeData(String title, String confidence,String location, String imageUrl) {
        this.title = title;
        this.confidence = confidence;
        this.location=location;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getLocation(){return location;}

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}