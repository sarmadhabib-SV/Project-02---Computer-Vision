#!/bin/bash
# File Organization Script for Vision Assist Project

cd "$(dirname "$0")"

echo "Organizing project files..."

# Create directories if they don't exist
mkdir -p Documentation Proposal Training Submission Resources

# Move documentation files
echo "Moving documentation files..."
for file in DOCUMENTATION.md REQUIREMENTS_CHECKLIST.md INTERFACE_MOCKUPS.md \
            BUSINESS_MODEL.md USE_CASE_DIAGRAM.md COMPLETE_PROJECT_STATUS.md \
            FINAL_SUBMISSION_GUIDE.md QUICK_START.md RUN_INSTRUCTIONS.md \
            PROJECT_SUMMARY.md; do
    if [ -f "$file" ]; then
        mv "$file" Documentation/ 2>/dev/null && echo "  ✓ Moved $file"
    fi
done

# Move proposal files
echo "Moving proposal files..."
[ -f "CS 663 Project 2 Proposal - Final (1).docx" ] && \
    mv "CS 663 Project 2 Proposal - Final (1).docx" Proposal/ 2>/dev/null && \
    echo "  ✓ Moved proposal docx"
[ -f "proposal.txt" ] && mv "proposal.txt" Proposal/ 2>/dev/null && echo "  ✓ Moved proposal.txt"
[ -f "Project 2 Computer Vision Document.docx" ] && \
    mv "Project 2 Computer Vision Document.docx" Proposal/ 2>/dev/null && \
    echo "  ✓ Moved instructions docx"
[ -f "project_instructions.txt" ] && \
    mv "project_instructions.txt" Proposal/ 2>/dev/null && \
    echo "  ✓ Moved instructions txt"

# Move training files
echo "Moving training files..."
[ -f "efficientdet_lite0.tflite" ] && \
    mv "efficientdet_lite0.tflite" Training/ 2>/dev/null && \
    echo "  ✓ Moved model file"
[ -f "labels.txt" ] && mv "labels.txt" Training/ 2>/dev/null && echo "  ✓ Moved labels file"

echo ""
echo "Organization complete!"
echo ""
echo "Current structure:"
echo "  Documentation/ - $(ls Documentation/*.md 2>/dev/null | wc -l | tr -d ' ') files"
echo "  Proposal/ - $(ls Proposal/* 2>/dev/null | wc -l | tr -d ' ') files"
echo "  Training/ - $(ls Training/* 2>/dev/null | wc -l | tr -d ' ') files"





