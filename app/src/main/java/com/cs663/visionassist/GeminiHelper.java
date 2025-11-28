package com.cs663.visionassist;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Optional Gemini API integration for enhanced narration.
 * This is an extra credit feature that provides more natural, contextual narrations.
 * 
 * To use this feature:
 * 1. Get a Gemini API key from Google AI Studio
 * 2. Add the API key to your app's build.gradle or use environment variables
 * 3. Uncomment the Gemini calls in ImageProcessor.java
 */
public class GeminiHelper {
    private static final String GEMINI_API_KEY = "YOUR_API_KEY_HERE"; // Replace with your API key
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent";
    
    /**
     * Sends image and detection summary to Gemini for enhanced narration.
     * Returns a more natural, contextual description.
     */
    public static CompletableFuture<String> generateEnhancedNarration(
            Bitmap image, List<Detection> detections, List<String> textDetections) {
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // Run in background thread
        new Thread(() -> {
            try {
                // Convert bitmap to base64
                String imageBase64 = bitmapToBase64(image);
                
                // Create detection summary
                StringBuilder detectionSummary = new StringBuilder();
                for (Detection det : detections) {
                    detectionSummary.append(String.format("%s (%s, %s, %.0f%%), ",
                        det.getLabel(), det.getSide(), det.getDistance(), 
                        det.getConfidence() * 100));
                }
                
                // Create prompt
                String prompt = String.format(
                    "You are an assistive technology narrator for low-vision users. " +
                    "Given this image and detected objects, provide a concise narration " +
                    "(max 12 words) that describes spatial relationships and potential hazards. " +
                    "Be action-oriented and sidewalk-focused.\n\n" +
                    "Detected objects: %s\n" +
                    "Detected text: %s\n\n" +
                    "Provide a short, clear narration in JSON format: " +
                    "{\"narration\": \"your narration here\"}",
                    detectionSummary.toString(),
                    String.join(", ", textDetections)
                );
                
                // Make API call (simplified - you would use actual HTTP client)
                // For production, use OkHttp or Retrofit
                String response = callGeminiAPI(imageBase64, prompt);
                
                // Parse JSON response
                String narration = parseGeminiResponse(response);
                future.complete(narration);
                
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to local narration
                future.complete(NarrationGenerator.generate(detections, textDetections));
            }
        }).start();
        
        return future;
    }
    
    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
    
    private static String callGeminiAPI(String imageBase64, String prompt) throws IOException {
        // This is a placeholder - implement actual HTTP call using OkHttp or similar
        // Example structure:
        /*
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);
        
        JSONObject imagePart = new JSONObject();
        JSONObject inlineData = new JSONObject();
        inlineData.put("mime_type", "image/jpeg");
        inlineData.put("data", imageBase64);
        imagePart.put("inline_data", inlineData);
        parts.put(imagePart);
        
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);
        
        RequestBody body = RequestBody.create(requestBody.toString(), mediaType);
        Request request = new Request.Builder()
            .url(GEMINI_API_URL + "?key=" + GEMINI_API_KEY)
            .post(body)
            .build();
        
        Response response = client.newCall(request).execute();
        return response.body().string();
        */
        
        // For now, return empty to use fallback
        throw new IOException("Gemini API not fully implemented - using fallback");
    }
    
    private static String parseGeminiResponse(String response) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(response);
            org.json.JSONArray candidates = json.getJSONArray("candidates");
            org.json.JSONObject candidate = candidates.getJSONObject(0);
            org.json.JSONObject content = candidate.getJSONObject("content");
            org.json.JSONArray parts = content.getJSONArray("parts");
            org.json.JSONObject part = parts.getJSONObject(0);
            String text = part.getString("text");
            
            // Try to extract JSON from text if it's wrapped
            if (text.contains("{")) {
                int start = text.indexOf("{");
                int end = text.lastIndexOf("}") + 1;
                String jsonStr = text.substring(start, end);
                org.json.JSONObject narrationJson = new org.json.JSONObject(jsonStr);
                return narrationJson.getString("narration");
            }
            
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "Enhanced narration unavailable";
        }
    }
}

