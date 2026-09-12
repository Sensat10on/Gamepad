import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Release signing is read from an optional, git-ignored keystore.properties in the project root:
//   storeFile=release.jks
//   storePassword=...
//   keyAlias=gamepad
//   keyPassword=...
// Without that file `assembleRelease` still works and produces an *unsigned* APK.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}
val releaseStoreFile = keystoreProperties.getProperty("storeFile")
val hasReleaseSigning = !releaseStoreFile.isNullOrBlank() && rootProject.file(releaseStoreFile).exists()

android {
    namespace = "com.example.bluetoothgamepad"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bluetoothgamepad"
        // BluetoothHidDevice (the HID Device role used by this app) was added in API 28,
        // so 28 is the hard floor: the app cannot run on anything older.
        minSdk = 28
        // Kept one level below compileSdk on purpose: Android 16 (API 36) ignores the
        // activity's fixed screenOrientation on large screens, which would break the
        // landscape gamepad layout. Targeting 35 keeps the orientation lock working.
        targetSdk = 35
        versionCode = 3
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        // Version-currency warnings are informational and must not break the build.
        disable += setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable", "OldTargetApi")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    doFirst {
        if (!hasReleaseSigning) {
            logger.warn(
                "WARNING: keystore.properties is missing or incomplete — the release APK will be UNSIGNED. " +
                    "See RELEASE.md for how to create a production signing key."
            )
        }
    }
}
