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
 * This provides more natural, contextual narrations using Google's Gemini 1.5 Flash model.
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
    // Gemini API endpoint - Using gemini-2.0-flash (doesn't use thinking tokens)
    // Gemini 2.5 Flash uses thinking tokens that consume the entire output budget
    // Switching to 2.0 Flash which should work better for short outputs
    // Alternative: gemini-2.0-flash-lite (even lighter, faster)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    // Alternative endpoints if primary fails (commented out - uncomment to try)
    // private static final String GEMINI_API_URL_ALT1 = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    // private static final String GEMINI_API_URL_ALT2 = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent";
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
                
                // Create prompt - ultra-shortened to minimize token usage
                // Gemini 2.5 Flash uses thinking tokens, so we need to keep prompt very concise
                String prompt = String.format(
                    "Narrate for low-vision user (max 12 words). Action-first, safety-focused, natural language.\n" +
                    "Objects: %s. Text: %s.\n" +
                    "Return only narration text.",
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
            // Using gemini-2.0-flash which doesn't use thinking tokens
            // Can set reasonable limit for 12-word narration output
            generationConfig.put("maxOutputTokens", 100); // 100 tokens is plenty for 12-word narrations
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
            // Log response for debugging (first 500 chars to avoid spam)
            String responsePreview = response.length() > 500 ? response.substring(0, 500) + "..." : response;
            Log.d(TAG, "Parsing Gemini response (preview): " + responsePreview);
            
            JSONObject json = new JSONObject(response);
            
            // Check for errors
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                String message = error.optString("message", "Unknown error");
                throw new IOException("Gemini API error: " + message);
            }
            
            // Extract candidates
            if (!json.has("candidates")) {
                Log.e(TAG, "No candidates in response. Full response: " + response);
                throw new IOException("No candidates in Gemini response");
            }
            
            JSONArray candidates = json.getJSONArray("candidates");
            if (candidates.length() == 0) {
                Log.e(TAG, "Empty candidates array. Full response: " + response);
                throw new IOException("Empty candidates array");
            }
            
            JSONObject candidate = candidates.getJSONObject(0);
            
            // Check for safety ratings first (might explain missing content)
            if (candidate.has("safetyRatings")) {
                Log.d(TAG, "Safety ratings: " + candidate.optJSONArray("safetyRatings").toString());
            }
            
            // Check for finishReason (safety filters might block response)
            if (candidate.has("finishReason")) {
                String finishReason = candidate.optString("finishReason", "");
                Log.d(TAG, "Finish reason: " + finishReason);
                
                // Handle MAX_TOKENS - response was truncated
                if (finishReason.equals("MAX_TOKENS")) {
                    Log.w(TAG, "Response truncated due to MAX_TOKENS limit. Content may be empty.");
                    // Continue parsing - might have partial content
                } else if (finishReason.equals("SAFETY") || finishReason.equals("RECITATION") || finishReason.equals("OTHER")) {
                    throw new IOException("Response blocked or filtered. Finish reason: " + finishReason);
                }
            }
            
            // Get content - handle different possible structures
            if (!candidate.has("content")) {
                Log.e(TAG, "No content in candidate. Candidate keys: " + candidate.keys());
                Log.e(TAG, "Full candidate: " + candidate.toString());
                throw new IOException("No content in candidate - may be blocked by safety filters");
            }
            
            JSONObject content = candidate.getJSONObject("content");
            
            // Get parts - handle case where parts might not exist
            if (!content.has("parts")) {
                Log.e(TAG, "No parts in content. Content: " + content.toString());
                Log.e(TAG, "Full response: " + response);
                // Check if it's due to MAX_TOKENS truncation
                if (candidate.has("finishReason") && candidate.optString("finishReason", "").equals("MAX_TOKENS")) {
                    throw new IOException("Response truncated at token limit - no content generated. Try reducing prompt size or increasing maxOutputTokens.");
                }
                throw new IOException("No parts in candidate content - response may be empty or blocked");
            }
            
            JSONArray parts = content.getJSONArray("parts");
            
            if (parts.length() == 0) {
                // Check if it's due to MAX_TOKENS truncation
                if (candidate.has("finishReason") && candidate.optString("finishReason", "").equals("MAX_TOKENS")) {
                    throw new IOException("Response truncated at token limit - no content in parts.");
                }
                throw new IOException("No parts in candidate content");
            }
            
            // Get text from first part
            JSONObject part = parts.getJSONObject(0);
            String text = part.optString("text", "");
            
            // If no text field, try to get the entire part as string
            if (text == null || text.trim().isEmpty()) {
                // Try alternative: maybe the response structure is different
                text = part.toString();
                Log.w(TAG, "No text field found, using part as string: " + text);
            }
            
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("Empty text in Gemini response");
            }
            
            return text.trim();
            
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error parsing Gemini response: " + e.getMessage(), e);
            Log.e(TAG, "Response was: " + response);
            throw new IOException("Error parsing Gemini response: " + e.getMessage());
        }
    }
}
