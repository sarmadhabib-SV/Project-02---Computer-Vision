# Gemini API Integration Setup

## Overview

The Vision Assist app now includes optional Gemini API integration for enhanced, natural-language narrations. This is an **extra credit feature** that uses Google's Gemini Pro Vision model to generate more contextual and natural descriptions of detected objects.

## How It Works

- **Primary**: When configured, the app uses Gemini API to generate narrations
- **Automatic Fallback**: If Gemini is unavailable or fails, the app automatically uses the local `NarrationGenerator`
- **No Configuration Required**: The app works perfectly fine without Gemini - it's completely optional!

## Setup Instructions

### Step 1: Get a Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Sign in with your Google account
3. Click **"Create API Key"**
4. Copy your API key (it will look like: `AIzaSy...`)

### Step 2: Configure the API Key

You have **two options** to set the API key:

#### Option A: Using local.properties (Recommended for Development)

1. Open or create `local.properties` in your project root
2. Add this line (replace with your actual key):
   ```properties
   GEMINI_API_KEY=AIzaSyYourActualApiKeyHere
   ```
3. Rebuild the project

**Note**: `local.properties` is already in `.gitignore`, so your key won't be committed to Git.

#### Option B: Using Environment Variable

1. Set the environment variable:
   ```bash
   export GEMINI_API_KEY=AIzaSyYourActualApiKeyHere
   ```
2. Rebuild the project

### Step 3: Rebuild and Run

```bash
./gradlew clean build
```

The app will automatically detect the API key and use Gemini when available.

## Verification

To verify Gemini is working:

1. Check the logcat for messages:
   - `"Gemini API key not configured, using fallback"` = Not configured
   - `"Using Gemini narration"` = Working!
   - `"Using local narration"` = Fallback (API failed or unavailable)

2. Take a photo and notice more natural, contextual narrations (if Gemini is active)

## Current Implementation

- ✅ **Fully Integrated**: GeminiHelper is called automatically from ImageProcessor
- ✅ **Async Processing**: API calls run in background threads
- ✅ **Timeout Handling**: 10-second timeout with automatic fallback
- ✅ **Error Handling**: Comprehensive error handling with graceful fallback
- ✅ **Configuration**: API key via BuildConfig (from local.properties or env var)

## API Usage

The Gemini integration:
- Uses **Gemini Pro Vision** model
- Sends compressed images (80% JPEG quality)
- Limits responses to 12 words for concise narrations
- Includes detection summaries and spatial relationships
- Optimized for assistive technology use cases

## Troubleshooting

### Gemini not being used?

1. Check that `local.properties` exists and contains `GEMINI_API_KEY=...`
2. Verify the API key is valid (no extra spaces)
3. Rebuild the project after adding the key
4. Check logcat for error messages

### API errors?

- **401 Unauthorized**: API key is invalid
- **429 Too Many Requests**: Rate limit exceeded (free tier has limits)
- **Timeout**: Network issue or API is slow (app will fallback automatically)

### Want to disable Gemini?

Simply remove or comment out the `GEMINI_API_KEY` line in `local.properties` or don't set the environment variable. The app will automatically use local narration.

## Cost Information

- Google provides **free tier** for Gemini API
- Check current pricing: [Google AI Studio Pricing](https://aistudio.google.com/pricing)
- The app optimizes API calls (image compression, concise prompts) to minimize usage

## Code Structure

- `GeminiHelper.java`: Handles all Gemini API communication
- `ImageProcessor.java`: Calls GeminiHelper with automatic fallback
- `app/build.gradle`: BuildConfig setup for API key injection

## Extra Credit Notes

This implementation satisfies the **Extra Credit A: Multiple Models** requirement:
- ✅ Gemini integration for enhanced narrations
- ✅ Automatic fallback maintains functionality
- ✅ Configurable and optional



