import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Read or generate local.properties from external config files
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

// Read API key from api_primary_key.txt in project root
val apiKeyFile = rootProject.file("../api_primary_key.txt")
val apiKey: String = if (apiKeyFile.exists()) {
    apiKeyFile.readText().trim()
} else {
    // Fallback to local.properties if api_primary_key.txt doesn't exist
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
        localProperties.getProperty("NATIONAL_HIGHWAYS_API_KEY") ?: ""
    } else {
        ""
    }
}

// Read Android SDK path from android_sdk_path.txt in project root
val sdkPathFile = rootProject.file("../android_sdk_path.txt")
val sdkPath: String = if (sdkPathFile.exists()) {
    sdkPathFile.readText().trim()
} else {
    // Fallback to local.properties or ANDROID_HOME
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
        localProperties.getProperty("sdk.dir") ?: System.getenv("ANDROID_HOME") ?: ""
    } else {
        System.getenv("ANDROID_HOME") ?: ""
    }
}

// Update local.properties with values from config files (if they exist)
if (apiKeyFile.exists() || sdkPathFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
    
    if (apiKeyFile.exists() && apiKey.isNotEmpty()) {
        localProperties.setProperty("NATIONAL_HIGHWAYS_API_KEY", apiKey)
    }
    
    if (sdkPathFile.exists() && sdkPath.isNotEmpty()) {
        localProperties.setProperty("sdk.dir", sdkPath)
    }
    
    // Write updated properties back to local.properties
    localProperties.store(FileOutputStream(localPropertiesFile), "Auto-generated from config files")
}

android {
    namespace = "com.severn.bridgemonitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.severn.bridgemonitor"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Inject API key into BuildConfig at compile time
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true  // Enable BuildConfig generation
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Networking - OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
