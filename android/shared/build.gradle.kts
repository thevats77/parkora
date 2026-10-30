/*
 * SHARED MODULE (Kotlin Multiplatform + Compose Multiplatform)
 * ------------------------------------------------------------
 * All Parkora screens, navigation, Firebase code and helpers live here ONCE and are
 * compiled twice:
 *   - for Android  -> used by the :app module (MainActivity)
 *   - for iPhone   -> packed into "Shared.framework", used by iosApp/ (Swift entry point)
 *
 * Folders:
 *   src/commonMain   code for BOTH platforms (almost everything)
 *   src/androidMain  small Android-only parts (Google sign-in sheet, photo resizing)
 *   src/iosMain      small iPhone-only parts (same jobs, using Apple's APIs)
 */
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.example.parkora.shared"
        compileSdk = 37
        minSdk = 26
        androidResources { enable = true }
    }

    // iPhone (real device) + iPhone simulator on Apple-silicon Macs
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true   // Firebase iOS libraries are linked by the Xcode app (Swift Package Manager)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.cmp.runtime)   // the :app module calls the ParkoraApp() composable
            implementation(libs.cmp.foundation)
            implementation(libs.cmp.ui)
            implementation(libs.cmp.material3)
            implementation(libs.cmp.icons.extended)
            implementation(libs.cmp.resources)
            implementation(libs.cmp.backhandler)
            implementation(libs.cmp.navigation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.gitlive.auth)
            implementation(libs.gitlive.firestore)
            implementation(libs.filekit.dialogs)
            implementation(libs.coil.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.coil.network)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.googleid)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
        }
        iosMain.dependencies {
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.example.parkora.resources"
}
