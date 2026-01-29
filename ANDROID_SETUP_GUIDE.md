# Android Development Setup Guide

## Overview

To develop and test Android apps locally, you need:
1. **Android Studio** - Official IDE for Android development
2. **Android SDK** - Android development tools (comes with Android Studio)
3. **Android Emulator** - Virtual device for testing (comes with Android Studio)
4. **Java/Kotlin** - Programming languages (comes with Android Studio)

## Why Android Studio?

- ✅ All-in-one solution (IDE, SDK, emulator, build tools)
- ✅ Official Google tool - best support
- ✅ Built-in Android Emulator for local testing
- ✅ Visual layout editor
- ✅ Debugging tools
- ✅ Free and open source

## Installation Steps

### Step 1: Install Android Studio

**Download:**
1. Go to: https://developer.android.com/studio
2. Download "Android Studio for Mac (Apple Silicon)" or (Intel) depending on your Mac
3. File is ~1.1 GB

**Install:**
```bash
# After downloading, open the .dmg file
# Drag Android Studio to Applications folder
# Open Android Studio from Applications
```

Or using Homebrew (if you have it):
```bash
brew install --cask android-studio
```

### Step 2: First Launch Setup

When you first open Android Studio:

1. **Welcome Screen** → Click "Next"
2. **Install Type** → Choose "Standard" (recommended)
3. **Select UI Theme** → Choose your preference
4. **Verify Settings** → Check:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device (AVD)
   - Performance (Intel HAXM or Hypervisor.Framework for M1/M2/M3)
5. Click "Finish" - This will download ~3-5 GB of SDK components

**⏱️ This takes 15-30 minutes depending on internet speed**

### Step 3: Create Virtual Device (Emulator)

After SDK installation:

1. Open Android Studio
2. Click "More Actions" → "Virtual Device Manager"
3. Click "Create Device"
4. Choose a device:
   - **Recommended**: Pixel 5 or Pixel 6 (good middle ground)
   - Click "Next"
5. Select a System Image:
   - **Recommended**: Latest stable API (Android 14 - API 34 or Android 13 - API 33)
   - Click "Download" next to the release
   - Click "Next" after download
6. Verify Configuration → Click "Finish"

**⏱️ System image download: ~1-2 GB, takes 5-15 minutes**

### Step 4: Verify Installation

```bash
# Check Android SDK location
ls ~/Library/Android/sdk

# Check available emulators
~/Library/Android/sdk/emulator/emulator -list-avds
```

## Creating Your Bridge App Project

### Option 1: Using Android Studio UI (Easiest)

1. Open Android Studio
2. Click "New Project"
3. Select "Empty Activity" or "Empty Views Activity"
4. Configure:
   - **Name**: Bridge Monitor
   - **Package name**: com.yourname.bridgemonitor
   - **Save location**: /Users/ds185431/git/bridge_app/android/
   - **Language**: Kotlin
   - **Minimum SDK**: API 26 (Android 8.0) - covers 95%+ of devices
5. Click "Finish"

### Option 2: Command Line (Advanced)

```bash
cd /Users/ds185431/git/bridge_app
mkdir android
cd android

# Create new Android project using gradle
# (We'll provide the commands after Android Studio is installed)
```

## Running Your App in Emulator

### Method 1: From Android Studio

1. Open your project in Android Studio
2. Click the "Run" button (green play icon) in the toolbar
3. Select your virtual device from the list
4. Click "OK"

**First launch**: Emulator takes 1-2 minutes to boot
**Subsequent launches**: ~30 seconds

### Method 2: Command Line

```bash
# Start emulator
~/Library/Android/sdk/emulator/emulator -avd Pixel_5_API_34

# In another terminal, install and run your app
cd /Users/ds185431/git/bridge_app/android
./gradlew installDebug
```

## System Requirements

### Minimum:
- **macOS**: 10.14 (Mojave) or higher
- **RAM**: 8 GB (16 GB recommended)
- **Disk Space**: 15-20 GB free for Android Studio + SDK + Emulator
- **Screen**: 1280 x 800 minimum

### Your Mac:
Let's check your specs:
```bash
# Check macOS version
sw_vers

# Check available disk space
df -h / | awk 'NR==2 {print "Free space: " $4}'

# Check RAM
sysctl hw.memsize | awk '{print "RAM: " $2/1024/1024/1024 " GB"}'

# Check processor
sysctl -n machdep.cpu.brand_string
```

## Testing Without Emulator (Alternative)

If your Mac is too slow for the emulator:

### Option 1: Use Your Physical Android Phone
1. Enable "Developer Options" on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable "USB Debugging":
   - Settings → Developer Options → USB Debugging
3. Connect phone via USB
4. Click "Run" in Android Studio → Select your physical device

### Option 2: Use Android Studio's Device Mirroring
- For newer Android devices (Android 11+)
- Wireless debugging over WiFi

## What's Next?

After installation, we'll:
1. ✅ Create the Bridge Monitor app project
2. ✅ Set up the UI with Material Design
3. ✅ Implement the API client (using OkHttp + XML parsing)
4. ✅ Add auto-refresh functionality
5. ✅ Test in the emulator

## Estimated Timeline

- **Download & Install Android Studio**: 30-60 minutes
- **Setup SDK & Emulator**: 20-30 minutes
- **Create First Project**: 5 minutes
- **First App Run**: 5 minutes (after setup)

**Total setup time: ~1-2 hours**

## Quick Start Commands (After Installation)

```bash
# Check installation
which android-studio
~/Library/Android/sdk/tools/bin/sdkmanager --version

# List installed packages
~/Library/Android/sdk/tools/bin/sdkmanager --list

# Create and run emulator
~/Library/Android/sdk/emulator/emulator -avd Pixel_5_API_34 &
```

## Troubleshooting

### "Android SDK not found"
- Restart Android Studio
- Go to Preferences → Appearance & Behavior → System Settings → Android SDK
- Note the SDK Location path

### "Emulator won't start"
- Check virtualization is enabled (should be automatic on Mac)
- Try a different device (less RAM intensive)
- Use a lower API level (older Android version)

### "Build failed"
- Make sure all SDK components are downloaded
- Try: Tools → SDK Manager → SDK Tools → Check all boxes → Apply

## Need Help?

Common issues and solutions:
- **Slow emulator**: Reduce emulator RAM in AVD settings
- **Out of disk space**: Remove old system images in SDK Manager
- **Java errors**: Android Studio includes JDK, no need to install separately

## Let's Get Started!

Once you've installed Android Studio, run:
```bash
cd /Users/ds185431/git/bridge_app
ls -la
```

And let me know - we'll create the app together! 🚀
