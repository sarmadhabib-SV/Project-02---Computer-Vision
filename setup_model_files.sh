#!/bin/bash
# Setup script to copy model files to assets folder

cd "$(dirname "$0")"

echo "Setting up model files..."
echo ""

# Create assets folder
mkdir -p app/src/main/assets

# Find and copy model files
if [ -f efficientdet_lite0.tflite ]; then
    cp efficientdet_lite0.tflite app/src/main/assets/
    echo "✓ Copied efficientdet_lite0.tflite from root"
elif [ -f Training/efficientdet_lite0.tflite ]; then
    cp Training/efficientdet_lite0.tflite app/src/main/assets/
    echo "✓ Copied efficientdet_lite0.tflite from Training/"
else
    echo "✗ Model file not found!"
fi

if [ -f labels.txt ]; then
    cp labels.txt app/src/main/assets/
    echo "✓ Copied labels.txt from root"
elif [ -f Training/labels.txt ]; then
    cp Training/labels.txt app/src/main/assets/
    echo "✓ Copied labels.txt from Training/"
else
    echo "✗ Labels file not found!"
fi

echo ""
echo "Files in assets folder:"
ls -lh app/src/main/assets/ 2>/dev/null || echo "Assets folder is empty"

echo ""
echo "✅ Setup complete! Ready to run in Android Studio."





