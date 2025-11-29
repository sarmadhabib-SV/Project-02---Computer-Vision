#!/bin/bash
# Complete File Organization Script
# This script organizes ALL extra files into their respective folders

cd "$(dirname "$0")"

echo "=========================================="
echo "  Vision Assist - File Organization"
echo "=========================================="
echo ""

# Create directories
mkdir -p Documentation Proposal Training Submission Resources

# Counter
moved=0
failed=0

# Function to move file with error handling
move_file() {
    local src="$1"
    local dst="$2"
    local name="$3"
    
    if [ -f "$src" ] && [ ! -f "$dst" ]; then
        if mv "$src" "$dst" 2>/dev/null; then
            echo "  ✓ Moved: $name"
            ((moved++))
            return 0
        else
            echo "  ✗ Failed: $name (file may be in use)"
            ((failed++))
            return 1
        fi
    elif [ -f "$dst" ]; then
        echo "  ⊙ Already exists: $name"
        return 0
    else
        echo "  ⊙ Not found: $name"
        return 0
    fi
}

echo "📚 Moving Documentation files..."
move_file "DOCUMENTATION.md" "Documentation/DOCUMENTATION.md" "DOCUMENTATION.md"
move_file "REQUIREMENTS_CHECKLIST.md" "Documentation/REQUIREMENTS_CHECKLIST.md" "REQUIREMENTS_CHECKLIST.md"
move_file "INTERFACE_MOCKUPS.md" "Documentation/INTERFACE_MOCKUPS.md" "INTERFACE_MOCKUPS.md"
move_file "BUSINESS_MODEL.md" "Documentation/BUSINESS_MODEL.md" "BUSINESS_MODEL.md"
move_file "USE_CASE_DIAGRAM.md" "Documentation/USE_CASE_DIAGRAM.md" "USE_CASE_DIAGRAM.md"
move_file "COMPLETE_PROJECT_STATUS.md" "Documentation/COMPLETE_PROJECT_STATUS.md" "COMPLETE_PROJECT_STATUS.md"
move_file "FINAL_SUBMISSION_GUIDE.md" "Documentation/FINAL_SUBMISSION_GUIDE.md" "FINAL_SUBMISSION_GUIDE.md"
move_file "QUICK_START.md" "Documentation/QUICK_START.md" "QUICK_START.md"
move_file "RUN_INSTRUCTIONS.md" "Documentation/RUN_INSTRUCTIONS.md" "RUN_INSTRUCTIONS.md"
move_file "PROJECT_SUMMARY.md" "Documentation/PROJECT_SUMMARY.md" "PROJECT_SUMMARY.md"
move_file "PROJECT_STRUCTURE.md" "Documentation/PROJECT_STRUCTURE.md" "PROJECT_STRUCTURE.md"
move_file "ORGANIZATION_GUIDE.md" "Documentation/ORGANIZATION_GUIDE.md" "ORGANIZATION_GUIDE.md"

echo ""
echo "📄 Moving Proposal files..."
move_file "CS 663 Project 2 Proposal - Final (1).docx" "Proposal/CS 663 Project 2 Proposal - Final (1).docx" "Proposal (docx)"
move_file "proposal.txt" "Proposal/proposal.txt" "proposal.txt"
move_file "Project 2 Computer Vision Document.docx" "Proposal/Project 2 Computer Vision Document.docx" "Instructions (docx)"
move_file "project_instructions.txt" "Proposal/project_instructions.txt" "project_instructions.txt"

echo ""
echo "🤖 Moving Training files..."
move_file "efficientdet_lite0.tflite" "Training/efficientdet_lite0.tflite" "Model file"
move_file "labels.txt" "Training/labels.txt" "Labels file"

echo ""
echo "🔧 Moving Scripts..."
move_file "organize_files.sh" "Resources/organize_files.sh" "organize_files.sh"

echo ""
echo "=========================================="
echo "  Summary"
echo "=========================================="
echo "  Files moved: $moved"
echo "  Files failed: $failed"
echo ""
echo "  Documentation/: $(ls Documentation/*.md 2>/dev/null | wc -l | tr -d ' ') files"
echo "  Proposal/: $(ls Proposal/* 2>/dev/null | wc -l | tr -d ' ') files"
echo "  Training/: $(ls Training/* 2>/dev/null | wc -l | tr -d ' ') files"
echo ""
echo "=========================================="

if [ $failed -gt 0 ]; then
    echo ""
    echo "⚠️  Some files could not be moved (may be open in an editor)."
    echo "   Close any open files and run this script again."
fi

echo ""
echo "✅ Organization complete!"
echo ""





