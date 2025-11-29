# Training Folder

This folder contains the trained model files.

## 📄 Files

- **efficientdet_lite0.tflite** - Trained EfficientDet Lite0 model
- **labels.txt** - COCO dataset class labels (80 classes)

## 📋 Model Information

- **Model**: EfficientDet Lite0
- **Dataset**: COCO 2017 dataset
- **Architecture**: 
  - Backbone: EfficientNet-Lite0 for multi-scale visual feature extraction
  - Feature Fusion: BiFPN (Bidirectional Feature Pyramid Network) to fuse features from different resolutions
- **Training Configuration**:
  - Loss Function: Combined Focal Loss (classification) + Smooth L1 Loss (bounding box regression)
  - Optimizer: RMSProp with cosine learning-rate decay and warmup scheduling
  - Data Augmentation: Random flipping, resizing, color jitter for outdoor sidewalk environments
- **Model Specifications**:
  - Input Size: 320x320 pixels
  - Output: Object detections with bounding boxes
  - Classes: 80 COCO classes (person, car, bicycle, stop sign, bench, traffic light, etc.)
  - Format: TensorFlow Lite (.tflite) with mobile optimizations
    - Fused convolution layers
    - Activation pruning
- **Performance**: Achieves efficient real-time performance on Android devices with good accuracy for sidewalk-specific objects

## 🚀 Usage

These files need to be copied to the Android app's assets folder:

```bash
# From project root directory
mkdir -p app/src/main/assets
cp Training/efficientdet_lite0.tflite app/src/main/assets/
cp Training/labels.txt app/src/main/assets/
```

## ✅ Status

Model training is complete. Files are ready to use in the Android application.

---

**Note**: The model files are already in the correct location for the Android app. This folder serves as a backup/reference.





