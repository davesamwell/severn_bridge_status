# Severn Bridge Monitor - Testing Guide

## Overview

This guide explains how to test the Severn Bridge Monitor app, including:
1. Running unit tests
2. Using Debug Mode for visual testing
3. Test scenarios available

## Unit Tests

### Running Unit Tests

Unit tests are located in `/app/src/test/java/com/severn/bridgemonitor/`

**Run from Android Studio:**
1. Open the project in Android Studio
2. Navigate to `BridgeApiClientTest.kt`
3. Right-click on the test class
4. Select "Run 'BridgeApiClientTest'"

**Run from Command Line:**
```bash
cd BridgeMonitor
./gradlew test
```

**View Test Results:**
```bash
# After running tests, open the report:
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Test Coverage

The unit tests cover:

#### Direction Parsing Tests
- Tests parsing of `eastBound`, `westBound`, `bothDirections`
- Validates handling of unknown/empty direction values
- Ensures correct enum mapping

#### Description Cleaning Tests
- Tests removal of mile marker references (e.g., "201/5-196/0")
- Validates that junction numbers and road names are preserved
- Tests regex patterns for various marker formats

#### Directional Status Analysis Tests
- Tests status determination with no closures (OPEN)
- Tests lane closures (RESTRICTED status)
- Tests carriageway closures (CLOSED status)
- Tests BOTH direction closures affecting both eastbound and westbound

### Adding More Tests

To add new tests, edit `BridgeApiClientTest.kt`:

```kotlin
@Test
fun testYourNewFeature() {
    val client = TestableApiClient()
    
    // Your test logic here
    assertEquals(expected, actual)
}
```

## Debug Mode - Visual Testing

### Activating Debug Mode

1. Launch the app
2. **Long-press** on the "Severn Bridge Status" title at the top
3. A debug menu will appear with test scenarios

### Available Test Scenarios

#### 1. ALL OPEN
- **Purpose:** Test normal operation with no closures
- **What to check:**
  - Both bridges show green status
  - "Open - No restrictions" message
  - No active closures displayed
  - Eastbound and westbound both green

#### 2. M48 EASTBOUND CLOSED
- **Purpose:** Test single direction closure
- **What to check:**
  - M48 shows red status for eastbound
  - M48 westbound remains green
  - Closure reason displays (high winds)
  - Countdown timer if end time is in future
  - M4 unaffected

#### 3. M4 WESTBOUND RESTRICTED
- **Purpose:** Test lane closure (restricted status)
- **What to check:**
  - M4 shows yellow status for westbound
  - "Restricted - 1 lane closure(s)" message
  - Eastbound remains green
  - Closure details show lane closure, not full closure

#### 4. BOTH BRIDGES CLOSED
- **Purpose:** Test worst-case scenario
- **What to check:**
  - Both M48 and M4 show red status
  - Both directions closed for both bridges
  - Multiple closure cards displayed
  - Clear emergency messaging
  - Different closure reasons (emergency repairs vs incident)

#### 5. COUNTDOWN 2 MINUTES
- **Purpose:** Test imminent closure countdown
- **What to check:**
  - Countdown shows "in 2m" or "in 1m 30s"
  - Timer updates every second
  - Status badge shows "📅 PLANNED"
  - Yellow background for upcoming closure
  - Check transition at exactly 0 (should become active)

#### 6. COUNTDOWN 30 MINUTES
- **Purpose:** Test longer countdown display
- **What to check:**
  - Countdown shows "in 30m" format
  - Timer counts down correctly
  - Bridge status remains GREEN until closure starts
  - Closure card visible in "Future Pain" tab

#### 7. MULTIPLE CLOSURES
- **Purpose:** Test complex scenario with active + planned closures
- **What to check:**
  - Present Pain: Shows active closures only
  - Future Pain: Shows planned closures
  - Different bridges affected differently
  - Countdown timers work for planned closures
  - Tab switching works correctly

#### 8. FUTURE WORKS ONLY
- **Purpose:** Test "all quiet now, but work coming" scenario
- **What to check:**
  - Present Pain: All green, no active closures
  - Future Pain: Shows multiple planned works
  - Countdown timers for closures starting in 8+ hours
  - "Today/Tomorrow" time formatting

### Testing Checklist

For each scenario, verify:

- [ ] **Colors look correct**
  - Green = Open (#4CAF50)
  - Yellow = Restricted (#FFA500)
  - Red = Closed (#F44336)
  
- [ ] **Dark mode support**
  - Switch device to dark mode
  - Check text is readable
  - Check colors don't clash
  - Background should be dark (#121212)

- [ ] **Countdown timers**
  - Timer updates every second
  - Format is correct ("in 2m", "in 1h 30m", etc.)
  - No flashing or jumping
  - Handles single-digit seconds

- [ ] **Tab switching**
  - Present Pain / Future Pain tabs work
  - Underline indicator animates
  - Selected tab is bold and blue
  - Unselected tab is normal and gray
  - Content switches correctly

- [ ] **Directional display**
  - Eastbound = Wales → England
  - Westbound = England → Wales
  - Both directions show separate status
  - Split view shows both directions clearly

- [ ] **Refresh functionality**
  - Pull-to-refresh shows spinner
  - Refresh button works
  - Loading doesn't break current view
  - Error messages handled gracefully

### Exiting Debug Mode

1. Long-press the title again
2. Select "Exit Debug Mode" from the menu
3. App will reload real data from the API
4. Title changes back to normal (no 🛠️ icon)

## Testing Real-World Scenarios

### Testing With Real API Data

1. Exit debug mode (or launch fresh)
2. App will fetch real data from National Highways API
3. Check that:
   - Data loads within 5-10 seconds
   - Error handling works if network unavailable
   - Cache works (fast refresh within 30 seconds)

### Testing Network Errors

**Airplane Mode Test:**
1. Enable airplane mode on device
2. Open app (or refresh)
3. Should show: "No internet connection"
4. Disable airplane mode
5. Pull to refresh
6. Data should load

**API Key Error Test:**
1. Temporarily remove API key from `local.properties`
2. Rebuild app
3. Should show: "API key not configured"

## Manual Testing Checklist

### Installation & First Launch
- [ ] App installs without errors
- [ ] First launch shows loading state
- [ ] Data loads within 10 seconds
- [ ] No crashes on first launch

### User Interface
- [ ] Title is readable
- [ ] Tab switcher works smoothly
- [ ] Bridge cards display correctly
- [ ] Directional split view is clear
- [ ] National Highways link works
- [ ] Refresh button is visible and works

### Data Accuracy
- [ ] Bridge names correct (M48 vs M4)
- [ ] Full names shown (Original Bridge 1966 vs Second Severn 1996)
- [ ] Directions labeled correctly (eastbound/westbound)
- [ ] Junction numbers match reality
- [ ] Mile markers removed from descriptions

### Performance
- [ ] App launches in < 3 seconds
- [ ] Data loads in < 10 seconds
- [ ] Smooth scrolling
- [ ] No lag when switching tabs
- [ ] Countdown timer doesn't cause lag

### Edge Cases
- [ ] No closures - shows "Open - No restrictions"
- [ ] Many closures - scrollable list
- [ ] Very long closure descriptions - text wraps correctly
- [ ] Missing start/end times - handled gracefully
- [ ] Unknown direction - still displays closure

## Automated Testing Future Enhancements

Consider adding:

1. **UI Tests (Espresso)**
   - Test tab switching
   - Test pull-to-refresh
   - Test debug menu activation

2. **Integration Tests**
   - Test API client with mock server
   - Test XML parsing with sample data
   - Test error handling paths

3. **Snapshot Tests**
   - Capture UI state for each scenario
   - Detect visual regressions

4. **Accessibility Tests**
   - Screen reader compatibility
   - Color contrast ratios
   - Touch target sizes

## Reporting Issues

When reporting bugs, include:
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if relevant
- Debug scenario used (if in debug mode)
- Logcat output (if crash)

## Test Data Sources

- **Debug data:** `DebugDataProvider.kt` - modify to add new scenarios
- **Unit test data:** `BridgeApiClientTest.kt` - add new test cases here
- **Real API data:** National Highways API (requires API key)

## Tips for Visual Testing

1. **Test at different times:** Countdown timers behave differently at different intervals
2. **Test dark mode:** Toggle dark mode in device settings
3. **Test rotation:** Rotate device to test landscape mode
4. **Test with slow network:** Use network throttling to test loading states
5. **Test notifications:** If added, test with app in background

## Debug Mode Implementation Details

The debug mode works by:
1. Long-press detection on title (`appTitle.setOnLongClickListener`)
2. AlertDialog with scenario list
3. `DebugDataProvider` returns pre-built `BridgeData` objects
4. Direct UI update bypasses ViewModel and API client
5. Title shows 🛠️ icon to indicate debug mode active
6. "Exit Debug Mode" returns to normal API operation

This allows you to:
- Test all visual states without waiting for real closures
- Verify countdown timer accuracy
- Check color schemes in all states
- Test dark mode with all scenarios
- Demo the app with predictable data
