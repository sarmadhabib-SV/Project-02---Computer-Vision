package com.cs663.visionassist;

import java.util.List;

public class NarrationGenerator {
    private static final int MAX_NARRATION_WORDS = 12;
    
    public static String generate(List<Detection> detections, List<String> textDetections) {
        if (detections.isEmpty() && textDetections.isEmpty()) {
            return "No objects or text detected in this scene.";
        }
        
        StringBuilder narration = new StringBuilder();
        
        // Prioritize near objects and hazards
        List<Detection> sortedDetections = prioritizeDetections(detections);
        
        // Add critical near objects first
        boolean hasNearHazard = false;
        for (Detection det : sortedDetections) {
            if (det.getDistance().equals("near") && isHazard(det.getLabel())) {
                if (!hasNearHazard) {
                    narration.append("Caution: ").append(det.getLabel())
                            .append(" ahead ").append(det.getSide()).append(".");
                    hasNearHazard = true;
                    break;
                }
            }
        }
        
        // Add text detections if present
        if (!textDetections.isEmpty() && narration.length() == 0) {
            narration.append("Sign reads: ").append(textDetections.get(0));
        }
        
        // Add other significant detections
        if (narration.length() == 0 && !sortedDetections.isEmpty()) {
            Detection first = sortedDetections.get(0);
            narration.append(first.getLabel())
                    .append(" detected ").append(first.getSide())
                    .append(", ").append(first.getDistance()).append(".");
        }
        
        // Add additional context if space allows
        if (narration.length() < 50 && sortedDetections.size() > 1) {
            Detection second = sortedDetections.get(1);
            if (second.getDistance().equals("near")) {
                narration.append(" Also ").append(second.getLabel())
                        .append(" ").append(second.getSide()).append(".");
            }
        }
        
        String result = narration.toString();
        
        // Ensure narration is concise (max 12 words)
        String[] words = result.split("\\s+");
        if (words.length > MAX_NARRATION_WORDS) {
            StringBuilder shortened = new StringBuilder();
            for (int i = 0; i < MAX_NARRATION_WORDS; i++) {
                shortened.append(words[i]);
                if (i < MAX_NARRATION_WORDS - 1) {
                    shortened.append(" ");
                }
            }
            shortened.append(".");
            result = shortened.toString();
        }
        
        return result;
    }
    
    private static List<Detection> prioritizeDetections(List<Detection> detections) {
        // Sort by: 1) distance (near first), 2) confidence (high first)
        java.util.Collections.sort(detections, (d1, d2) -> {
            // Near objects first
            int distanceCompare = getDistancePriority(d1.getDistance()) 
                                - getDistancePriority(d2.getDistance());
            if (distanceCompare != 0) {
                return distanceCompare;
            }
            // Then by confidence
            return Float.compare(d2.getConfidence(), d1.getConfidence());
        });
        return detections;
    }
    
    private static int getDistancePriority(String distance) {
        switch (distance) {
            case "near": return 0;
            case "mid": return 1;
            case "far": return 2;
            default: return 1;
        }
    }
    
    private static boolean isHazard(String label) {
        String lowerLabel = label.toLowerCase();
        return lowerLabel.contains("car") ||
               lowerLabel.contains("truck") ||
               lowerLabel.contains("bus") ||
               lowerLabel.contains("bicycle") ||
               lowerLabel.contains("motorcycle") ||
               lowerLabel.contains("cone") ||
               lowerLabel.contains("barrier") ||
               lowerLabel.contains("pole") ||
               lowerLabel.contains("curb") ||
               lowerLabel.contains("stair");
    }
}





