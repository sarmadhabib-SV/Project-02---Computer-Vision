package com.cs663.visionassist;

import android.content.Context;
import android.graphics.Bitmap;

public class ImageProcessor {
    
    public static ProcessingResult processImage(Context context, Bitmap bitmap) {
        // Resize bitmap for processing (maintain aspect ratio)
        int maxDimension = 640;
        Bitmap processedBitmap = resizeBitmap(bitmap, maxDimension);
        
        // Run object detection
        ObjectDetector detector = new ObjectDetector(context);
        java.util.List<Detection> detections = detector.detect(processedBitmap);
        
        // Run OCR - extract text with bounding boxes
        OCRProcessor ocrProcessor = new OCRProcessor(context);
        java.util.List<String> textDetections = ocrProcessor.extractText(processedBitmap);
        java.util.List<OCRProcessor.TextDetection> textDetectionsWithBoxes = 
            ocrProcessor.extractTextWithBoxes(processedBitmap);
        
        // Analyze spatial relationships
        SpatialAnalyzer analyzer = new SpatialAnalyzer();
        java.util.List<Detection> analyzedDetections = analyzer.analyze(detections, 
            processedBitmap.getWidth(), processedBitmap.getHeight());
        
        // Generate narration - try Gemini first if available, fallback to local
        String narration = generateNarration(processedBitmap, analyzedDetections, textDetections);
        
        // Create summary
        String summary = createSummary(analyzedDetections, textDetections);
        
        // Convert to JSON - include both object and text detections
        String detectionsJson = convertToJson(analyzedDetections, textDetectionsWithBoxes);
        
        return new ProcessingResult(narration, summary, detectionsJson, processedBitmap);
    }
    
    /**
     * Generates narration using Gemini API if available, otherwise falls back to local narration.
     * This method handles the async Gemini call and waits for result with timeout.
     */
    private static String generateNarration(Bitmap bitmap, java.util.List<Detection> detections, 
                                           java.util.List<String> textDetections) {
        // Check if Gemini is available
        if (GeminiHelper.isAvailable()) {
            try {
                // Try Gemini with timeout (10 seconds)
                java.util.concurrent.CompletableFuture<String> geminiFuture = 
                    GeminiHelper.generateEnhancedNarration(bitmap, detections, textDetections);
                
                try {
                    // Wait for Gemini response with timeout
                    String geminiNarration = geminiFuture.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (geminiNarration != null && !geminiNarration.trim().isEmpty()) {
                        android.util.Log.d("ImageProcessor", "Using Gemini narration");
                        return geminiNarration;
                    }
                } catch (java.util.concurrent.TimeoutException e) {
                    android.util.Log.w("ImageProcessor", "Gemini API timeout, using fallback");
                } catch (java.util.concurrent.ExecutionException e) {
                    android.util.Log.w("ImageProcessor", "Gemini API error, using fallback: " + e.getMessage());
                } catch (InterruptedException e) {
                    android.util.Log.w("ImageProcessor", "Gemini API interrupted, using fallback");
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                android.util.Log.w("ImageProcessor", "Error calling Gemini API, using fallback: " + e.getMessage());
            }
        }
        
        // Fallback to local narration
        android.util.Log.d("ImageProcessor", "Using local narration");
        return NarrationGenerator.generate(detections, textDetections);
    }
    
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }
        
        float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    private static String createSummary(java.util.List<Detection> detections, 
                                       java.util.List<String> texts) {
        StringBuilder summary = new StringBuilder();
        
        if (detections.isEmpty() && texts.isEmpty()) {
            return "No objects or text detected.";
        }
        
        if (!detections.isEmpty()) {
            summary.append("Objects: ");
            for (int i = 0; i < Math.min(detections.size(), 5); i++) {
                Detection d = detections.get(i);
                summary.append(d.getLabel())
                       .append(" (").append(d.getSide())
                       .append(", ").append(d.getDistance()).append(")");
                if (i < Math.min(detections.size(), 5) - 1) {
                    summary.append(" • ");
                }
            }
            if (detections.size() > 5) {
                summary.append(" and ").append(detections.size() - 5).append(" more");
            }
        }
        
        if (!texts.isEmpty()) {
            if (summary.length() > 0) {
                summary.append("\n");
            }
            summary.append("Text: ");
            for (int i = 0; i < Math.min(texts.size(), 3); i++) {
                summary.append(texts.get(i));
                if (i < Math.min(texts.size(), 3) - 1) {
                    summary.append(", ");
                }
            }
        }
        
        return summary.toString();
    }
    
    private static String convertToJson(java.util.List<Detection> detections, 
                                       java.util.List<OCRProcessor.TextDetection> textDetections) {
        org.json.JSONArray jsonArray = new org.json.JSONArray();
        
        // Add object detections
        for (Detection detection : detections) {
            try {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("type", "object");
                obj.put("label", detection.getLabel());
                obj.put("confidence", detection.getConfidence());
                obj.put("side", detection.getSide());
                obj.put("distance", detection.getDistance());
                
                org.json.JSONObject bbox = new org.json.JSONObject();
                bbox.put("left", detection.getLeft());
                bbox.put("top", detection.getTop());
                bbox.put("right", detection.getRight());
                bbox.put("bottom", detection.getBottom());
                obj.put("bbox", bbox);
                
                jsonArray.put(obj);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        
        // Add text detections with bounding boxes
        for (OCRProcessor.TextDetection textDetection : textDetections) {
            try {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("type", "text");
                obj.put("label", textDetection.getText());
                obj.put("confidence", 1.0); // OCR doesn't provide confidence
                obj.put("side", "center"); // Default for text
                obj.put("distance", "mid"); // Default for text
                
                org.json.JSONObject bbox = new org.json.JSONObject();
                bbox.put("left", textDetection.getLeft());
                bbox.put("top", textDetection.getTop());
                bbox.put("right", textDetection.getRight());
                bbox.put("bottom", textDetection.getBottom());
                obj.put("bbox", bbox);
                
                jsonArray.put(obj);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        
        return jsonArray.toString();
    }
}



