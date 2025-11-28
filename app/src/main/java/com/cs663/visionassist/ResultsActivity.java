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
        
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true);
        
        try {
            JSONArray detections = new JSONArray(detectionsJson);
            for (int i = 0; i < detections.length(); i++) {
                JSONObject detection = detections.getJSONObject(i);
                
                String label = detection.getString("label");
                float confidence = (float) detection.getDouble("confidence");
                JSONObject bbox = detection.getJSONObject("bbox");
                
                int left = bbox.getInt("left");
                int top = bbox.getInt("top");
                int right = bbox.getInt("right");
                int bottom = bbox.getInt("bottom");
                
                String side = detection.optString("side", "center");
                String distance = detection.optString("distance", "mid");
                
                // Choose color based on distance
                if (distance.equals("near")) {
                    paint.setColor(Color.RED);
                } else if (distance.equals("mid")) {
                    paint.setColor(Color.YELLOW);
                } else {
                    paint.setColor(Color.GREEN);
                }
                
                // Draw bounding box
                Rect rect = new Rect(left, top, right, bottom);
                canvas.drawRect(rect, paint);
                
                // Draw label with confidence
                String labelText = String.format("%s (%.0f%%) %s %s", 
                    label, confidence * 100, side, distance);
                canvas.drawText(labelText, left, top - 10, textPaint);
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

