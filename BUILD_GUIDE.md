# Build & Release Guide

## Understanding Build Variants

Android automatically provides two build types:

| Build Type | Purpose | Features | When to Use |
|------------|---------|----------|-------------|
| **`debug`** | Development & Testing | • Debug mode enabled<br>• Debugger attachable<br>• Not optimized<br>• Not signed for release | During development and testing |
| **`release`** | Production | • Debug mode **disabled**<br>• Optimized & minified<br>• Signed with release key<br>• Smaller APK size | For Google Play Store |

### What's Different?

**Debug Build:**
- ✅ Long-press title works (debug menu appears)
- ✅ Unit tests included
- ✅ Logging enabled
- ✅ Debugger can attach
- ❌ Larger APK size (~5-10MB)
- ❌ Slower performance

**Release Build:**
- ❌ Debug menu completely disabled (long-press does nothing)
- ❌ No test code included
- ✅ Optimized and minified
- ✅ Smaller APK size (~2-3MB)
- ✅ Faster performance
- ✅ Ready for Google Play

## How to Build

### For Testing (Debug Build)

```bash
cd BridgeMonitor

# Build debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

**Install on device:**
```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or in Android Studio:
# Run → Run 'app' (the green play button)
```

### For Release (Production Build)

```bash
cd BridgeMonitor

# Build release APK (must be signed)
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release-unsigned.apk
# or app-release.apk (if signing configured)
```

## Code Changes Made

### Debug Mode is Build-Aware

The code now checks `BuildConfig.DEBUG`:

```kotlin
// Long press on title to enable debug mode (DEBUG BUILDS ONLY)
if (BuildConfig.DEBUG) {
    binding.appTitle.setOnLongClickListener {
        showDebugMenu()
        true
    }
}
```

**What this means:**
- **Debug build:** Long-press on title → Debug menu appears ✅
- **Release build:** Long-press on title → Nothing happens (feature removed) ❌

### Why This is Safe

The `if (BuildConfig.DEBUG)` block is:
1. **Evaluated at compile time** - Release builds have `DEBUG = false`
2. **Completely stripped** - ProGuard/R8 removes the entire block in release
3. **Zero overhead** - No performance impact in production
4. **No security risk** - Debug features never reach users

## Build Configuration

### Current Setup

Your `app/build.gradle.kts` already has build types configured:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = false  // Consider enabling for production
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
    debug {
        // Debug-specific configuration
        applicationIdSuffix = ".debug"  // Optional: different package name
        versionNameSuffix = "-debug"    // Optional: adds "-debug" to version
    }
}
```

### Recommended Improvements

For a production release, update your `release` build type:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true          // Enable code shrinking
        isShrinkResources = true        // Remove unused resources
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        // Add your signing config here
        signingConfig = signingConfigs.getByName("release")
    }
    debug {
        applicationIdSuffix = ".debug"  // Allows debug & release installed together
        versionNameSuffix = "-DEBUG"
        isDebuggable = true
    }
}
```

## Signing Your Release Build

### Step 1: Create a Keystore

```bash
keytool -genkey -v -keystore severn-bridge-release.keystore \
  -alias severn-bridge -keyalg RSA -keysize 2048 -validity 10000
```

**Important:** Store this keystore safely! You need it for all future updates.

### Step 2: Configure Signing

**Option A: Using `local.properties` (Recommended for security)**

Add to `local.properties` (this file is gitignored):
```properties
# Existing API key
api_key=your_api_key_here

# Release signing
release.storeFile=/path/to/severn-bridge-release.keystore
release.storePassword=your_store_password
release.keyAlias=severn-bridge
release.keyPassword=your_key_password
```

Update `app/build.gradle.kts`:
```kotlin
// Load signing config from local.properties
val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["release.storeFile"] as String)
            storePassword = keystoreProperties["release.storePassword"] as String
            keyAlias = keystoreProperties["release.keyAlias"] as String
            keyPassword = keystoreProperties["release.keyPassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other release config
        }
    }
}
```

## Testing Your Builds

### Test Debug Build

```bash
# Build
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Test debug features
# 1. Launch app
# 2. Long-press title
# 3. Debug menu should appear ✅
```

### Test Release Build

```bash
# Build
./gradlew assembleRelease

# Install (alongside debug if using applicationIdSuffix)
adb install -r app/build/outputs/apk/release/app-release.apk

# Test production behavior
# 1. Launch app
# 2. Long-press title
# 3. Nothing should happen ✅ (debug menu disabled)
```

### Verify Feature Removal

```bash
# Check APK contents (debug menu code should be removed)
unzip -l app/build/outputs/apk/release/app-release.apk | grep -i debug

# Should show minimal/no debug references
```

## Running Tests

### Unit Tests (Always Run Before Release!)

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests BridgeApiClientTest

# Generate test report
./gradlew test
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Run Tests in Debug Build Only

Tests automatically run against the debug variant. They don't run in release builds.

## Build Workflows

### Development Workflow

```bash
# 1. Make changes
# 2. Run unit tests
./gradlew test

# 3. Build debug version
./gradlew assembleDebug

# 4. Install and test
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 5. Test debug features (long-press title, try scenarios)
```

### Release Workflow

```bash
# 1. Update version number in build.gradle.kts
# versionCode = 2
# versionName = "0.2.0"

# 2. Run ALL tests
./gradlew test

# 3. Build release APK
./gradlew assembleRelease

# 4. Test release build thoroughly
adb install -r app/build/outputs/apk/release/app-release.apk
# Verify: No debug features accessible

# 5. Upload to Google Play Console
```

## Best Practices

### ✅ DO:
- Always test debug builds during development
- Run unit tests before every release
- Test release build before uploading to Play Store
- Keep keystore in secure location (not in git!)
- Use `BuildConfig.DEBUG` for debug-only features
- Enable minification in release builds
- Verify debug features are removed in release

### ❌ DON'T:
- Don't commit keystore or passwords to git
- Don't ship debug builds to users
- Don't test only in debug mode (test release too!)
- Don't skip running unit tests
- Don't hardcode debug flags (use `BuildConfig.DEBUG`)
- Don't forget to increment version code for each release

## Version Management

### Before Each Release

Update in `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        versionCode = 2        // Increment by 1 each release
        versionName = "0.2.0"  // Semantic versioning
    }
}
```

**Version Code:** Integer that must increase with each release (1, 2, 3, ...)
**Version Name:** Human-readable version (0.1.0, 0.2.0, 1.0.0, ...)

## Build Size Comparison

Typical sizes for this app:

| Build Type | Size | Notes |
|------------|------|-------|
| Debug | ~6-8 MB | Includes debug info, not optimized |
| Release (no minify) | ~5-6 MB | Signed, but not optimized |
| Release (minified) | ~2-3 MB | Optimized, shrunk (recommended) |

## Troubleshooting

### "Signing key not found"
```bash
# Make sure keystore path is correct in local.properties
release.storeFile=/absolute/path/to/keystore
```

### "Debug menu appears in release build"
```bash
# Clean and rebuild
./gradlew clean assembleRelease

# Verify BuildConfig.DEBUG check is in place
```

### "Tests fail on release variant"
```bash
# This is expected - tests only run on debug
# Use: ./gradlew testDebugUnitTest
```

### "APK is too large"
```bash
# Enable minification and resource shrinking
isMinifyEnabled = true
isShrinkResources = true
```

## Quick Commands Reference

```bash
# Development
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug             # Build + install debug APK
./gradlew test                     # Run unit tests

# Testing
./gradlew connectedAndroidTest    # Run instrumented tests
./gradlew lint                     # Run lint checks

# Release
./gradlew assembleRelease         # Build release APK
./gradlew bundleRelease           # Build App Bundle (for Play Store)

# Cleanup
./gradlew clean                   # Clean build artifacts
./gradlew cleanBuildCache         # Clean build cache
```

## Play Store Deployment

### Recommended: Use App Bundle (AAB)

```bash
# Build App Bundle (smaller download for users)
./gradlew bundleRelease

# Output:
# app/build/outputs/bundle/release/app-release.aab
```

**Why App Bundle?**
- Smaller downloads (Google Play optimizes per-device)
- Required for apps over 150MB
- Better compression
- Recommended by Google

### Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app
3. Production → Create new release
4. Upload `app-release.aab`
5. Fill in release notes
6. Review and release

## Summary

**For Development & Testing:**
```bash
./gradlew assembleDebug
# Debug menu enabled ✅
# Tests included ✅
# Larger APK ✅
```

**For Production Release:**
```bash
./gradlew assembleRelease
# Debug menu disabled ✅
# Optimized & minified ✅
# Smaller APK ✅
# Ready for Play Store ✅
```

The key change: `if (BuildConfig.DEBUG)` ensures debug features are **completely removed** from release builds automatically.
