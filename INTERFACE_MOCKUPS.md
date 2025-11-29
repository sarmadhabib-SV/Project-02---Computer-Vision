# Interface Mockups - Vision Assist

## Design Philosophy
The interface is designed with accessibility as the primary concern, featuring:
- Large touch targets (minimum 48dp)
- High contrast colors (black text on white background)
- Minimal cognitive load
- Voice guidance for all key actions
- Single-tap interactions

## Screen 1: Capture Screen (MainActivity)

```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│                                     │
│      [Camera Preview View]          │
│      (Live camera feed)             │
│                                     │
│                                     │
│                                     │
│                                     │
│                                     │
│                                     │
│                                     │
│                                     │
│                                     │
│          ┌───────────┐              │
│          │   📷      │              │
│          │  Take     │              │
│          │ Picture   │              │
│          └───────────┘              │
│                                     │
└─────────────────────────────────────┘
```

**Elements:**
- **Camera Preview**: Full-screen camera viewfinder
- **Capture Button**: Large circular button (120dp) with camera emoji
- **Status Text** (optional): "Camera ready. Double tap to take a picture."

**User Actions:**
- Tap camera button to capture image
- App automatically processes and navigates to results

## Screen 2: Results Screen (ResultsActivity)

```
┌─────────────────────────────────────┐
│                                     │
│  [Captured Image with Bounding     │
│   Boxes - Red/Yellow/Green]        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Person detected center,    │   │
│  │  near. Car left, mid.       │   │
│  │                             │   │
│  │  Objects: person (center,   │   │
│  │  near) • car (left, mid)    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌──────────┐    ┌──────────┐     │
│  │ Play      │    │ Retake   │     │
│  │ Again     │    │ Photo    │     │
│  └──────────┘    └──────────┘     │
│                                     │
└─────────────────────────────────────┘
```

**Elements:**
- **Captured Image**: Shows original image with colored bounding boxes
  - Red boxes: Near objects (hazards)
  - Yellow boxes: Mid-distance objects
  - Green boxes: Far objects
- **Narration Card**: 
  - Large text (19sp) showing narration
  - Detection summary below
- **Control Buttons**:
  - "Play Again": Replays narration
  - "Retake Photo": Returns to camera screen

**User Actions:**
- Narration automatically plays on screen load
- Tap "Play Again" to replay narration
- Tap "Retake Photo" to capture another image

## Visual Design Specifications

### Colors
- **Background**: White (#FFFFFF)
- **Text**: Black (#000000)
- **Primary Button**: Purple (#6200EE)
- **Bounding Boxes**:
  - Near: Red (#FF0000)
  - Mid: Yellow (#FFFF00)
  - Far: Green (#00FF00)

### Typography
- **Narration Text**: 19sp, Bold, Black
- **Summary Text**: 14sp, Regular, Black
- **Button Text**: 16sp, Medium, White

### Spacing
- **Card Padding**: 16dp
- **Button Margin**: 8dp
- **Button Height**: 48dp minimum

### Accessibility Features
- **Touch Targets**: Minimum 48dp x 48dp
- **Contrast Ratio**: 4.5:1 minimum (WCAG AA)
- **Voice Guidance**: Text-to-Speech for all actions
- **Large Text Support**: Scalable fonts

## User Flow

```
[App Launch]
    ↓
[Camera Screen]
    ↓
[User Taps Camera Button]
    ↓
[Processing...] (1-3 seconds)
    ↓
[Results Screen]
    ↓
[Narration Auto-Plays]
    ↓
[User Options:]
    ├─ [Play Again] → Replay narration
    └─ [Retake Photo] → Return to Camera Screen
```

## Interaction States

### Camera Screen
- **Idle**: Camera preview active, button enabled
- **Capturing**: Button disabled, processing indicator shown
- **Error**: Toast message displayed, button re-enabled

### Results Screen
- **Loading**: Image displayed, narration text shows "Processing..."
- **Ready**: Full results displayed, narration auto-plays
- **Playing**: Narration audio active
- **Idle**: Ready for user interaction

## Responsive Design

The interface adapts to different screen sizes:
- **Small Screens** (< 5"): Compact layout, smaller margins
- **Medium Screens** (5-7"): Standard layout
- **Large Screens** (> 7"): Expanded layout with more spacing

## Accessibility Considerations

1. **Screen Readers**: All elements have content descriptions
2. **Voice Control**: Large buttons easy to target
3. **High Contrast**: Black on white for maximum readability
4. **Minimal Text**: Relies on icons and voice for guidance
5. **Single-Handed Use**: All controls within thumb reach

---

**Note**: These mockups represent the final implementation. The actual app follows these designs with Material Design components for consistency and accessibility.





