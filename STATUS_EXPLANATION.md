# Understanding Bridge Status in the National Highways API

## How Current Status Works

### Status Fields in the API

The API does **NOT** have a simple "currently open/closed" boolean. Instead, status is determined from multiple fields:

#### 1. `validityStatus` - Primary Status Indicator
- **`active`** = Closure is CURRENTLY HAPPENING (confirmed by operator)
- **`planned`** = Closure is scheduled but hasn't started yet
- **`suspended`** = Closure has been cancelled or postponed

#### 2. Time Window Fields
- **`overallStartTime`** = When the closure is scheduled to begin (ISO 8601 format)
- **`overallEndTime`** = When the closure is scheduled to end
- Used in combination with `validityStatus` to determine if we're within the closure period

#### 3. `probabilityOfOccurrence`
- **`certain`** = Confirmed closure
- **`probable`** = Likely closure (e.g., weather-dependent)
- **`riskOf`** = Possible closure

#### 4. `causeType` - Why the closure is happening
- **`roadMaintenance`** = Scheduled maintenance work
- **`poorEnvironment`** = Weather conditions (THIS IS KEY FOR WIND CLOSURES!)
- **`accident`** = Traffic incident
- **`constructionWork`** = Major construction
- Other causes...

## Determining "Currently Open or Closed"

### Our Logic

```python
if validityStatus == "active":
    # Bridge IS closed/restricted RIGHT NOW
    # Operator has confirmed the closure is happening
    return "CLOSED/RESTRICTED"
    
elif validityStatus == "planned":
    if current_time >= startTime AND current_time <= endTime:
        # We're in the planned window, closure might be active
        return "CLOSED/RESTRICTED"
    else:
        # Future or past closure
        return "OPEN (but closure planned)"
        
elif validityStatus == "suspended":
    # Closure was cancelled
    return "OPEN"
```

## Ad-Hoc Wind Closures

### How They Appear

When the Severn Bridges are closed due to high winds:
- **`validityStatus`** = `"active"` (closure is happening NOW)
- **`causeType`** = `"poorEnvironment"` or `"poorWeatherConditions"`
- **`probabilityOfOccurrence`** = Usually `"certain"` once decided
- **Time window** may be open-ended or short-term

Example of what a wind closure would look like:
```xml
<validityStatus>active</validityStatus>
<causeType>poorEnvironment</causeType>
<detailedCauseType>
  <poorEnvironmentType>strongWinds</poorEnvironmentType>
</detailedCauseType>
<probabilityOfOccurrence>certain</probabilityOfOccurrence>
<comment>M48 Severn Bridge closed due to high winds</comment>
```

## Current Test Results (28 Jan 2026, 15:00 UTC)

### Right Now:
- **M48 Severn Bridge**: 🟢 **OPEN** 
  - One planned closure tonight at 20:00 UTC (8 PM)
  - Status: "planned" (not yet active)

- **M4 Prince of Wales Bridge**: 🟢 **OPEN**
  - No closures detected

### Tonight's Planned Closure (Example):
```
M48 - M48 eastbound between J2 and J1
Status: planned (not yet active)
Description: M48 eastbound Jct 2 to 1 Severn Bridge carriageway closure
Starts: 2026-01-28 20:00 UTC
Ends: 2026-01-29 06:00 UTC
```

## Implementation for Android App

### Key Points:

1. **Poll API regularly** (every 5-15 minutes) to catch ad-hoc closures
2. **Check `validityStatus == "active"`** for REAL-TIME closures
3. **Parse time windows** for planned closures
4. **Monitor `causeType`** to identify wind-related closures
5. **Filter by location** using junction numbers:
   - M48: J1, J2 (Severn Bridge specific)
   - M4: J21-J24 (Severn Crossing area)

### Status Display Logic:

```kotlin
enum class BridgeStatus {
    OPEN,           // No active closures
    RESTRICTED,     // Lane closures but bridge passable
    CLOSED          // Full carriageway closure
}

fun determineBridgeStatus(closures: List<Closure>): BridgeStatus {
    val activeClosures = closures.filter { 
        it.validityStatus == "active" ||
        (it.validityStatus == "planned" && 
         now >= it.startTime && now <= it.endTime)
    }
    
    if (activeClosures.isEmpty()) return OPEN
    
    if (activeClosures.any { "carriageway closure" in it.description }) {
        return CLOSED
    }
    
    return RESTRICTED
}
```

## Testing Different Scenarios

To see how different closure types appear, check:
- **Scheduled maintenance**: `validityStatus="planned"` with future times
- **Active maintenance**: `validityStatus="active"` with `causeType="roadMaintenance"`
- **Wind closures**: `validityStatus="active"` with `causeType="poorEnvironment"`
- **Accidents**: `validityStatus="active"` with `causeType="accident"`

## Summary

✅ **Status IS inferred** from:
1. `validityStatus` field (active/planned/suspended)
2. Time windows (start/end times)
3. Current timestamp comparison

❌ **There is NO simple "isOpen" boolean** in the API

✅ **Ad-hoc wind closures** appear as:
- `validityStatus="active"` 
- `causeType="poorEnvironment"`

✅ **Our proof-of-concept correctly determines current status** by combining these fields!
