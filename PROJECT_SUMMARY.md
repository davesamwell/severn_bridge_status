# Severn Bridge Monitor - Project Summary

## Overview
Successfully tested the National Highways API and created a working proof-of-concept to monitor the Severn Bridges (M4 and M48 crossings).

## What Was Accomplished

### 1. API Research & Testing ✓
- **API Endpoint**: `https://api.data.nationalhighways.co.uk/roads/v2.0/closures`
- **Authentication**: Header-based using `Ocp-Apim-Subscription-Key`
- **API Key**: Located in `api_primary_key.txt` (2ed4c5ca9bc94ef89900b82d540a4e05)
- **Response Format**: DATEX II v3.4 (XML format despite JSON Accept header)
- **Status**: API is working correctly - tested successfully on 2026-01-28

### 2. Bridge Monitoring Proof-of-Concept ✓
Created a working Python CLI script ([bridge_monitor.py](bridge_monitor.py)) that:
- ✓ Connects to the National Highways API
- ✓ Fetches all road closure data
- ✓ Parses XML response (229 situations found)
- ✓ Filters for M4/M48 roads (34 closures found)
- ✓ Identifies Severn Bridge specific closures (1 found)
- ✓ Displays closure details including status, times, and descriptions

## Test Results (28 Jan 2026)

### Current Status Display Working! ✅

The enhanced proof-of-concept now shows:

**RIGHT NOW (15:00 UTC):**
- 🟢 **M48 Severn Bridge**: OPEN
  - One planned closure tonight at 20:00 UTC (not yet active)
  
- 🟢 **M4 Prince of Wales Bridge**: OPEN
  - No closures or restrictions

**Upcoming Tonight:**
- M48 eastbound carriageway closure: 20:00 - 06:00 UTC

### How "Current Status" is Determined

**Important Discovery**: The API does NOT have a simple "currently open/closed" field. Status is **inferred** from:

1. **`validityStatus` field**:
   - `"active"` = Closure is happening RIGHT NOW (confirmed)
   - `"planned"` = Scheduled but not yet started
   - `"suspended"` = Cancelled/postponed

2. **Time windows**: Compare current time with `overallStartTime` and `overallEndTime`

3. **`causeType` field** identifies the reason:
   - `"roadMaintenance"` = Planned maintenance
   - `"poorEnvironment"` = **Weather closures (including high winds!)**
   - `"accident"` = Traffic incidents

### Ad-Hoc Wind Closures

When bridges are closed due to high winds, they appear as:
- **`validityStatus="active"`** (happening NOW)
- **`causeType="poorEnvironment"`** 
- Usually with `detailedCauseType` containing `"strongWinds"`

See [STATUS_EXPLANATION.md](STATUS_EXPLANATION.md) for complete technical details.

## Files Created

1. **[API_PLAN.md](API_PLAN.md)** - Detailed API documentation and implementation plan
2. **[bridge_monitor.py](bridge_monitor.py)** - Working Python CLI tool (tested and working)
3. **[BridgeMonitor.kt](BridgeMonitor.kt)** - Kotlin version for Android (untested - requires Kotlin runtime)
4. **api_response.json** - Sample API response (205,158 lines of XML data)

## How to Run the Proof-of-Concept

```bash
cd /Users/ds185431/git/bridge_app
python3 bridge_monitor.py
```

Output shows:
- Current Severn Bridge closure information
- Other M4/M48 closures for context
- Real-time status with timestamps

## Next Steps for Android App

### Technology Stack
For the Android app, we should use:
- **Language**: Kotlin (native Android)
- **HTTP Client**: OkHttp or Ktor
- **XML Parser**: XmlPullParser (built-in) or SimpleXML
- **Async**: Kotlin Coroutines + Flow
- **UI**: Jetpack Compose (modern) or XML layouts (traditional)

### Key Features to Implement
1. **Real-time Bridge Status**
   - Display current status of both bridges (M4 & M48)
   - Show any active or planned closures
   - Color-coded status (Green=Open, Yellow=Restrictions, Red=Closed)

2. **Closure Details**
   - Start/end times
   - Affected lanes/carriageways
   - Reason for closure
   - Direction (eastbound/westbound)

3. **Notifications**
   - Alert users of upcoming closures
   - Background polling for changes
   - Push notifications for important updates

4. **Additional Features**
   - Traffic camera feeds (if available)
   - Weather conditions
   - Journey planning suggestions
   - Historical closure data

### API Integration Pattern
```kotlin
class BridgeMonitorService {
    private val apiKey = "2ed4c5ca9bc94ef89900b82d540a4e05"
    private val baseUrl = "https://api.data.nationalhighways.co.uk/roads/v2.0/closures"
    
    suspend fun fetchBridgeStatus(): BridgeStatus {
        // Make API call with OkHttp
        // Parse XML response
        // Filter for M4/M48 near Severn
        // Return structured data
    }
}
```

### Data Model
```kotlin
data class BridgeStatus(
    val m4Bridge: BridgeClosure?,
    val m48Bridge: BridgeClosure?,
    val lastUpdated: Instant
)

data class BridgeClosure(
    val road: String,
    val direction: String,
    val status: ClosureStatus,
    val startTime: Instant?,
    val endTime: Instant?,
    val description: String,
    val affectedLanes: List<String>
)

enum class ClosureStatus {
    OPEN, PLANNED, ACTIVE, SUSPENDED
}
```

## API Observations

1. **Response Format**: API returns XML despite `Accept: application/json` header
2. **Data Volume**: Large responses (~205K lines), consider caching
3. **Filtering**: Need to filter by:
   - Road name (M4, M48)
   - Location coordinates (51.55-51.65°N, -2.75 to -2.55°W)
   - Junction references (J1, J2 for M48; J21-J24 for M4 Wales side)

4. **Update Frequency**: API recommends frequent polling for real-time data
5. **Authentication**: Simple subscription key header - easy to implement

## Important Links
- API Documentation: https://developer.data.nationalhighways.co.uk/api-details#api=road-and-lane-closures-v2
- National Highways Severn Bridges page: https://nationalhighways.co.uk/travel-updates/the-severn-bridges/

## Conclusion

✅ **Proof-of-concept successful!** 

The National Highways API is working correctly and provides real-time information about Severn Bridge closures. We have:
- Working Python script demonstrating the API integration
- Clear understanding of data format and filtering requirements
- Ready-to-implement plan for Android app development

The API returns comprehensive closure data that can power a useful Android app to help travelers plan their journeys across the Severn Bridges.
