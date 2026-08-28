
import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.smartshop"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.smartshop"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val geminiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val razorpayKey = localProperties.getProperty("RAZORPAY_KEY_ID") ?: ""

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"$razorpayKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
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
    dependencies {


        // Gemini AI SDK
        implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

        // Razorpay Standard SDK
        implementation("com.razorpay:checkout:1.6.40")

        // Gson (For parsing local catalog.json & carts.json)
        implementation("com.google.code.gson:gson:2.10.1")

        // Coroutines (For background AI/API calls)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    }
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}