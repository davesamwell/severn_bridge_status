# Severn Bridge Monitor - Android App

**Version:** 0.1.0  
**Status:** Development Release  
**Min Android:** 8.0 (API 26)  
**Target Android:** 14 (API 34)

## Project Structure Created! ✅

This is a complete Android app that monitors the Severn Bridges (M48 and M4) using the National Highways API.

## Features

✅ **Real-time Status** - Shows current status of both bridges (OPEN/CLOSED/RESTRICTED)
✅ **Color-coded Display** - Green (Open), Yellow (Restricted), Red (Closed)
✅ **Active Closures** - Highlights closures happening RIGHT NOW
✅ **Planned Closures** - Shows upcoming planned maintenance
✅ **Auto-refresh** - Updates every 5 minutes automatically
✅ **Pull to Refresh** - Manual refresh with swipe-down gesture
✅ **Wind Closure Detection** - Identifies weather-related closures

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: OkHttp
- **XML Parsing**: Android's built-in XmlPullParser
- **Async**: Kotlin Coroutines
- **UI**: Material Design 3 components
- **Minimum Android**: Android 8.0 (API 26) - covers 95%+ devices

## How to Open in Android Studio

1. **Open Android Studio**
2. **File → Open**
3. Navigate to: `/Users/ds185431/git/bridge_app/BridgeMonitor`
4. Click "Open"
5. Wait for Gradle sync (first time takes 2-5 minutes)

## How to Run

### In Emulator:
1. Click the green "Run" button (▶️) in the toolbar
2. Select your virtual device (e.g., Pixel 6)
3. Wait for emulator to boot (~1-2 min first time)
4. App will install and launch automatically

### On Physical Device:
1. Enable USB Debugging on your phone
2. Connect via USB
3. Click "Run" and select your device

## Project Structure

```
BridgeMonitor/
├── app/
│   ├── src/main/
│   │   ├── java/com/severn/bridgemonitor/
│   │   │   ├── MainActivity.kt          # Main UI screen
│   │   │   ├── BridgeViewModel.kt       # Business logic & state
│   │   │   ├── BridgeApiClient.kt       # API communication
│   │   │   └── Models.kt                # Data models
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml    # UI layout
│   │   │   └── values/
│   │   │       ├── colors.xml           # Status colors
│   │   │       ├── strings.xml          # Text resources
│   │   │       └── themes.xml           # App theme
│   │   └── AndroidManifest.xml          # App configuration
│   └── build.gradle.kts                 # App dependencies
├── build.gradle.kts                     # Project config
└── settings.gradle.kts                  # Project settings
```

## Key Components

### BridgeApiClient.kt
- Fetches data from National Highways API
- Parses XML response
- Filters for Severn Bridge closures only
- Determines current status from validityStatus and time windows

### BridgeViewModel.kt
- Manages UI state and data
- Auto-refreshes every 5 minutes
- Handles loading and error states

### MainActivity.kt
- Displays bridge status with color coding
- Shows active and planned closures
- Pull-to-refresh functionality

## Status Logic (Same as Python POC)

The app determines current status by:
1. Checking `validityStatus` field:
   - `"active"` = Closure happening NOW
   - `"planned"` = Scheduled (checks time window)
   - `"suspended"` = Cancelled
2. Comparing current time with start/end times
3. Identifying closure type (full vs lane closure)

## Testing

### Current Expected Behavior (28 Jan 2026):
- M48 Bridge: **OPEN** (planned closure tonight at 20:00 UTC)
- M4 Bridge: **OPEN** (no restrictions)

### To Test Different Scenarios:
1. Wait until 20:00 UTC tonight - should show M48 as CLOSED
2. Check tomorrow morning - should show M48 as OPEN again
3. Pull to refresh - should update immediately

## Troubleshooting

### "Gradle sync failed"
- Make sure you have internet connection (needs to download dependencies)
- Wait for Android SDK to finish installing
- Try: File → Invalidate Caches → Restart

### "Cannot resolve symbol"
- Wait for Gradle sync to complete
- Check bottom-right corner of Android Studio for progress

### "App won't install on emulator"
- Make sure emulator is fully booted (shows home screen)
- Try: Run → Clean and Rebuild Project
- Then click Run again

## Next Steps / Enhancements

Possible future features:
- 🔔 **Notifications** - Alert when bridge closes unexpectedly
- 📍 **Location-based** - Show nearest bridge
- 📊 **History** - Track closure patterns
- 🌤️ **Weather** - Show current wind speeds
- 🚗 **Traffic** - Real-time traffic conditions
- ⏰ **Widget** - Home screen widget for quick status check

## Need Help?

Common first-time issues:
- **Build errors**: Make sure Android SDK is fully downloaded
- **Slow build**: First build takes 5-10 minutes (downloads dependencies)
- **Emulator slow**: Reduce emulator RAM in AVD settings
- **Can't find device**: Make sure USB debugging is enabled

## API Information

Uses National Highways Road and Lane Closures API (v2.0)
- Base URL: `https://api.data.nationalhighways.co.uk/roads/v2.0/closures`
- API Key: Embedded in `BridgeApiClient.kt`
- Response Format: XML (DATEX II v3.4)
- Update Frequency: Real-time (we poll every 5 minutes)

---

**Ready to run!** Just open in Android Studio and click the green Run button! 🚀
