package com.cs663.visionassist;

import java.util.List;

public class SpatialAnalyzer {
    private static final float CENTER_BAND = 0.15f; // 15% band for center
    private static final float NEAR_THRESHOLD = 0.3f; // Objects taking >30% of image height are near
    private static final float FAR_THRESHOLD = 0.1f; // Objects taking <10% of image height are far
    
    public List<Detection> analyze(List<Detection> detections, int imageWidth, int imageHeight) {
        int centerX = imageWidth / 2;
        
        for (Detection detection : detections) {
            // Determine side (left, center, right)
            int detectionCenterX = detection.getCenterX();
            float relativeX = (float)(detectionCenterX - centerX) / centerX;
            
            if (Math.abs(relativeX) <= CENTER_BAND) {
                detection.setSide("center");
            } else if (relativeX < 0) {
                detection.setSide("left");
            } else {
                detection.setSide("right");
            }
            
            // Determine distance (near, mid, far)
            float heightRatio = (float) detection.getHeight() / imageHeight;
            
            if (heightRatio >= NEAR_THRESHOLD) {
                detection.setDistance("near");
            } else if (heightRatio <= FAR_THRESHOLD) {
                detection.setDistance("far");
            } else {
                detection.setDistance("mid");
            }
        }
        
        return detections;
    }
}





