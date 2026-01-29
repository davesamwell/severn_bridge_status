# Example Scenarios - How Different Closures Appear

## Scenario 1: Normal Operation (CURRENT - 28 Jan 15:00)

**What you see:**
```
🟢 M48 SEVERN BRIDGE - Status: OPEN
🟢 M4 PRINCE OF WALES BRIDGE - Status: OPEN
```

**API Data:**
- No closures with `validityStatus="active"` for Severn area
- May have planned closures for tonight

---

## Scenario 2: Planned Maintenance (Tonight at 20:00)

**What you'll see at 20:05:**
```
🔴 M48 SEVERN BRIDGE - Status: CLOSED
   ⚠️ ACTIVE CLOSURE: M48 eastbound between J2 and J1
      M48 eastbound Jct 2 to 1 Severn Bridge carriageway closure
      Reason: ACTIVE (within planned time window)
      Until: 2026-01-29 06:00 UTC
```

**API Data:**
```xml
<validityStatus>planned</validityStatus>
<overallStartTime>2026-01-28T20:00:00Z</overallStartTime>
<overallEndTime>2026-01-29T06:00:00Z</overallEndTime>
<causeType>roadMaintenance</causeType>
```

**Logic:**
- Current time (20:05) is BETWEEN start (20:00) and end (06:00)
- Status shows as "CLOSED" because it's a "carriageway closure"

---

## Scenario 3: High Wind Closure (Ad-Hoc)

**What you'd see:**
```
🔴 M48 SEVERN BRIDGE - Status: CLOSED
   ⚠️ ACTIVE CLOSURE: M48 both directions between J1 and J2
      M48 Severn Bridge closed due to high winds
      Reason: ACTIVE (confirmed by operator)
      Cause: poorEnvironment
      Until: [open-ended or short notice]
```

**API Data:**
```xml
<validityStatus>active</validityStatus>
<causeType>poorEnvironment</causeType>
<detailedCauseType>
  <poorEnvironmentType>strongWinds</poorEnvironmentType>
</detailedCauseType>
<probabilityOfOccurrence>certain</probabilityOfOccurrence>
<comment>M48 Severn Bridge closed due to high winds</comment>
```

**Logic:**
- `validityStatus="active"` means it's happening RIGHT NOW
- `causeType="poorEnvironment"` tells us it's weather-related
- No need to check time windows - "active" means NOW

---

## Scenario 4: Lane Restriction (Not Full Closure)

**What you'd see:**
```
🟡 M4 PRINCE OF WALES BRIDGE - Status: RESTRICTED
   ⚠️ ACTIVE CLOSURE: M4 eastbound between J23 and J24
      M4 eastbound lane 1 closure for bridge inspection
      Reason: ACTIVE (confirmed by operator)
      Cause: roadMaintenance
```

**API Data:**
```xml
<validityStatus>active</validityStatus>
<comment>M4 eastbound lane 1 closure for bridge inspection</comment>
<causeType>roadMaintenance</causeType>
```

**Logic:**
- Status is "RESTRICTED" (not "CLOSED") because it's only a lane closure
- Bridge is still passable, just reduced capacity

---

## Scenario 5: Cancelled Closure

**What you'd see:**
```
🟢 M48 SEVERN BRIDGE - Status: OPEN
   ℹ️ Planned: M48 maintenance work
      Suspended/Cancelled
```

**API Data:**
```xml
<validityStatus>suspended</validityStatus>
<overallStartTime>2026-01-28T20:00:00Z</overallStartTime>
<comment>M48 maintenance work - CANCELLED</comment>
```

**Logic:**
- `validityStatus="suspended"` means it was cancelled
- Bridge is OPEN even though there's a closure record

---

## Scenario 6: Accident (Unplanned Closure)

**What you'd see:**
```
🔴 M4 PRINCE OF WALES BRIDGE - Status: CLOSED
   ⚠️ ACTIVE CLOSURE: M4 westbound between J22 and J23
      M4 westbound closed due to serious accident
      Reason: ACTIVE (confirmed by operator)
      Cause: accident
      Until: [To Be Confirmed]
```

**API Data:**
```xml
<validityStatus>active</validityStatus>
<causeType>accident</causeType>
<detailedCauseType>
  <accidentType>seriousAccident</accidentType>
</detailedCauseType>
<probabilityOfOccurrence>certain</probabilityOfOccurrence>
<comment>M4 westbound closed due to serious accident</comment>
```

**Logic:**
- `validityStatus="active"` = happening now
- `causeType="accident"` identifies it as incident-related
- End time may not be specified (open-ended)

---

## Key Indicators Summary

### Bridge is OPEN when:
- ✅ No closures with `validityStatus="active"`
- ✅ OR planned closures where current time is OUTSIDE the time window
- ✅ OR closures with `validityStatus="suspended"`

### Bridge is CLOSED when:
- 🔴 `validityStatus="active"` AND description contains "carriageway closure"
- 🔴 OR `validityStatus="planned"` with current time INSIDE window AND full closure

### Bridge is RESTRICTED when:
- 🟡 `validityStatus="active"` with lane closures (not full carriageway)
- 🟡 Hard shoulder closures
- 🟡 Speed restrictions

### Ad-Hoc (Wind/Weather) Closures:
- Look for `validityStatus="active"` + `causeType="poorEnvironment"`
- These appear immediately when operator closes bridge
- Usually have short or no advance notice

---

## Testing the App

To verify your Android app handles all scenarios:

1. **Test with current data** - Should show both bridges OPEN
2. **Test tonight at 20:00** - Should show M48 CLOSED
3. **Mock "active" status** - To test UI for live closures
4. **Mock "poorEnvironment" cause** - To test wind closure alerts
5. **Mock different times** - To verify time window logic works

The proof-of-concept demonstrates all the logic needed!
