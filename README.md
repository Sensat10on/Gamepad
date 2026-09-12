# BlueTooth GamePad & Mouse

## Current features

- Standard gamepad, **Android gamepad**, 8Bit, Sega six-button, and mouse/touchpad layouts.
- The two gamepad profiles carry the same axes and triggers but order the buttons for different hosts: **Standard** matches physical-pad/DirectInput numbering (Windows and most host software), **Android gamepad** matches the positional numbering the Linux kernel assigns, so Select/Start/Home/L3/R3 arrive as the `BUTTON_*` codes their labels promise.
- Face-button glyphs switch between A B X Y and ○ □ ✕ △ in **Appearance** — a cosmetic option, not a separate controller profile.
- Vector controller skins, one per layout: **Modern** (rounded shell with grips, glossy buttons and sticks), **Sega six-button** (rounded shell, glossy cluster), **8Bit** (boxy shell, flat matte controls). Nothing is bundled from stock art, so the app stays small and licence-clean.
- Three independent visual layers: the general wallpaper, the controller shell — which can be hidden and can carry **its own image**, cropped to the shell outline — and the controls on top. Both images are chosen separately in **Appearance**.
- Combined Bluetooth HID gamepad (report 1), relative mouse (report 2), and keyboard navigation (report 3). Global descriptor state is isolated with Push/Pop to prevent mouse axes inheriting the gamepad hat's physical range.
- Touchpad cursor movement, press-and-hold mouse buttons (drag works), double-tap left click, scrolling, and directional keys with Enter.
- Russian, Ukrainian, and English interfaces; phone/tablet/portrait-adaptive layouts; automatic rotation and optional landscape lock.
- Wallpaper selection, pinch zoom/cropping, control and text colors, and saved sensitivity settings.
- Explicit session control: **Stop and exit** in the UI and a stop action on the foreground notification.
- Settings survive a missing or revoked Bluetooth permission: the app shows a banner with a shortcut to system settings instead of crashing.

Source builds require JDK 17 or newer and Android SDK 36. `targetSdk` is deliberately one level below `compileSdk` (see below). Local SDK paths, signing keys, APKs, and device screenshots are excluded from Git. **The APK shipped as 1.1.0 was signed with the shared Android debug keystore; 1.2.0 must be signed with a real upload key — see `RELEASE.md`.**

Android 9+ application that registers a phone as a standard Bluetooth HID gamepad and sends touch-control input to Android/Google TV, Windows, and compatible Android hosts. Every gamepad layout shares the same standards-based descriptor and emits byte-identical reports, so the profile choice never reaches the host; this is not Xbox or DualSense impersonation and not native Windows XInput.

## Implementation

1. A single 9-byte, Report ID 1 HID descriptor exposes four stick axes, two triggers, one hat and 16 buttons. Axes, triggers and the hat already match what Android's keylayouts expect; the two gamepad profiles only differ in button order. **Standard** follows physical gamepads (A B X Y, L1 R1, Select Start, L3 R3, Home), **Android** follows the Linux kernel's positional Game Pad mapping (1 A, 2 B, 4 X, 5 Y, 7 L1, 8 R1, 11 Select, 12 Start, 13 Home, 14 L3, 15 R3).
2. `HidGamepadService` owns the HID Device proxy, SDP registration, callbacks, connection, reports, foreground notification and cleanup. Callback connection events—not the return from `connect()`—are authoritative. Proxy instances are generation-tagged so a closed proxy cannot report into a newer session.
3. Touch state flows through controls and `GamepadViewModel` to immutable `GamepadState`. The service coalesces the newest state with `StateFlow.sample(10 ms)` — at most 100 reports per second, and no wake-ups while idle.
4. Independent Compose pointer handlers permit simultaneous stick, D-pad, trigger and face-button input. Every gesture resets its control in a `finally` block, and the activity neutralises all controls on `onStop`, so a cancelled gesture cannot leave input stuck on the host.
5. Preferences DataStore persists profile, last host, dead zone, sensitivity, inversion, haptics and theme. The profile is applied from the settings flow, not from a possibly-unloaded initial value.
6. Android 12+ Bluetooth runtime permissions and Android 13+ notification permission are requested; pre-Android 12 manifest permissions remain bounded to API 30. The foreground service is only started once `BLUETOOTH_CONNECT` is actually granted, because Android 14+ rejects a `connectedDevice` foreground service without it.
7. Layouts are selected from the available window size (`BoxWithConstraints`), with a compact variant for narrow/portrait windows.
8. The gamepad screen renders three layers: the wallpaper, the controller shell (a vector silhouette per skin, optionally filled with the user's shell image), and the controls. Controls read the active skin from a `CompositionLocal`, so their drawing code needs no per-layout plumbing. All images are decoded downsampled on a background dispatcher.

### Why `targetSdk = 35` while `compileSdk = 36`

Android 16 (API 36) ignores a fixed `android:screenOrientation` on large screens for apps targeting
API 36+, which would silently break the landscape gamepad layout. Targeting 35 keeps the orientation
lock working. The layouts are adaptive anyway, so raising `targetSdk` later is a one-line change plus
device validation. `compileSdk` stays at 36 because the current AGP/Compose dependency set requires it.

## Milestones

- M1 (implemented): buildable project, report encoder and unit tests.
- M2 (implemented): HID registration/service lifecycle, paired-host list, connect/disconnect and foreground operation.
- M3 (implemented): landscape multi-touch gamepad and three controller layouts.
- M4 (next device-validation step): verify descriptor enumeration and mappings on representative Android TV and Windows hosts; tune mappings from captured results.
- M5: movable/resizable controls, saved layout presets and expanded haptics.

## Gathering Results

For each physical host, record phone model/Android version, host OS, pairing success, HID connection callback sequence, recognized axes/buttons, sustained report latency and reconnect behavior. On Windows use `joy.cpl` to inspect DirectInput reports. Capture `adb logcat | findstr BluetoothGamepad` when registration or connection fails. Acceptance requires simultaneous Left Stick + Right Stick + R2 + South, clean neutral reports on release, reconnect after service restart, and no unbounded report queue. The full 14-case matrix is in `RELEASE.md`.

## Build

Requirements: Android Studio 2026.1.4 or compatible, Android SDK 36, and its bundled JDK. Open this directory and let Gradle sync, or run:

```powershell
./gradlew.bat lintDebug testDebugUnitTest assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/app-debug.apk` on a physical Android 9+ phone. Most emulators cannot validate Bluetooth HID Device behavior.

Release builds are minified with R8 (~3 MB instead of ~25 MB) and must be signed — see `RELEASE.md`.

## Pairing

Grant Nearby devices (and notification) permissions, enable Bluetooth, and pair the receiving host in Android's system Bluetooth settings. Open the app, choose a profile, select the already-paired device, and tap **Connect**.

- Android/Google TV: Settings → Remotes & Accessories → Pair accessory; make the phone discoverable from its system Bluetooth settings, complete pairing, then connect in the app.
- Windows: Settings → Bluetooth & devices → Add device → Bluetooth; pair the phone, connect in the app, then run `joy.cpl` to test axes and buttons.

## Permissions

Android 12+ uses `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, and `BLUETOOTH_ADVERTISE`. Android 11 and below use legacy Bluetooth permissions plus location for discovery. The connected-device foreground-service permission/type keeps an active session alive. If Bluetooth permission is denied, the app explains the situation and offers a shortcut to system settings rather than starting a session it cannot maintain.

## Known limitations

- Windows enumerates this as a generic HID/DirectInput controller. Games requiring XInput may need Steam Input or another mapper; the app does not claim to be a genuine Xbox controller.
- Layout profiles change the on-screen control set (and face-button glyphs), nothing more. They add no vendor authentication, XInput support, DualSense features, audio, lightbar or adaptive triggers.
- Triggers are analog-only; games that expect a discrete L2/R2 button may not react.
- Hosts must support the Bluetooth HID Device role and may cache old descriptors; remove and re-pair after descriptor changes.
- The application id is still `com.example.bluetoothgamepad`, which Google Play rejects. Changing it later forces users to reinstall, so it should be decided before the user base grows.

## Troubleshooting

- No devices: pair in system settings first and grant Nearby devices.
- Registration rejected: toggle Bluetooth, stop/reopen the app, and check that the phone vendor exposes `BluetoothHidDevice`.
- Host shows an old layout: remove the phone on the host and re-pair.
- Windows game ignores input while `joy.cpl` works: the game likely requires XInput.
- Input sticks after interruption: return to the gamepad screen. Controls also neutralise automatically when the app leaves the foreground; if it still sticks, use **Stop and exit** to end the session.
- Touchpad does nothing: make sure the mouse profile is selected — the connection screen's profile list is the single source of truth.
- Old settings keep working: profile ids `generic`, `xbox` and `playstation` from versions before 1.2.0 resolve to the standard gamepad automatically.
