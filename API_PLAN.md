# National Highways API - Severn Bridges Monitoring Plan

## API Overview

The National Highways API provides real-time road and lane closure data for the UK's Strategic Road Network (SRN). This includes the Severn Bridges (M4 and M48 crossings).

## Authentication

- **Method**: Subscription Key Header
- **Header Name**: `Ocp-Apim-Subscription-Key`
- **Our Key**: `2ed4c5ca9bc94ef89900b82d540a4e05`

## Base Endpoint

```
GET https://api.data.nationalhighways.co.uk/roads/v2.0/closures
```

## Query Parameters

- `closureType` - Optional: 'planned' or 'unplanned' (omit for both)
- `startDateTime` - Optional: ISO 8601 format
- `endDateTime` - Optional: ISO 8601 format  
- `modifiedSinceDateTime` - Optional: Get recently modified records
- `pageCursor` - Optional: For pagination

## Severn Bridges Context

The Severn Bridges are:
1. **M48 Severn Bridge** (original bridge, opened 1966)
2. **M4 Second Severn Crossing** (Prince of Wales Bridge, opened 1996)

Both bridges cross the River Severn between England and Wales.

## Data Format

- **Response Format**: DATEX II v3.4 (JSON)
- **Coordinates**: WGS84 (latitude/longitude)
- **Structure**: Nested JSON with:
  - `D2Payload` (root)
  - `situation[]` - Array of traffic situations
  - Each situation contains:
    - `situationRecord[]` - Details of closures/incidents
    - Location data (road names, coordinates)
    - Validity times (start/end)
    - Cause information
    - Lane/carriageway impacts

## Implementation Strategy for Android

1. **Language**: Kotlin (native Android language)
2. **HTTP Client**: OkHttp or Ktor (popular Kotlin libraries)
3. **JSON Parsing**: kotlinx.serialization or Gson
4. **Coroutines**: For async API calls

## Filtering for Severn Bridges

To identify Severn Bridge data, we should look for:
- Road names: "M4" or "M48"
- Location descriptions containing "Severn" or bridge-specific identifiers
- Coordinates near the bridges:
  - M48: Approximately 51.61°N, 2.64°W
  - M4: Approximately 51.57°N, 2.64°W

## Test Plan

1. Make a simple API call with no filters
2. Parse the response to understand the data structure
3. Filter for M4/M48 closures near the Severn crossings
4. Extract relevant information:
   - Closure status (active/planned)
   - Validity period (start/end times)
   - Affected lanes/carriageways
   - Reason for closure
   - Impact description

## Next Steps

1. Create a Kotlin CLI script to test the API
2. Parse and display Severn Bridge information
3. Once validated, integrate into Android app structure
