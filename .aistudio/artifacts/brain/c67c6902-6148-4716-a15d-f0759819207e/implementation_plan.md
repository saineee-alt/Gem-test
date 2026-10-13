# Islet — Dynamic Island Capsule Overlay & Companion Hub

Islet brings a fluid, responsive capsule overlay anchored to the front camera cutout on Android. It transitions smoothly between a subtle idle notch hug, a collapsed pill with glanceable device telemetry, and rich expanded cards for battery charging stats, live media controls with a squiggly scrubber, package downloads, and actionable notifications.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed through Phase 1 user alignment:
> - **Cutout Positioning**: Calibrated for a center punch-hole camera by default, accompanied by live X/Y offset sliders and size controls in the Style settings.
> - **Testing & Simulation Mode**: A built-in interactive Test Studio is included directly inside the app, letting users trigger, preview, and experiment with Charging, Media, Download, Notification, and Mini Capsule states instantly without needing external hardware signals.
> - **Idle State**: Displays a small, refined pill resting right against the camera cutout, ready to spring open upon incoming events or touch gestures.

---

### 1. Overview & Core Concept

- **What It Does**: Renders a floating, hardware-anchored capsule window using Android `WindowManager` with a Compose runtime. The capsule expands into interactive cards upon tap, swipe, or system events (charger connection, media playback, incoming notifications, Bluetooth connections), then springs back into a collapsed pill.
- **Target Audience / Persona**: Android users desiring contextual, glanceable multitasking and dynamic island-style alerts that respect native Material You system theming.
- **Key Value**: Delivers zero-clutter situational awareness without leaving current apps, while providing full control over gestures, event triggers, and cutout placement.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Onboarding & Permission Hub**: Clean Material 3 setup checklist for granting Display Over Other Apps (`SYSTEM_ALERT_WINDOW`), Notification Listener access, and Foreground Service permissions with direct system intent deep-links.
2. **Main Navigation Hub ("Islet")**:
   - **Capsule Tab (Home)**: Service status toggle hero card, quick launch / test banner, and 4 elevated category cards with tonal icon clusters (*Mini Capsule Events*, *Dynamic Cards*, *Gestures & Cards*, *Notifications*).
   - **Style Tab**: Real-time interactive positioning calibration canvas (offset X/Y, scale, width, height, pill opacity, corner radius, and floating vs. notch-pinned modes).
   - **Settings Tab**: Advanced behavior toggles, lock-screen visibility, auto-collapse duration slider, and the interactive Test Studio to fire any card scenario on demand.
3. **Capsule Interaction States**:
   - **Idle Pill**: Subtle stadium shape hugging the camera lens.
   - **Collapsed State**: Split pill displaying live indicators (e.g., green bolt + battery %, or album art + animated waveform).
   - **Expanded Card**: Spring-morphs downward with spring physics:
     - *Charging*: Circular battery ring, 2×2 stats grid (time-to-full, voltage, temperature, current draw, wattage $V \times A$).
     - *Media*: Album art, title/artist, animated wavy progress scrubber, elapsed/remaining time, full transport controls (shuffle, prev, play/pause, next, favorite).
     - *Download*: App icon, status text, animated progress bar with percentage and estimated time remaining.
     - *Notification*: Sender avatar, app-specific icon badge, sender title, message preview, and inline "Reply" pill (with inline text field) plus "Mark as read".
     - *Mini Capsules*: Flash alerts (1.5–2s) for mute/unmute, Bluetooth headset connected (with battery %), Wi-Fi toggle, and flashlight.

#### Visual Identity & Theme
- **Pure Material You Dynamic Colors**: No hardcoded hex palettes. Colors strictly derive from `dynamicLightColorScheme` / `dynamicDarkColorScheme` (falling back to standard M3 palettes on older versions). The capsule automatically updates its palette if the system wallpaper or dark theme changes.
  - *Charging*: `colorScheme.tertiary` and `tertiaryContainer`
  - *Media*: `colorScheme.primary` and `primaryContainer`
  - *Downloads*: `colorScheme.secondary` and `secondaryContainer`
  - *Notifications*: Sender brand icon with `colorScheme.primary` glow
  - *Off / Error / Mute*: `colorScheme.error` and `errorContainer`
- **Typography & Elevation**:
  - Capsule text uses `typography.labelLarge` and `typography.titleMedium` with tight, legible letter spacing.
  - Collapsed pill uses `surfaceContainerHighest` and `inverseSurface` elevation for contrast over arbitrary wallpapers.
  - Expanded card uses `colorScheme.surface` with smooth 28dp rounded corners.
- **Motion Spec**:
  - `expand`: `spring(dampingRatio = 0.7f, stiffness = 400f)` shape morph synchronizing width, height, and corner radius.
  - `collapse`: snappy spring with staggered content crossfade to prevent clipped text.
  - Scrubber/Waveform: Continuous sinusoidal Bezier wave calculation in Compose Canvas.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: ComposeView in WindowManager with Custom LifecycleOwner**
  - *Chosen Approach*: Embed a Jetpack Compose `ComposeView` into a top-anchored `TYPE_APPLICATION_OVERLAY` window hosted by `CapsuleOverlayService`, bound to a dedicated synthetic `LifecycleOwner` and `SavedStateRegistryOwner`.
  - *Why*: Enables 100% Jetpack Compose UI, animations, gestures, and dynamic theming on top of all other apps without traditional XML RemoteViews limitations.
  - *Alternatives Considered*: Custom View drawing via Canvas (loses Compose state model and animation primitives).
- **Decision 2: Universal Testing Studio Inside Companion App**
  - *Chosen Approach*: Pair actual system monitors (`BatteryManager` broadcast receiver, `MediaSessionManager`, `NotificationListenerService`) with an in-app "Test Lab" that can inject synthetic events into the central `CapsuleStateHolder`.
  - *Why*: Allows immediate visual verification inside emulators, previews, and restricted environments where physical headphone/charger hardware events cannot be triggered easily.
- **Decision 3: Jetpack DataStore (Preferences) for Settings**
  - *Chosen Approach*: Reactive `PreferencesDataStore` providing `Flow<CapsuleSettings>` consumed by both MainActivity and the background overlay service.
  - *Why*: Lightweight, thread-safe asynchronous disk persistence without SQLite overhead for key-value preferences.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       System Signals                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────┐  │
│  │  BatteryMonitor  │  │   MediaMonitor   │  │ Test Lab  │  │
│  └─────────┬────────┘  └────────┬─────────┘  └─────┬─────┘  │
│            │                    │                  │        │
│            ▼                    ▼                  ▼        │
│         ┌──────────────────────────────────────────────┐    │
│         │         CapsuleStateHolder (StateFlow)       │    │
│         └───────────────────────┬──────────────────────┘    │
└─────────────────────────────────┼───────────────────────────┘
                                  │
          ┌───────────────────────┴───────────────────────┐
          ▼                                               ▼
┌───────────────────────────────┐     ┌───────────────────────────────┐
│     CapsuleOverlayService     │     │      MainActivity (App)       │
│  ┌─────────────────────────┐  │     │  ┌─────────────────────────┐  │
│  │ WindowManager (Overlay) │  │     │  │ NavigationBar (3 Tabs)  │  │
│  └────────────┬────────────┘  │     │  │ - Capsule Home Hub      │  │
│               ▼               │     │  │ - Appearance & Styles   │  │
│  ┌─────────────────────────┐  │     │  │ - Advanced & Test Lab   │  │
│  │ ComposeView (CapsuleUI) │  │     │  └─────────────────────────┘  │
│  │ - Idle / Collapsed Pill │  │     │  ┌─────────────────────────┐  │
│  │ - Expanded Cards (5)    │  │     │  │ Permission Onboarding   │  │
│  └─────────────────────────┘  │     │  └─────────────────────────┘  │
└───────────────────────────────┘     └───────────────┬───────────────┘
                                                      │
                                                      ▼
                                       ┌─────────────────────────────┐
                                       │   CapsulePreferencesStore   │
                                       └─────────────────────────────┘
```

#### State & Data Models
- **`CapsuleUiState`**:
  - `Idle`: Minimal camera hug dot/pill.
  - `Collapsed(card: CapsuleCardData)`: Compact pill with leading/trailing glyphs.
  - `Expanded(card: CapsuleCardData)`: Detailed card with interactive controls.
- **`CapsuleCardData`**: Sealed class covering `ChargingCard`, `MediaCard`, `DownloadCard`, `MessageCard`, and `MiniQuickToggle`.
- **`CapsuleSettings`**: Preferences for overlay enabled, cutout X/Y offset, scale, opacity, auto-collapse timeout (seconds), card toggles, and gesture bindings.

#### Interactive Component Mapping
- **Overlay Gesture Layer**: `Modifier.pointerInput` captures tap (expand), tap-away (collapse), swipe-down (expand), swipe-up (dismiss), and long-press (open app).
- **Settings Sliders**: Real-time adjustment of pill dimensions, position offset, and opacity with instant preview feedback.
- **Media Controller**: Seekbar scrubbing via touch drag on the wavy canvas, play/pause state toggle, and track change buttons.
- **Notification Inline Reply**: Text field inside the expanded message card with send action.
