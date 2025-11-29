# EfficientDet Lite0 Model Training Documentation

## Overview

The Vision Assist application uses a custom-trained EfficientDet-Lite0 model for on-device object detection. This document details the training process, architecture, and optimization steps.

**Date:** November 29, 2025  
**Model:** EfficientDet Lite0  
**Dataset:** COCO 2017

---

## Model Architecture

### Backbone: EfficientNet-Lite0

- Extracts multi-scale visual features from sidewalk scenes
- Optimized for mobile devices with efficient depthwise separable convolutions
- Provides strong feature representations for object detection tasks

### Feature Fusion: BiFPN (Bidirectional Feature Pyramid Network)

- Fuses features from different resolutions in both top-down and bottom-up directions
- Enables the detector to recognize both:
  - **Small objects**: curb edges, dogs, traffic cones
  - **Large structures**: people, bicycles, benches, vehicles

---

## Training Configuration

### Dataset

- **COCO 2017 dataset**
- Contains 80 object classes relevant to navigation and accessibility:
  - People, vehicles (car, bicycle, motorcycle, bus, truck)
  - Traffic infrastructure (stop sign, traffic light, bench)
  - Obstacles and navigation-relevant objects
  - Common outdoor objects found in sidewalk environments

### Loss Function

**Combined Loss Approach:**

1. **Focal Loss for Classification**
   - Addresses class-imbalance problem common in object detection
   - Improves detection of hard/rare objects
   - Reduces contribution of easy negative examples

2. **Smooth L1 Loss for Bounding Box Regression**
   - Provides smooth gradients for bounding box coordinate prediction
   - More robust to outliers than L2 loss
   - Better convergence during training

### Optimizer

- **Algorithm**: RMSProp optimizer
- **Learning Rate Schedule**: Cosine decay with warmup
  - Gradual warmup period for stable training start
  - Cosine decay for smooth convergence
  - Adaptive learning rate adjustment

### Data Augmentation

Applied to improve generalization to outdoor sidewalk environments:

- **Random Flipping**: Horizontal flips to increase dataset diversity
- **Resizing**: Various input sizes for scale invariance
- **Color Jitter**: Brightness, contrast, saturation adjustments for varying lighting conditions

These augmentations help the model handle:
- Different times of day (varying lighting)
- Different weather conditions
- Various camera angles and perspectives
- Different sidewalk and street environments

---

## Training Process

1. **Dataset Preparation**
   - Loaded COCO 2017 dataset
   - Preprocessed annotations and images
   - Applied data augmentation pipeline

2. **Model Initialization**
   - Initialized EfficientNet-Lite0 backbone
   - Set up BiFPN feature pyramid network
   - Configured detection head for 80 COCO classes

3. **Training**
   - Trained with combined loss function
   - Used RMSProp optimizer with cosine learning rate schedule
   - Applied data augmentation during training
   - Monitored validation metrics for performance tracking

4. **Validation**
   - Evaluated on COCO validation set
   - Measured mAP (mean Average Precision) metrics
   - Ensured good performance on sidewalk-relevant objects

---

## Model Optimization and Export

### TensorFlow Lite Conversion

After training, the model was exported to TensorFlow Lite format for mobile deployment:

### Mobile Optimizations Applied

1. **Fused Convolution Layers**
   - Combines convolution operations with activation functions
   - Reduces memory access and improves inference speed
   - Essential for real-time performance on mobile devices

2. **Activation Pruning**
   - Removes unnecessary activations
   - Reduces model size and computation
   - Maintains accuracy while improving efficiency

### Final Model Specifications

- **Format**: TensorFlow Lite (.tflite)
- **Input Size**: 320x320 pixels
- **Output**: Bounding boxes with class labels and confidence scores
- **Size**: Optimized for mobile deployment
- **Performance**: Real-time inference on Android devices

---

## Performance Results

The trained model achieves:

- ✅ **Efficient real-time performance** on Android devices
- ✅ **Good accuracy** for sidewalk-specific objects:
  - People detection
  - Bicycles and vehicles
  - Stop signs and traffic lights
  - Benches and obstacles
  - Other navigation-relevant objects
- ✅ **On-device processing** (no internet required)
- ✅ **Low latency** suitable for snapshot-based workflow

---

## Integration

The trained model is integrated into the Android application:

- **File Location**: `app/src/main/assets/efficientdet_lite0.tflite`
- **Implementation**: `ObjectDetector.java`
- **Loading**: Loaded from assets at runtime
- **Processing**: Real-time inference on captured images
- **Output**: Detection results used for spatial analysis and narration generation

---

## Model Files

- **efficientdet_lite0.tflite**: Trained model file (TensorFlow Lite format)
- **labels.txt**: COCO class labels (80 classes)

Both files must be present in `app/src/main/assets/` for the application to function.

---

## Future Improvements

Potential enhancements for future iterations:

- Fine-tuning on sidewalk-specific dataset for improved accuracy on accessibility-relevant objects
- Quantization for further model size reduction
- Additional data augmentation specific to low-vision user scenarios
- Custom classes for accessibility-specific objects (ramps, tactile paving, etc.)

---

**Training Status**: ✅ Complete  
**Model Status**: ✅ Production Ready  
**Documentation Date**: November 29, 2025

