# Quick Build Reference Card

## 🔧 For Development & Testing

```bash
cd BridgeMonitor

# Build debug version
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Debug build includes:**
- ✅ Long-press title → Debug menu works
- ✅ Unit tests included
- ✅ Debugger attachable
- ✅ All test scenarios available

---

## 🚀 For Release (Production)

```bash
cd BridgeMonitor

# Run tests first!
./gradlew test

# Build release version
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

**Release build automatically:**
- ❌ Debug menu disabled (long-press does nothing)
- ✅ Optimized & minified
- ✅ Smaller APK size
- ✅ Ready for Google Play

---

## 🎯 The Key Difference

**Code protection added:**
```kotlin
// MainActivity.kt line ~86
if (BuildConfig.DEBUG) {  // ← Only in debug builds!
    binding.appTitle.setOnLongClickListener {
        showDebugMenu()
        true
    }
}
```

**What happens:**
- **Debug build:** `BuildConfig.DEBUG = true` → Debug menu enabled
- **Release build:** `BuildConfig.DEBUG = false` → Code completely removed

---

## ✅ How to Test Both

### Test Debug Build
```bash
./gradlew installDebug
# Launch app
# Long-press title
# ✅ Should see debug menu with 8 scenarios
```

### Test Release Build  
```bash
./gradlew installRelease
# Launch app
# Long-press title
# ✅ Should do nothing (debug menu removed)
```

---

## 📦 What Gets Included

| Feature | Debug Build | Release Build |
|---------|-------------|---------------|
| Debug menu (long-press) | ✅ Included | ❌ Removed |
| DebugDataProvider.kt | ✅ Included | ❌ Removed by optimizer |
| Unit tests | ✅ Runs in test | ❌ Not in APK |
| Real API functionality | ✅ Works | ✅ Works |
| All normal features | ✅ Works | ✅ Works |

---

## 🛡️ Security & Best Practices

### ✅ Safe for Production
The `if (BuildConfig.DEBUG)` pattern:
- Evaluated at **compile time**
- Code is **stripped in release** by R8/ProGuard
- Zero performance overhead
- No security risk

### ✅ You Can Keep Both Installed
Add to `build.gradle.kts` to install both versions:
```kotlin
debug {
    applicationIdSuffix = ".debug"  // Separate package names
}
```

Now you'll have:
- `com.severn.bridgemonitor` (release) 🚀
- `com.severn.bridgemonitor.debug` (debug) 🔧

---

## 🎓 Understanding Build Types

```
┌─────────────────────────────────────────────────┐
│                Your Source Code                 │
│  (includes debug menu + all features)           │
└─────────────────┬───────────────────────────────┘
                  │
         ┌────────┴────────┐
         │                 │
    ┌────▼─────┐     ┌────▼─────┐
    │  DEBUG   │     │ RELEASE  │
    │  BUILD   │     │  BUILD   │
    └────┬─────┘     └────┬─────┘
         │                │
    ✅ Debug menu    ❌ Debug menu
    ✅ Test code     ❌ Test code
    ❌ Optimized     ✅ Optimized
    📦 6-8 MB        📦 2-3 MB
```

---

## 🚦 When to Use Each

**Use DEBUG build when:**
- Developing new features
- Testing visual states
- Checking countdown timers
- Verifying dark mode
- Creating screenshots/demos
- QA testing all scenarios

**Use RELEASE build when:**
- Final testing before launch
- Uploading to Google Play
- Beta testing with users
- Performance testing
- Checking APK size

---

## ⚡ One-Command Workflows

### Development
```bash
./gradlew installDebug && adb shell am start -n com.severn.bridgemonitor.debug/.MainActivity
# Builds, installs, and launches debug app
```

### Testing
```bash
./gradlew test && ./gradlew assembleRelease
# Runs tests, then builds release if tests pass
```

### Clean Build
```bash
./gradlew clean assembleRelease
# Cleans previous builds, builds fresh release
```

---

## 📱 Device Testing Checklist

### Debug Build Testing
- [ ] Long-press title → debug menu appears
- [ ] Try "COUNTDOWN 2 MINUTES" scenario
- [ ] Timer counts down correctly
- [ ] Try "BOTH BRIDGES CLOSED" scenario
- [ ] Colors look correct
- [ ] Exit debug mode → real data loads

### Release Build Testing  
- [ ] Long-press title → nothing happens ✅
- [ ] App loads real data from API
- [ ] All normal features work
- [ ] No debug features accessible
- [ ] Performance is smooth
- [ ] APK size is smaller

---

## 🐛 Troubleshooting

**"Debug menu shows in release"**
```bash
# Clean and rebuild
./gradlew clean assembleRelease
```

**"Can't install both versions"**
```bash
# Add to build.gradle.kts:
debug { applicationIdSuffix = ".debug" }
```

**"Tests won't run"**
```bash
# Tests only run on debug variant
./gradlew testDebugUnitTest
```

---

## 📚 More Info

- Full details: [BUILD_GUIDE.md](BUILD_GUIDE.md)
- Testing: [TESTING_GUIDE.md](TESTING_GUIDE.md)
- Debug scenarios: [DEBUG_MODE_REFERENCE.md](DEBUG_MODE_REFERENCE.md)

---

## ✨ Summary

**The change made:** Added `if (BuildConfig.DEBUG)` wrapper
**Result:** Debug features automatically disabled in production
**You get:** Safe testing in debug, clean release for users
**No work needed:** Android build system handles everything!
