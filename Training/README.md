# Training Folder

This folder contains the trained model files.

## 📄 Files

- **efficientdet_lite0.tflite** - Trained EfficientDet Lite0 model
- **labels.txt** - COCO dataset class labels (80 classes)

## 📋 Model Information

- **Model**: EfficientDet Lite0
- **Input Size**: 320x320 pixels
- **Output**: Object detections with bounding boxes
- **Classes**: 80 COCO classes (person, car, bicycle, etc.)
- **Format**: TensorFlow Lite (.tflite)

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

