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
    // Every screen and all data code come from the shared module (also used by the iPhone app)
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
