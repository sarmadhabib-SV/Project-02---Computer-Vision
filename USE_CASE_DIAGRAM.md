# Use Case Diagram and Description - Vision Assist

## Use Case Diagram

```
                    Vision Assist System
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   Low-Vision User    Caregiver/Family   Rehabilitation Professional
        │                  │                  │
        │                  │                  │
    ┌───┴──────────────────┴──────────────────┴───┐
    │                                                │
    │  Primary Use Cases                            │
    │                                                │
    │  ┌──────────────────────────────────────┐    │
    │  │ 1. Launch Application                │    │
    │  └──────────────────────────────────────┘    │
    │              │                                │
    │              ▼                                │
    │  ┌──────────────────────────────────────┐    │
    │  │ 2. Capture Image                     │    │
    │  │    - Point camera at scene           │    │
    │  │    - Tap capture button             │    │
    │  └──────────────────────────────────────┘    │
    │              │                                │
    │              ▼                                │
    │  ┌──────────────────────────────────────┐    │
    │  │ 3. Process Image                     │    │
    │  │    - Object detection                 │    │
    │  │    - Text recognition (OCR)          │    │
    │  │    - Spatial analysis                │    │
    │  └──────────────────────────────────────┘    │
    │              │                                │
    │              ▼                                │
    │  ┌──────────────────────────────────────┐    │
    │  │ 4. View Results                      │    │
    │  │    - See image with bounding boxes  │    │
    │  │    - Read narration text             │    │
    │  │    - View detection summary           │    │
    │  └──────────────────────────────────────┘    │
    │              │                                │
    │              ▼                                │
    │  ┌──────────────────────────────────────┐    │
    │  │ 5. Listen to Narration               │    │
    │  │    - Auto-plays on results screen   │    │
    │  │    - Text-to-Speech output          │    │
    │  └──────────────────────────────────────┘    │
    │              │                                │
    │              ├─────────────────┐             │
    │              │                 │             │
    │              ▼                 ▼             │
    │  ┌──────────────────┐  ┌──────────────────┐ │
    │  │ 6a. Play Again   │  │ 6b. Retake Photo│ │
    │  └──────────────────┘  └──────────────────┘ │
    │                                                │
    └────────────────────────────────────────────────┘
```

## Use Case Descriptions

### Use Case 1: Launch Application

**Actor**: Low-Vision User, Caregiver, or Professional

**Preconditions**:
- Android device with Vision Assist installed
- Camera permission granted
- Device has sufficient battery

**Main Flow**:
1. User opens Vision Assist from app drawer
2. App requests camera permission (if not granted)
3. Camera viewfinder appears
4. Text-to-Speech announces: "Camera ready. Double tap to take a picture."

**Postconditions**:
- Camera is active and ready
- User can see camera preview (if low-vision, not blind)
- App is ready to capture images

**Alternative Flows**:
- **1a. Permission Denied**: App shows message requesting camera permission
- **1b. Camera Unavailable**: App shows error message

---

### Use Case 2: Capture Image

**Actor**: Low-Vision User

**Preconditions**:
- App is launched and camera is ready
- User has positioned device to view desired scene

**Main Flow**:
1. User points camera at scene (sidewalk, street, room, etc.)
2. User taps the large camera button
3. Image is captured
4. Processing indicator appears
5. Status text shows "Processing..."

**Postconditions**:
- Image is captured and stored temporarily
- Processing begins automatically
- User sees processing feedback

**Alternative Flows**:
- **2a. Camera Error**: Error message displayed, user can retry
- **2b. Low Light**: Warning message, but processing continues

---

### Use Case 3: Process Image

**Actor**: System (Automatic)

**Preconditions**:
- Image has been captured
- Model files are available in assets

**Main Flow**:
1. Image is resized for processing (max 640px)
2. Object detection runs (TensorFlow Lite)
   - EfficientDet model processes image
   - Detects objects with bounding boxes
   - Filters by confidence threshold (0.5)
3. OCR runs (ML Kit)
   - Extracts text from image
   - Filters for navigation-relevant text
4. Spatial analysis runs
   - Determines side (left/center/right)
   - Determines distance (near/mid/far)
5. Narration is generated
   - Prioritizes near objects and hazards
   - Creates concise description (≤12 words)
   - Includes spatial information

**Postconditions**:
- Detections are processed and analyzed
- Narration is generated
- Results are ready for display

**Alternative Flows**:
- **3a. No Detections**: Narration says "No objects detected"
- **3b. Processing Error**: Fallback to basic detection

---

### Use Case 4: View Results

**Actor**: Low-Vision User

**Preconditions**:
- Image processing is complete
- Results are available

**Main Flow**:
1. Results screen appears
2. Captured image is displayed with colored bounding boxes
   - Red: Near objects (hazards)
   - Yellow: Mid-distance objects
   - Green: Far objects
3. Narration text is displayed in large font
4. Detection summary is shown below narration
5. Control buttons are visible (Play Again, Retake Photo)

**Postconditions**:
- User can see results
- User can interact with controls
- Narration is ready to play

**Alternative Flows**:
- **4a. No Detections**: Shows "No objects detected" message
- **4b. Image Load Error**: Shows error message

---

### Use Case 5: Listen to Narration

**Actor**: Low-Vision User

**Preconditions**:
- Results screen is displayed
- Text-to-Speech is initialized

**Main Flow**:
1. Narration automatically plays when results screen appears
2. Text-to-Speech reads the narration text
3. User listens to description of detected objects and their locations

**Postconditions**:
- User has heard the narration
- User understands what objects are detected and where

**Alternative Flows**:
- **5a. TTS Not Available**: Text remains visible, no audio
- **5b. User Interrupts**: Narration stops, can replay

---

### Use Case 6a: Play Again

**Actor**: Low-Vision User

**Preconditions**:
- Results screen is displayed
- Narration has been played at least once

**Main Flow**:
1. User taps "Play Again" button
2. Narration replays via Text-to-Speech
3. User listens to narration again

**Postconditions**:
- Narration has been replayed
- User can continue to interact with app

---

### Use Case 6b: Retake Photo

**Actor**: Low-Vision User

**Preconditions**:
- Results screen is displayed

**Main Flow**:
1. User taps "Retake Photo" button
2. App returns to camera screen
3. Camera viewfinder is active
4. User can capture a new image

**Postconditions**:
- User is back at camera screen
- Previous results are cleared
- Ready for new capture

---

## Scenario Examples

### Scenario 1: Navigating Sidewalk

**User**: Low-vision person walking on sidewalk

**Steps**:
1. Launch app
2. Point camera ahead while walking
3. Capture image
4. Listen: "Cone ahead center, near. Bench left, mid."
5. Adjust path to avoid cone
6. Retake photo to check next section

**Expected Outcome**: User safely navigates around obstacles

---

### Scenario 2: Reading Street Sign

**User**: Low-vision person at intersection

**Steps**:
1. Launch app
2. Point camera at street sign
3. Capture image
4. Listen: "Sign reads: STOP. Person detected right, near."
5. Understands sign and nearby person
6. Retake to check other signs

**Expected Outcome**: User reads sign and is aware of surroundings

---

### Scenario 3: Finding Objects in Room

**User**: Low-vision person looking for item

**Steps**:
1. Launch app
2. Point camera around room
3. Capture image
4. Listen: "Chair detected center, near. Table left, mid."
5. Locates furniture
6. Retake to scan other areas

**Expected Outcome**: User identifies objects and their locations

---

## System Boundaries

### Included in System
- Camera capture
- Image processing
- Object detection
- Text recognition
- Spatial analysis
- Narration generation
- Text-to-Speech
- Results display

### Excluded from System
- Video streaming (snapshot-based only)
- Cloud processing (optional, extra credit)
- Navigation routing
- GPS integration
- Social features
- Data storage/cloud sync

---

## Actor Descriptions

### Primary Actor: Low-Vision User
- **Characteristics**: Has vision impairment (20/70 to 20/200)
- **Goals**: Navigate safely, identify objects, read text
- **Skills**: Basic smartphone usage
- **Constraints**: Limited vision, may need voice guidance

### Secondary Actor: Caregiver/Family
- **Characteristics**: Assists low-vision person
- **Goals**: Help set up app, ensure proper usage
- **Skills**: Smartphone proficiency
- **Constraints**: May not be present during use

### Secondary Actor: Rehabilitation Professional
- **Characteristics**: Vision rehabilitation specialist
- **Goals**: Train users, evaluate app effectiveness
- **Skills**: Professional expertise in assistive technology
- **Constraints**: May need training materials

---

This use case diagram and description provides a comprehensive view of how users interact with Vision Assist to accomplish their goals of safe navigation and object identification.





