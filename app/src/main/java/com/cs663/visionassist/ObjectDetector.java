package com.cs663.visionassist;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetector {
    private static final String MODEL_FILE = "efficientdet_lite0.tflite";
    private static final int INPUT_SIZE = 320;
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final int NUM_DETECTIONS = 10;
    
    private Interpreter interpreter;
    private List<String> labels;
    private Context context;
    
    public ObjectDetector(Context context) {
        this.context = context;
        try {
            loadModel();
            loadLabels();
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Model failed to load - interpreter will be null
            // This will be handled gracefully in detect() method
        }
    }
    
    private void loadModel() {
        try {
            // Check if file exists in assets
            try {
                java.io.InputStream inputStream = context.getAssets().open(MODEL_FILE);
                inputStream.close();
            } catch (IOException e) {
                throw new IOException("Model file not found in assets: " + MODEL_FILE + ". Make sure efficientdet_lite0.tflite is in app/src/main/assets/", e);
            }
            
            ByteBuffer modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(modelBuffer, options);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load model: " + MODEL_FILE + ". Error: " + e.getMessage(), e);
        }
    }
    
    private void loadLabels() {
        labels = new ArrayList<>();
        try {
            List<String> labelList = FileUtil.loadLabels(context, "labels.txt");
            for (String label : labelList) {
                labels.add(label.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Use default COCO labels if file not found
            loadDefaultLabels();
        }
    }
    
    private void loadDefaultLabels() {
        labels = new ArrayList<>();
        String[] defaultLabels = {
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck",
            "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench",
            "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra",
            "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove",
            "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup",
            "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange",
            "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
            "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink",
            "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier",
            "toothbrush"
        };
        for (String label : defaultLabels) {
            labels.add(label);
        }
    }
    
    public List<Detection> detect(Bitmap bitmap) {
        List<Detection> detections = new ArrayList<>();
        
        if (interpreter == null) {
            return detections;
        }
        
        // Preprocess image
        TensorImage tensorImage = preprocessImage(bitmap);
        
        // Get model output tensor info
        int outputTensorCount = interpreter.getOutputTensorCount();
        
        // Prepare output arrays - EfficientDet typically has 4 outputs
        // Output 0: locations [1, num_detections, 4] (ymin, xmin, ymax, xmax)
        // Output 1: classes [1, num_detections]
        // Output 2: scores [1, num_detections]
        // Output 3: num_detections [1]
        
        float[][][] outputLocations = new float[1][NUM_DETECTIONS][4];
        float[][] outputClasses = new float[1][NUM_DETECTIONS];
        float[][] outputScores = new float[1][NUM_DETECTIONS];
        float[] numDetections = new float[1];
        
        // Run inference
        try {
            ByteBuffer inputBuffer = tensorImage.getBuffer();
            java.util.Map<Integer, Object> outputs = new java.util.HashMap<>();
            
            // Map outputs based on tensor count
            if (outputTensorCount >= 4) {
                outputs.put(0, outputLocations);
                outputs.put(1, outputClasses);
                outputs.put(2, outputScores);
                outputs.put(3, numDetections);
            } else if (outputTensorCount == 3) {
                // Some models don't have num_detections output
                outputs.put(0, outputLocations);
                outputs.put(1, outputClasses);
                outputs.put(2, outputScores);
            }
            
            interpreter.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputs);
            
            // Parse results
            int numDetected = outputTensorCount >= 4 ? (int) numDetections[0] : NUM_DETECTIONS;
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();
            
            for (int i = 0; i < Math.min(numDetected, NUM_DETECTIONS); i++) {
                float score = outputScores[0][i];
                if (score < CONFIDENCE_THRESHOLD) {
                    continue;
                }
                
                int classId = (int) outputClasses[0][i];
                if (classId < 0 || classId >= labels.size()) {
                    continue;
                }
                
                String label = labels.get(classId);
                
                // Convert normalized coordinates to pixel coordinates
                // EfficientDet format: [ymin, xmin, ymax, xmax] normalized 0-1
                float yMin = outputLocations[0][i][0] * imageHeight;
                float xMin = outputLocations[0][i][1] * imageWidth;
                float yMax = outputLocations[0][i][2] * imageHeight;
                float xMax = outputLocations[0][i][3] * imageWidth;
                
                int left = Math.max(0, (int) xMin);
                int top = Math.max(0, (int) yMin);
                int right = Math.min(imageWidth - 1, (int) xMax);
                int bottom = Math.min(imageHeight - 1, (int) yMax);
                
                // Only add if valid bounding box
                if (right > left && bottom > top) {
                    Detection detection = new Detection(label, score, left, top, right, bottom);
                    detections.add(detection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Return empty list on error
        }
        
        return detections;
    }
    
    private TensorImage preprocessImage(Bitmap bitmap) {
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
            .add(new ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .build();
        
        TensorImage tensorImage = new TensorImage(org.tensorflow.lite.DataType.UINT8);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);
        
        return tensorImage;
    }
    
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}

