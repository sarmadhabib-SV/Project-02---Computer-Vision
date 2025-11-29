# Vision Assist - Real-Time Object and Text Detection for Accessibility

## 📱 Project Overview

Vision Assist is a complete Android application designed to assist low-vision or blind users by detecting obstacles, reading text, and providing concise spoken feedback. The system combines on-device real-time computer vision using TensorFlow Lite with ML Kit for text recognition.

## 🚀 Quick Start - RUN THE PROJECT

### Step 1: Copy Model Files (IMPORTANT!)
```bash
cd "/Users/sarmadhabib/Documents/Project 02 - Computer Vision"
mkdir -p app/src/main/assets
cp efficientdet_lite0.tflite app/src/main/assets/
cp labels.txt app/src/main/assets/
```

**OR if files are in Training folder:**
```bash
cp Training/efficientdet_lite0.tflite app/src/main/assets/
cp Training/labels.txt app/src/main/assets/
```

### Step 2: Open in Android Studio
1. Launch **Android Studio**
2. **File → Open**
3. Select this project folder
4. Wait for Gradle sync (5-10 minutes first time)

### Step 3: Run!
1. Connect Android device (API 24+) with USB debugging enabled
2. Click green **▶ Run** button
3. Grant camera permission
4. App launches and ready to use!

## 📁 Project Structure

```
Project 02 - Computer Vision/
├── app/                          # Android Application
│   └── src/main/
│       ├── assets/               # ⚠️ Model files go here!
│       ├── java/                 # Java source code
│       └── res/                  # Resources
├── Documentation/                # All documentation
├── Proposal/                     # Proposal files
├── Training/                     # Model files (backup)
└── Build files (root)
```

## ⚠️ CRITICAL: Model Files Location

**YES, labels.txt goes with the tflite file!**

Both must be in: `app/src/main/assets/`
- `efficientdet_lite0.tflite` - The trained model
- `labels.txt` - Class labels (80 COCO classes)

The code loads them from assets:
- `ObjectDetector.java` uses `FileUtil.loadMappedFile(context, "efficientdet_lite0.tflite")`
- `ObjectDetector.java` uses `FileUtil.loadLabels(context, "labels.txt")`

## 📚 Documentation

- **RUN_PROJECT.md** - Detailed run instructions
- **QUICK_RUN.md** - Quick start guide
- **Documentation/DOCUMENTATION.md** - Complete project documentation

## 🎯 Features

- ✅ Real-time object detection (EfficientDet Lite0)
- ✅ Text recognition (ML Kit OCR)
- ✅ Spatial awareness (left/center/right, near/mid/far)
- ✅ Text-to-Speech narration
- ✅ Accessible UI design
- ✅ **Gemini API integration** (Optional - Extra Credit Feature)
  - Enhanced natural-language narrations
  - Automatic fallback to local narration
  - See `GEMINI_SETUP.md` for setup instructions

## 🔧 Technical Stack

- **Language**: Java
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **ML Framework**: TensorFlow Lite 2.14.0
- **OCR**: ML Kit Text Recognition 16.0.0

## ✅ Project Status

✅ All code complete  
✅ All documentation complete  
✅ Ready to run!  

**Next Step**: Copy model files to assets and open in Android Studio!

---

**Status**: ✅ Ready to run! See RUN_PROJECT.md for detailed instructions.
