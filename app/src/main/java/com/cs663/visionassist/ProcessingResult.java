package com.cs663.visionassist;

import android.graphics.Bitmap;

public class ProcessingResult {
    private String narration;
    private String detectionsSummary;
    private String detectedObjectsJson;
    private Bitmap processedBitmap;
    
    public ProcessingResult(String narration, String detectionsSummary, 
                          String detectedObjectsJson, Bitmap processedBitmap) {
        this.narration = narration;
        this.detectionsSummary = detectionsSummary;
        this.detectedObjectsJson = detectedObjectsJson;
        this.processedBitmap = processedBitmap;
    }
    
    public String getNarration() {
        return narration;
    }
    
    public String getDetectionsSummary() {
        return detectionsSummary;
    }
    
    public String getDetectedObjectsJson() {
        return detectedObjectsJson;
    }
    
    public Bitmap getProcessedBitmap() {
        return processedBitmap;
    }
}





