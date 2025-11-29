package com.cs663.visionassist;

public class Detection {
    private String label;
    private float confidence;
    private int left, top, right, bottom;
    private String side; // left, center, right
    private String distance; // near, mid, far
    
    public Detection(String label, float confidence, int left, int top, int right, int bottom) {
        this.label = label;
        this.confidence = confidence;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    
    public String getLabel() {
        return label;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public int getLeft() {
        return left;
    }
    
    public int getTop() {
        return top;
    }
    
    public int getRight() {
        return right;
    }
    
    public int getBottom() {
        return bottom;
    }
    
    public String getSide() {
        return side != null ? side : "center";
    }
    
    public void setSide(String side) {
        this.side = side;
    }
    
    public String getDistance() {
        return distance != null ? distance : "mid";
    }
    
    public void setDistance(String distance) {
        this.distance = distance;
    }
    
    public int getWidth() {
        return right - left;
    }
    
    public int getHeight() {
        return bottom - top;
    }
    
    public int getCenterX() {
        return (left + right) / 2;
    }
    
    public int getCenterY() {
        return (top + bottom) / 2;
    }
}





