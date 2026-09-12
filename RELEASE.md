# Release runbook

Everything needed to produce, verify and publish a build of **BlueTooth GamePad & Mouse**.

The app is distributed as a sideloaded APK from a private GitHub repository. If that ever changes
(Google Play, F-Droid), read section 6 first — there is one blocking item there.

---

## 1. One-time: create a production signing key

> The 1.1.0 APK in `Release/` was signed with the **Android debug keystore**
> (`CN=Android Debug`, password `android`). That key is public, is regenerated on every fresh
> machine, and cannot be used for any store. 1.2.0 is the moment to switch.

```powershell
keytool -genkeypair -v -keystore release.jks -alias gamepad -keyalg RSA -keysize 4096 -validity 10000
```

Then copy `keystore.properties.example` to `keystore.properties` and fill it in:

```properties
storeFile=release.jks
storePassword=<password>
keyAlias=gamepad
keyPassword=<password>
```

* `keystore.properties` and `*.jks` are git-ignored — verify with `git status` that neither appears.
* **Back the keystore and its passwords up in two independent places.** Losing them means no future
  build can update an installed app; users would have to uninstall and lose their settings.
* Because the signing identity changes, users updating from 1.1.0 must uninstall first. Say so in
  the release notes.

## 2. Pre-flight

```powershell
./gradlew.bat clean lintDebug testDebugUnitTest assembleRelease
```

Expected: all green, `lintDebug` with 0 errors, all unit tests passing.

If `keystore.properties` is absent the build still succeeds but prints:

```
WARNING: keystore.properties is missing or incomplete — the release APK will be UNSIGNED.
```

An unsigned APK is **not** shippable.

## 3. Verify the artifact

```powershell
# The DN must be your key, never "CN=Android Debug".
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs `
  app\build\outputs\apk\release\app-release.apk

# Size sanity check: the R8 build is ~3 MB. A ~25 MB APK means minification was turned off.
Get-Item app\build\outputs\apk\release\app-release.apk | Select-Object Length

# Checksum to publish next to the download.
Get-FileHash app\build\outputs\apk\release\app-release.apk -Algorithm SHA256
```

## 4. Device validation matrix

Record the results in `docs/device-validation.md` (create it on first run). At minimum:

| # | Scenario | Expected |
|---|---|---|
| 1 | Phone, Android 9 / 12 / 14 / 16 | app starts, HID registers, session connects |
| 2 | Phone in **portrait** (rotation unlocked) | all controls visible and reachable |
| 3 | Tablet, Android 16, portrait and landscape | layout adapts, nothing clipped |
| 4 | Android TV / Google TV host | all 16 buttons, hat, both sticks, both triggers |
| 5 | Windows host via `joy.cpl` | axes centred at rest, full range, 16 buttons, hat |
| 6 | Deny Bluetooth permission, then grant from system settings | no crash, banner → "Open settings" → session starts on return |
| 7 | Revoke Bluetooth permission while connected | no crash, controls degrade to a placeholder label |
| 8 | Bluetooth switched off, then on, then Connect | session starts (proxy is rebuilt) |
| 9 | Hold A + R2, then press Home (app to background) | host sees everything released |
| 10 | Same while rotating the device | everything released, layout re-created |
| 11 | Disconnect → reconnect | neutral state re-sent, no stuck input |
| 12 | Touchpad: hold left button, drag a second finger | host receives a drag, not just a click |
| 13 | Notification action "Stop and exit" | session ends, notification disappears, app exits |
| 14 | Repeat 1–13 with the mouse/touchpad profile after a cold start | touchpad works without re-selecting the profile |

Acceptance criteria (unchanged from the README): simultaneous Left Stick + Right Stick + R2 + South,
clean neutral reports on release, reconnect after a service restart, no unbounded report queue.

Capture logs with:

```powershell
adb logcat | findstr BluetoothGamepad
```

## 5. Publish

```powershell
git tag -a v1.2.0 -m "BlueTooth GamePad & Mouse 1.2.0"
git push origin main --tags
```

1. Attach `app-release.apk` and its SHA-256 to the GitHub release.
2. Update `RELEASE_NOTES.md` (ru + en), including the required-uninstall note from section 1.
3. Keep the previous release APK available for rollback.
4. Never publish `app-debug.apk` or `app-release-unsigned.apk`.

## 6. Before publishing to Google Play

Currently **not** possible, for two reasons:

1. `applicationId` is `com.example.bluetoothgamepad`. Play rejects `com.example.*`.
   Changing it later means a different app listing and a fresh install for every user, so decide
   before the user base grows. The rename touches `namespace`/`applicationId` in
   `app/build.gradle.kts` plus every `package`/directory under `app/src/main/java`.
2. The signing key must be a dedicated upload key (section 1).

Additional Play requirements: a privacy policy (Bluetooth permissions are used, no data leaves the
device), the Data safety form, the foreground-service declaration for the `connectedDevice` type,
and a content rating.
