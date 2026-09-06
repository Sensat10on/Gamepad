# BlueTooth GamePad & Mouse

## Current features

- Generic, Xbox, PlayStation, 8Bit, Sega six-button, and mouse/touchpad profiles.
- Combined Bluetooth HID gamepad (report 1), relative mouse (report 2), and keyboard navigation (report 3). Global descriptor state is isolated with Push/Pop to prevent mouse axes inheriting the gamepad hat's physical range.
- Touchpad cursor movement, double-tap left click, mouse buttons, scrolling, and directional keys with Enter.
- Russian, Ukrainian, and English interfaces; phone/tablet layouts; automatic rotation and optional landscape lock.
- Wallpaper selection, pinch zoom/cropping, control and text colors, and saved sensitivity settings.

Source builds require JDK 17 or newer and Android SDK 36. Local SDK paths, signing keys, APKs, and device screenshots are excluded from Git. The existing APK in the local Release folder uses a development signing certificate.

Android 9+ application that registers a phone as a standard Bluetooth HID gamepad and sends touch-control input to Android/Google TV, Windows, and compatible Android hosts. Xbox-style and PlayStation-style are visual/logical layouts over the same standards-based descriptor; this is not Xbox hardware impersonation or native Windows XInput.

## Implementation

1. A single 9-byte, Report ID 1 HID descriptor exposes four stick axes, two triggers, one hat and 16 buttons.
2. `HidGamepadService` owns the HID Device proxy, SDP registration, callbacks, connection, reports, foreground notification and cleanup. Callback connection events—not the return from `connect()`—are authoritative.
3. Touch state flows through controls and `GamepadViewModel` to immutable `GamepadState`. The service coalesces changes and sends the newest state at no more than 100 Hz.
4. Independent Compose pointer handlers permit simultaneous stick, D-pad, trigger and face-button input. Stick input is circularly clamped, dead-zone rescaled, sensitivity-adjusted and optionally Y-inverted.
5. Preferences DataStore persists profile, last host, dead zone, sensitivity, inversion, haptics and theme.
6. Android 12+ Bluetooth runtime permissions and Android 13+ notification permission are requested; pre-Android 12 manifest permissions remain bounded to API 30.

## Milestones

- M1 (implemented): buildable project, report encoder and unit tests.
- M2 (implemented): HID registration/service lifecycle, paired-host list, connect/disconnect and foreground operation.
- M3 (implemented): landscape multi-touch gamepad and three controller layouts.
- M4 (next device-validation step): verify descriptor enumeration and mappings on representative Android TV and Windows hosts; tune mappings from captured results.
- M5: movable/resizable controls, saved layout presets and expanded haptics.

## Gathering Results

For each physical host, record phone model/Android version, host OS, pairing success, HID connection callback sequence, recognized axes/buttons, sustained report latency and reconnect behavior. On Windows use `joy.cpl` to inspect DirectInput reports. Capture `adb logcat | findstr BluetoothGamepad` when registration or connection fails. Acceptance requires simultaneous Left Stick + Right Stick + R2 + South, clean neutral reports on release, reconnect after service restart, and no unbounded report queue.

## Build

Requirements: Android Studio 2026.1.4 or compatible, Android SDK 36, and its bundled JDK. Open this directory and let Gradle sync, or run:

```powershell
./gradlew.bat testDebugUnitTest assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/app-debug.apk` on a physical Android 9+ phone. Most emulators cannot validate Bluetooth HID Device behavior.

## Pairing

Grant Nearby devices (and notification) permissions, enable Bluetooth, and pair the receiving host in Android's system Bluetooth settings. Open the app, choose a profile, select the already-paired device, and tap **Connect**.

- Android/Google TV: Settings → Remotes & Accessories → Pair accessory; make the phone discoverable from its system Bluetooth settings, complete pairing, then connect in the app.
- Windows: Settings → Bluetooth & devices → Add device → Bluetooth; pair the phone, connect in the app, then run `joy.cpl` to test axes and buttons.

## Permissions

Android 12+ uses `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, and `BLUETOOTH_ADVERTISE`. Android 11 and below use legacy Bluetooth permissions plus location for discovery. The connected-device foreground-service permission/type keeps an active session alive.

## Known limitations

- Windows enumerates this as a generic HID/DirectInput controller. Games requiring XInput may need Steam Input or another mapper; the app does not claim to be a genuine Xbox controller.
- Xbox/PlayStation modes alter labels and logical mapping, not vendor authentication, XInput, DualSense features, audio, lightbar or adaptive triggers.
- Hosts must support the Bluetooth HID Device role and may cache old descriptors; remove and re-pair after descriptor changes.

## Troubleshooting

- No devices: pair in system settings first and grant Nearby devices.
- Registration rejected: toggle Bluetooth, stop/reopen the app, and check that the phone vendor exposes `BluetoothHidDevice`.
- Host shows an old layout: remove the phone on the host and re-pair.
- Windows game ignores input while `joy.cpl` works: the game likely requires XInput.
- Input sticks after interruption: return to the gamepad screen; lifecycle cleanup sends a neutral report when the activity closes.
