import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.lakescorp.twisterroulette"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lakescorp.twisterroulette"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Signing material lives OUTSIDE the project tree (never committed).
    // Default location follows the per-project convention; override with TWISTER_KEYSTORE_DIR.
    val keystoreDir = File(
        System.getenv("TWISTER_KEYSTORE_DIR")
            ?: "/home/vant/SynologyDrive/Proyectos/TwisterRoulette"
    )
    val credentialsFile = File(keystoreDir, "upload-credentials.txt")
    val keystoreFile = File(keystoreDir, "upload-keystore.jks")
    val keystoreProperties = Properties()
    if (credentialsFile.exists()) {
        credentialsFile.inputStream().use { keystoreProperties.load(it) }
    }

    signingConfigs {
        create("release") {
            if (credentialsFile.exists() && keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = keystoreProperties.getProperty("password")
                keyAlias = keystoreProperties.getProperty("alias")
                keyPassword = keystoreProperties.getProperty("passwordAlias")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    implementation(enforcedPlatform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lucide.icons.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(enforcedPlatform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.24")
    }
}
