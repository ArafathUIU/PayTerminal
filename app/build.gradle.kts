plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.arafath.payterminalversion2"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.arafath.payterminalversion2"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Dev backend base URL. 10.0.2.2 is the Android emulator's alias for the host
        // machine's loopback interface, so the app reaches the local ASP.NET Core API
        // without any production URL being hardcoded.
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5058/\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // DI: Hilt wires the dependency graph (Retrofit, OkHttp, Room, DAOs, repositories).
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)

    // Networking: Retrofit over OkHttp with Gson payload conversion.
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Local persistence: Room mirrors the auth/session domain locally.
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Navigation: single-Activity app with a fragment NavHost.
    implementation(libs.navigation.fragment)

    // MVVM: ViewModels + LiveData for UI state observation.
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Secure storage: Keystore-backed EncryptedSharedPreferences for JWT tokens.
    implementation(libs.security.crypto)

testImplementation(libs.junit)
testImplementation(libs.mockito)
androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
