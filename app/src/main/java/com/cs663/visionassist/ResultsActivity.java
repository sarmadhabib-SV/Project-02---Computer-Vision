package com.cs663.visionassist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {
    private ImageView capturedImage;
    private TextView narrationText;
    private TextView detectionsSummary;
    private MaterialButton playAgainButton;
    private MaterialButton retakeButton;
    
    private TextToSpeech textToSpeech;
    private Bitmap originalBitmap;
    private String narration;
    private String detectionsJson;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        
        initializeViews();
        initializeTextToSpeech();
        loadResults();
    }
    
    private void initializeViews() {
        capturedImage = findViewById(R.id.capturedImage);
        narrationText = findViewById(R.id.narrationText);
        detectionsSummary = findViewById(R.id.detectionsSummary);
        playAgainButton = findViewById(R.id.playAgainButton);
        retakeButton = findViewById(R.id.retakeButton);
        
        playAgainButton.setOnClickListener(v -> playNarration());
        retakeButton.setOnClickListener(v -> finish());
    }
    
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Language not supported
                }
            }
        });
    }
    
    private void loadResults() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        
        // Load bitmap from URI instead of Intent
        String imageUriString = extras.getString("imageUri");
        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                originalBitmap = BitmapFactory.decodeFile(imageUri.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        narration = extras.getString("narration", "No narration available");
        String detectionsSummaryText = extras.getString("detections", "");
        detectionsJson = extras.getString("detectedObjects", "[]");
        
        // Display narration
        narrationText.setText(narration);
        detectionsSummary.setText(detectionsSummaryText);
        
        // Draw detections on image
        if (originalBitmap != null) {
            Bitmap annotatedBitmap = drawDetections(originalBitmap, detectionsJson);
            capturedImage.setImageBitmap(annotatedBitmap);
        }
        
        // Auto-play narration
        playNarration();
    }
    
    private Bitmap drawDetections(Bitmap bitmap, String detectionsJson) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        
        Paint objectPaint = new Paint();
        objectPaint.setStyle(Paint.Style.STROKE);
        objectPaint.setStrokeWidth(4f);
        
        Paint textPaint = new Paint();
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(4f);
        textPaint.setColor(Color.CYAN); // Cyan for text bounding boxes
        
        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(32f);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setFakeBoldText(true);
        
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAlpha(128); // Semi-transparent background for labels
        
        try {
            JSONArray detections = new JSONArray(detectionsJson);
            for (int i = 0; i < detections.length(); i++) {
                JSONObject detection = detections.getJSONObject(i);
                
                String type = detection.optString("type", "object");
                String label = detection.getString("label");
                JSONObject bbox = detection.getJSONObject("bbox");
                
                int left = bbox.getInt("left");
                int top = bbox.getInt("top");
                int right = bbox.getInt("right");
                int bottom = bbox.getInt("bottom");
                
                Rect rect = new Rect(left, top, right, bottom);
                
                if ("text".equals(type)) {
                    // Draw text bounding box in cyan
                    canvas.drawRect(rect, textPaint);
                    
                    // Draw text label
                    String labelText = "TEXT: " + label;
                    float textWidth = labelPaint.measureText(labelText);
                    Rect labelRect = new Rect(left, Math.max(0, top - 40), 
                                             left + (int)textWidth + 10, top);
                    canvas.drawRect(labelRect, backgroundPaint);
                    canvas.drawText(labelText, left + 5, top - 10, labelPaint);
                } else {
                    // Object detection
                    float confidence = (float) detection.getDouble("confidence");
                    String side = detection.optString("side", "center");
                    String distance = detection.optString("distance", "mid");
                    
                    // Choose color based on distance for objects
                    if (distance.equals("near")) {
                        objectPaint.setColor(Color.RED);
                    } else if (distance.equals("mid")) {
                        objectPaint.setColor(Color.YELLOW);
                    } else {
                        objectPaint.setColor(Color.GREEN);
                    }
                    
                    // Draw object bounding box
                    canvas.drawRect(rect, objectPaint);
                    
                    // Draw object label with confidence
                    String labelText = String.format("%s (%.0f%%)", label, confidence * 100);
                    float textWidth = labelPaint.measureText(labelText);
                    Rect labelRect = new Rect(left, Math.max(0, top - 40), 
                                             left + (int)textWidth + 10, top);
                    canvas.drawRect(labelRect, backgroundPaint);
                    canvas.drawText(labelText, left + 5, top - 10, labelPaint);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return mutableBitmap;
    }
    
    private void playNarration() {
        if (textToSpeech != null && narration != null && !narration.isEmpty()) {
            textToSpeech.speak(narration, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

