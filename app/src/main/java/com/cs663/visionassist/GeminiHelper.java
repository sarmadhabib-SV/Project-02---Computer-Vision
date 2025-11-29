package com.cs663.visionassist;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.cs663.visionassist.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Gemini API integration for enhanced narration.
 * This provides more natural, contextual narrations using Google's Gemini Pro Vision model.
 * 
 * To use this feature:
 * 1. Get a Gemini API key from Google AI Studio: https://aistudio.google.com/app/apikey
 * 2. Add the API key to local.properties: GEMINI_API_KEY=your_key_here
 *    OR set environment variable: export GEMINI_API_KEY=your_key_here
 * 3. Rebuild the project
 * 
 * If no API key is provided, the app will automatically fallback to local narration.
 */
public class GeminiHelper {
    private static final String TAG = "GeminiHelper";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int TIMEOUT_SECONDS = 30;
    
    /**
     * Checks if Gemini API is configured and available.
     */
    public static boolean isAvailable() {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY_HERE");
    }
    
    /**
     * Sends image and detection summary to Gemini for enhanced narration.
     * Returns a more natural, contextual description.
     * If API is unavailable or fails, returns null to trigger fallback.
     */
    public static CompletableFuture<String> generateEnhancedNarration(
            Bitmap image, List<Detection> detections, List<String> textDetections) {
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // Check if API is available
        if (!isAvailable()) {
            Log.d(TAG, "Gemini API key not configured, using fallback");
            future.complete(null); // Return null to trigger fallback
            return future;
        }
        
        // Run in background thread
        new Thread(() -> {
            try {
                // Convert bitmap to base64
                String imageBase64 = bitmapToBase64(image);
                
                // Create detection summary
                StringBuilder detectionSummary = new StringBuilder();
                if (detections != null && !detections.isEmpty()) {
                    for (int i = 0; i < Math.min(detections.size(), 10); i++) {
                        Detection det = detections.get(i);
                        detectionSummary.append(String.format("%s (%s, %s, %.0f%%), ",
                            det.getLabel(), det.getSide(), det.getDistance(), 
                            det.getConfidence() * 100));
                    }
                } else {
                    detectionSummary.append("No objects detected");
                }
                
                // Create text summary
                String textSummary = "";
                if (textDetections != null && !textDetections.isEmpty()) {
                    textSummary = String.join(", ", textDetections.subList(0, Math.min(textDetections.size(), 5)));
                } else {
                    textSummary = "No text detected";
                }
                
                // Create prompt - optimized for low-vision users per project requirements
                String prompt = String.format(
                    "You are a friendly, supportive assistive technology narrator helping a low-vision or blind user navigate safely. " +
                    "Your role is to provide clear, natural, and actionable guidance.\n\n" +
                    "NARRATION REQUIREMENTS:\n" +
                    "- Maximum 12 words (be concise)\n" +
                    "- Action-first: Tell the user what to do, not just what exists\n" +
                    "- Sidewalk-focused: Prioritize navigation and safety-relevant information\n" +
                    "- Natural language: Use friendly, conversational phrasing (e.g., 'on your left' not 'left detected')\n" +
                    "- Safety priority: Highlight immediate hazards first\n" +
                    "- Clear spatial relationships: Use 'ahead', 'on your left/right', 'near you' for clarity\n" +
                    "- Empathetic tone: Be supportive and helpful, like a trusted guide\n\n" +
                    "CONTEXT:\n" +
                    "Detected objects: %s\n" +
                    "Detected text: %s\n\n" +
                    "GENERATE a narration that:\n" +
                    "1. Helps the user navigate safely\n" +
                    "2. Uses friendly, natural language (e.g., 'Curb ahead; bench on your left')\n" +
                    "3. Prioritizes immediate concerns and actionable guidance\n" +
                    "4. Sounds like a helpful companion, not a robotic detector\n\n" +
                    "Return ONLY the narration text (no JSON, no quotes, no explanations). " +
                    "Keep it under 12 words, be natural and friendly, and focus on what matters most for safe navigation.",
                    detectionSummary.toString(),
                    textSummary
                );
                
                // Make API call
                String response = callGeminiAPI(imageBase64, prompt);
                
                // Parse response
                String narration = parseGeminiResponse(response);
                
                if (narration != null && !narration.isEmpty()) {
                    // Clean up narration - remove any JSON formatting if present
                    narration = narration.trim();
                    if (narration.startsWith("{") && narration.contains("narration")) {
                        try {
                            JSONObject json = new JSONObject(narration);
                            narration = json.optString("narration", narration);
                        } catch (Exception e) {
                            // Not JSON, use as is
                        }
                    }
                    
                    // Ensure it's concise (max 12 words)
                    String[] words = narration.split("\\s+");
                    if (words.length > 12) {
                        StringBuilder shortened = new StringBuilder();
                        for (int i = 0; i < 12; i++) {
                            shortened.append(words[i]);
                            if (i < 11) shortened.append(" ");
                        }
                        narration = shortened.toString();
                    }
                    
                    Log.d(TAG, "Gemini narration generated: " + narration);
                    future.complete(narration);
                } else {
                    throw new IOException("Empty narration from Gemini API");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error calling Gemini API: " + e.getMessage(), e);
                // Return null to trigger fallback to local narration
                future.complete(null);
            }
        }).start();
        
        return future;
    }
    
    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Compress to reduce size (80% quality is good balance)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
    
    private static String callGeminiAPI(String imageBase64, String prompt) throws IOException {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("Gemini API key not configured");
        }
        
        // Create HTTP client with timeout
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
        
        // Build request JSON
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        // Add text prompt
        JSONObject textPart = new JSONObject();
        try {
            textPart.put("text", prompt);
            parts.put(textPart);
        } catch (org.json.JSONException e) {
            throw new IOException("Error creating text part: " + e.getMessage());
        }
        
        // Add image
        JSONObject imagePart = new JSONObject();
        JSONObject inlineData = new JSONObject();
        try {
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", imageBase64);
            imagePart.put("inline_data", inlineData);
            parts.put(imagePart);
        } catch (org.json.JSONException e) {
            throw new IOException("Error creating image part: " + e.getMessage());
        }
        
        // Assemble request
        try {
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
            
            // Add generation config for better responses
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("maxOutputTokens", 100);
            generationConfig.put("temperature", 0.4); // Lower temperature for more focused responses
            requestBody.put("generationConfig", generationConfig);
        } catch (org.json.JSONException e) {
            throw new IOException("Error assembling request: " + e.getMessage());
        }
        
        // Create HTTP request
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        String url = GEMINI_API_URL + "?key=" + apiKey;
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Gemini API error: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body() != null ? response.body().string() : "";
            if (responseBody.isEmpty()) {
                throw new IOException("Empty response from Gemini API");
            }
            
            return responseBody;
        }
    }
    
    private static String parseGeminiResponse(String response) throws IOException {
        try {
            JSONObject json = new JSONObject(response);
            
            // Check for errors
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                String message = error.optString("message", "Unknown error");
                throw new IOException("Gemini API error: " + message);
            }
            
            // Extract candidates
            if (!json.has("candidates")) {
                throw new IOException("No candidates in Gemini response");
            }
            
            JSONArray candidates = json.getJSONArray("candidates");
            if (candidates.length() == 0) {
                throw new IOException("Empty candidates array");
            }
            
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            
            if (parts.length() == 0) {
                throw new IOException("No parts in candidate content");
            }
            
            JSONObject part = parts.getJSONObject(0);
            String text = part.optString("text", "");
            
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("Empty text in Gemini response");
            }
            
            return text.trim();
            
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error parsing Gemini response: " + e.getMessage(), e);
            throw new IOException("Error parsing Gemini response: " + e.getMessage());
        }
    }
}
