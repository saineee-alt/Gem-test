# Islet — Dynamic Island Capsule for Android

Islet brings an interactive, capsule-shaped overlay to Android, anchored near the front camera punch-hole cutout. Built 100% with Kotlin and Jetpack Compose, it morphs between a slim idle pill and rich interactive cards driven by real system events and user gestures.

---

## 1. Platform & Architectural Constraints

> **Platform Constraint**: Android OS security does not allow any third-party app to draw literally inside the physical status bar or inside the camera cutout glass itself. Instead, Islet implements a hardware-hugging `TYPE_APPLICATION_OVERLAY` window anchored directly to the top edge of the display using `WindowInsets.displayCutout` and customizable X/Y offset sliders. This ensures a seamless visual merger with center or corner camera holes without modifying system firmware.

---

## 2. Required Permissions & Setup

To experience Islet across all your applications, grant the following permissions:

1. **Display over other apps (`SYSTEM_ALERT_WINDOW`)**:
   - *Why*: Allows the capsule pill to float on top of other running apps and home screens.
   - *How to grant*: Tap the shield icon in the app's top bar or the prompt card on the Home tab, which takes you directly to Android's "Display over other apps" toggle for Islet.
2. **Notification Listener Access (`BIND_NOTIFICATION_LISTENER_SERVICE`)**:
   - *Why*: Enables Islet to read incoming messages from allowlisted apps (e.g. WhatsApp, Telegram, Google Messages) and surface active media playback controls with album art.
   - *How to grant*: Enabled via Settings → Apps & notifications → Special app access → Device & app notifications.
3. **Post Notifications (`POST_NOTIFICATIONS`)**:
   - *Why*: Keeps the Android foreground service running reliably without background OS termination.

---

## 3. How to Test Each Dynamic Card

Islet supports both **real hardware triggers** and an **interactive Test Studio** built directly into the app:

### Method A: Real Device Signals
- **Charging Card**: Plug in a USB-C charger. The capsule instantly expands to show live voltage (e.g. `4.1 V`), battery temperature, current draw (`A`), wattage ($V \times A$), and estimated minutes to full.
- **Media Player Card**: Play a track in Spotify, YouTube Music, or any audio player. Islet displays a 4-bar equalizer in the collapsed pill and an animated squiggly sinusoidal seeker with playback controls in the expanded card.
- **Notification & Inline Reply Card**: Receive an incoming message from an enabled messaging app. Tap "Reply" to type inline directly from the capsule and submit.
- **Quick-Toggle Mini Pills**: Toggle silent/vibrate mode or connect a Bluetooth headset. The capsule flashes a compact status pill for ~2 seconds.

### Method B: In-App Test Studio (No Hardware Needed)
Switch to the **Test Lab** tab in the bottom navigation bar to trigger any card on demand with one tap:
- Tap **Fast Charging** to see the 2×2 charging metrics grid.
- Tap **Media Player** to interact with the squiggly waveform scrubber, play/pause, and track skipping.
- Tap **Incoming WhatsApp Message** to test the inline reply input field.
- Tap **Package Install Progress** to preview animated downloading bars.

---

## 4. Gestures & Customization

- **Tap collapsed**: Expand into full card.
- **Tap expanded (outside buttons)**: Collapse back to pill.
- **Swipe Down on collapsed**: Expand card.
- **Swipe Up on expanded**: Dismiss to idle.
- **Long Press**: Open Islet settings companion app.
- **Style Tab**: Calibrate vertical notch offset, horizontal offset, pill width/height, opacity, border width, and corner radius.

---

## 5. GitHub Actions (Manual APK Builds)

A manual workflow is configured in `.github/workflows/build-apk.yml` using `workflow_dispatch`.

### How to trigger:
1. Navigate to the **Actions** tab in your GitHub repository.
2. Select the **Build APKs** workflow on the left.
3. Click **Run workflow**.
4. Choose the build type:
   - `all` (produces both Debug and Release APKs)
   - `debug` (Debug APK only)
   - `release` (Release APK only)
5. Once the run finishes, scroll down to **Artifacts** to download `islet-debug-apk` and `islet-release-apk`.

