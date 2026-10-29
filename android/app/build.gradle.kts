import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing key for Google Play (file is private – see keystore/ in .gitignore)
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore/keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.example.parkora"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Store ID on Google Play / App Store (the Kotlin package name can stay com.example.parkora)
        applicationId = "in.parkora.app"
        // minSdk 26 (Android 8.0) lets us use java.time (LocalDate, LocalTime) for booking dates.
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "2.0.1"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // R8 shrinks the whole app. (Optimizing only androidx/kotlin packages, as in the
            // project template, crashed the release build with an IllegalAccessError.)
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose (UI toolkit)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation between screens
    implementation(libs.androidx.navigation.compose)

    // Firebase: Google sign-in + Cloud Firestore (live shared database, see ParkoraRepository.kt)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    // "Sign in with Google" bottom sheet (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // Showing photos: built-in demo pictures, uploaded photos and Google profile pictures (internet)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
