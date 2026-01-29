# Bridge App Proof of concept

**Version:** 0.1.0  
**Status:** Development - Proof of Concept Complete ✅

This is going to be a very small android app to moitor the status of the two severn bridges.

National Highways in the UK provide a developer API which I have provided a primary key for in the file api_primary_key.txt

First figure out how the national highways in the UK API works, and how to get data from there. Bare im mind this will be in a android app so choose the right language and tools to do this.

The first test is to write some code, and figure out how to get the data about the severn bridges. Lets write a proof of concept we can test from the CLI

The national highways themselves show this sort of info on a website. This is the sort of thing we want to show in the app

https://nationalhighways.co.uk/travel-updates/the-severn-bridges/

Documenation about the API is here

https://developer.data.nationalhighways.co.uk/api-details#api=road-and-lane-closures-v2&operation=RoadClosures

Lets first come up with a plan and test out the API.

---

## ✅ STATUS: COMPLETED - Proof of Concept Working!

### What's Been Done:

1. ✅ **API Research Complete** - See [API_PLAN.md](API_PLAN.md)
2. ✅ **Working Python CLI Tool** - Run `python3 bridge_monitor.py` to see live bridge status
3. ✅ **Kotlin Template Created** - [BridgeMonitor.kt](BridgeMonitor.kt) for Android development
4. ✅ **API Tested Successfully** - Real-time current status detection working!
5. ✅ **Status Logic Implemented** - Determines OPEN/CLOSED/RESTRICTED from API data

### Quick Test:
```bash
python3 bridge_monitor.py
```

### Current Output Shows:
- 🟢/🔴 **Real-time status** of both bridges (OPEN/CLOSED/RESTRICTED)
- ⚠️ **Active closures** happening RIGHT NOW
- 📅 **Upcoming planned closures**
- ℹ️ Explanation of status fields

### Current Bridge Status (as of 28 Jan 2026, 15:00):
- 🟢 **M48 Severn Bridge**: OPEN (planned closure tonight 20:00)
- 🟢 **M4 Prince of Wales Bridge**: OPEN

### Important Discovery:
The API **doesn't have a simple "open/closed" field**. Current status is **inferred** from:
- `validityStatus` (active/planned/suspended)
- Time windows (start/end times)
- `causeType` (maintenance, weather, accidents)

**Ad-hoc wind closures** appear with `validityStatus="active"` and `causeType="poorEnvironment"`

See [STATUS_EXPLANATION.md](STATUS_EXPLANATION.md) for technical details.
See [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for complete implementation guide.

