package com.cs663.visionassist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class OCRProcessor {
    private TextRecognizer textRecognizer;
    private Context context;
    
    public static class TextDetection {
        private String text;
        private Rect boundingBox;
        
        public TextDetection(String text, Rect boundingBox) {
            this.text = text;
            this.boundingBox = boundingBox;
        }
        
        public String getText() {
            return text;
        }
        
        public Rect getBoundingBox() {
            return boundingBox;
        }
        
        public int getLeft() { return boundingBox.left; }
        public int getTop() { return boundingBox.top; }
        public int getRight() { return boundingBox.right; }
        public int getBottom() { return boundingBox.bottom; }
    }
    
    public OCRProcessor(Context context) {
        this.context = context;
        this.textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }
    
    public List<String> extractText(Bitmap bitmap) {
        List<TextDetection> detections = extractTextWithBoxes(bitmap);
        List<String> textResults = new ArrayList<>();
        for (TextDetection detection : detections) {
            textResults.add(detection.getText());
        }
        return textResults;
    }
    
    public List<TextDetection> extractTextWithBoxes(Bitmap bitmap) {
        List<TextDetection> textResults = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        
        textRecognizer.process(image)
            .addOnSuccessListener(visionText -> {
                for (Text.TextBlock block : visionText.getTextBlocks()) {
                    String text = block.getText().trim();
                    if (!text.isEmpty() && isNavigationText(text)) {
                        Rect boundingBox = block.getBoundingBox();
                        textResults.add(new TextDetection(text, boundingBox));
                    }
                }
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                e.printStackTrace();
                latch.countDown();
            });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return textResults;
    }
    
    private boolean isNavigationText(String text) {
        // Filter for navigation-relevant text
        String upperText = text.toUpperCase();
        return upperText.contains("STOP") ||
               upperText.contains("WALK") ||
               upperText.contains("DON'T WALK") ||
               upperText.contains("CROSS") ||
               upperText.contains("DETOUR") ||
               upperText.contains("CLOSED") ||
               upperText.contains("EXIT") ||
               upperText.contains("ENTRANCE") ||
               upperText.length() <= 20; // Short text likely to be signs
    }
    
    public void close() {
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}




