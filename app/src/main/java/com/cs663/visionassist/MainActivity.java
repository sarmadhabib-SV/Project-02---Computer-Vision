package com.cs663.visionassist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    
    private PreviewView cameraPreview;
    private MaterialButton captureButton;
    private MaterialButton galleryButton;
    private ProgressBar progressBar;
    private TextView statusText;
    
    private ImageCapture imageCapture;
    private TextToSpeech textToSpeech;
    private ProcessCameraProvider cameraProvider;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeTextToSpeech();
        checkCameraPermission();
    }
    
    private void initializeViews() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        
        cameraPreview = findViewById(R.id.cameraPreview);
        captureButton = findViewById(R.id.captureButton);
        galleryButton = findViewById(R.id.galleryButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);
        
        captureButton.setOnClickListener(v -> captureImage());
        if (galleryButton != null) {
            galleryButton.setOnClickListener(v -> selectFromGallery());
        }
    }
    
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Language not supported, will use default
                }
            }
        });
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
                
                  imageCapture = new ImageCapture.Builder()
                      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                      .setTargetRotation(cameraPreview.getDisplay().getRotation())
                      .build();
                
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                
                Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture);
                
                // Speak initial guidance
                if (textToSpeech != null) {
                    textToSpeech.speak(getString(R.string.camera_ready), 
                        TextToSpeech.QUEUE_FLUSH, null, null);
                }
                
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void captureImage() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setProcessingState(true);
        
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                    Bitmap bitmap = imageProxyToBitmap(imageProxy);
                    imageProxy.close(); // Important: close the ImageProxy
                    if (bitmap != null) {
                        processImage(bitmap);
                    } else {
                        runOnUiThread(() -> {
                            setProcessingState(false);
                            Toast.makeText(MainActivity.this, 
                                "Failed to convert image", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    runOnUiThread(() -> {
                        setProcessingState(false);
                        Toast.makeText(MainActivity.this, 
                            "Error capturing image: " + exception.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
    }
    
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        // Convert ImageProxy to Bitmap
        android.media.Image image = imageProxy.getImage();
        if (image == null) {
            return null;
        }
        
        int format = image.getFormat();
        int width = image.getWidth();
        int height = image.getHeight();
        android.media.Image.Plane[] planes = image.getPlanes();
        
        // Safety check: ensure we have at least one plane
        if (planes == null || planes.length == 0) {
            return null;
        }
        
        // Handle YUV_420_888 format (most common for CameraX ImageCapture)
        // But only if we have 3 planes (Y, U, V)
        if (format == android.graphics.ImageFormat.YUV_420_888 && planes.length >= 3) {
            try {
                java.nio.ByteBuffer yBuffer = planes[0].getBuffer();
                java.nio.ByteBuffer uBuffer = planes[1].getBuffer();
                java.nio.ByteBuffer vBuffer = planes[2].getBuffer();
                
                int ySize = yBuffer.remaining();
                int uSize = uBuffer.remaining();
                int vSize = vBuffer.remaining();
                
                byte[] nv21 = new byte[ySize + uSize + vSize];
                
                yBuffer.get(nv21, 0, ySize);
                vBuffer.get(nv21, ySize, vSize);
                uBuffer.get(nv21, ySize + vSize, uSize);
                
                android.graphics.YuvImage yuvImage = new android.graphics.YuvImage(
                    nv21, android.graphics.ImageFormat.NV21, 
                    width, height, null);
                
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                yuvImage.compressToJpeg(
                    new android.graphics.Rect(0, 0, width, height), 
                    100, out);
                byte[] imageBytes = out.toByteArray();
                
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } catch (Exception e) {
                e.printStackTrace();
                // Fall through to alternative method
            }
        }
        
        // Alternative method: Handle JPEG format or single plane images
        // For ImageCapture, the image might be in JPEG format already
        if (format == android.graphics.ImageFormat.JPEG || planes.length == 1) {
            try {
                java.nio.ByteBuffer buffer = planes[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.rewind();
                buffer.get(bytes);
                
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Last resort: Create a bitmap and manually convert
        // This is a fallback - may not produce perfect results
        return createBitmapFromPlanes(image, planes, width, height);
    }
    
    private Bitmap createBitmapFromPlanes(android.media.Image image, 
                                         android.media.Image.Plane[] planes,
                                         int width, int height) {
        if (planes.length == 0) {
            return null;
        }
        
        // Try to create bitmap from available data
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        // For single plane formats, try to decode
        if (planes.length == 1) {
            java.nio.ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            
            if (pixelStride == 4) { // ARGB_8888
                buffer.rewind();
                bitmap.copyPixelsFromBuffer(buffer);
                return bitmap;
            }
        }
        
        // If we can't convert properly, return a placeholder
        // In production, you'd want proper YUV to RGB conversion
        return bitmap;
    }
    
    private void processImage(Bitmap bitmap) {
        if (bitmap == null) {
            setProcessingState(false);
            Toast.makeText(this, "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Process image asynchronously
        new Thread(() -> {
            try {
                ProcessingResult result = ImageProcessor.processImage(
                    MainActivity.this, bitmap);
                
                runOnUiThread(() -> {
                    setProcessingState(false);
                    
                    // Save processed bitmap to temporary file (has correct bounding box coordinates)
                    Bitmap processedBitmap = result.getProcessedBitmap();
                    Uri imageUri = saveBitmapToFile(processedBitmap);
                    if (imageUri != null) {
                        Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                        intent.putExtra("imageUri", imageUri.toString());
                        intent.putExtra("narration", result.getNarration());
                        intent.putExtra("detections", result.getDetectionsSummary());
                        intent.putExtra("detectedObjects", result.getDetectedObjectsJson());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, 
                            "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    setProcessingState(false);
                    Toast.makeText(MainActivity.this, 
                        "Error processing image: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void setProcessingState(boolean isProcessing) {
        progressBar.setVisibility(isProcessing ? View.VISIBLE : View.GONE);
        captureButton.setEnabled(!isProcessing);
        if (galleryButton != null) {
            galleryButton.setEnabled(!isProcessing);
        }
        statusText.setText(isProcessing ? R.string.processing : R.string.camera_ready);
        statusText.setVisibility(View.VISIBLE);
    }
    
    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    
                    if (bitmap != null) {
                        setProcessingState(true);
                        processImage(bitmap);
                    } else {
                        Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            // Create a temporary file in the app's cache directory
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "captured_image_" + System.currentTimeMillis() + ".jpg");
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            // Compress bitmap to reduce size
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            
            // Return file URI
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}

