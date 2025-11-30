# Gemini API Troubleshooting - Model Name Fix

## Current Status
- **Issue**: 404 error - model not found
- **Current Attempt**: `v1/models/gemini-pro:generateContent`

## Try These Options (One at a Time)

### Option 1: Try v1beta with gemini-pro (Most Common)
```java
private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
```

### Option 2: Try v1beta with gemini-1.5-pro
```java
private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
```

### Option 3: Try v1beta with gemini-1.5-flash-latest
```java
private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
```

### Option 4: Check Available Models
Test this URL in browser (replace YOUR_API_KEY):
```
https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_API_KEY
```

This will list all available models for your API key.

## Quick Fix Instructions

1. Open: `app/src/main/java/com/cs663/visionassist/GeminiHelper.java`
2. Find line 41: `GEMINI_API_URL = "..."`
3. Try Option 1 first (v1beta/gemini-pro)
4. Rebuild and test
5. If still 404, try Option 2, then Option 3

## Note
The correct model name depends on:
- Your API key's access level
- Regional availability
- API version support

The app will automatically fall back to local narration if Gemini API fails, so functionality is not broken.
