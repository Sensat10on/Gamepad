# R8 configuration for the release build.
#
# The app has no reflection-based entry points of its own: HID descriptors are plain
# byte arrays, settings are read through DataStore's generated preference keys and the
# UI is Compose. The default AGP/Compose rules therefore do most of the work; the rules
# below only protect DataStore's protobuf-lite serializer, which is resolved by name.

-keep class androidx.datastore.preferences.protobuf.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Kotlin coroutines and Compose ship their own consumer rules; keep the warnings quiet
# for the optional annotations they reference.
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.coroutines.debug.**

# Keep line numbers so Bluetooth stack traces stay readable; the mapping file is written
# next to the APK for symbolication.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
