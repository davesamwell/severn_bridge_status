# Severn Bridge Status Monitor

**Version:** 0.1.0  
**Status:** Production Ready ✅

An Android app to monitor the status of the two Severn bridges in real-time using the National Highways UK API.

## API Key Setup

This app requires an API key from National Highways UK. The API key is **not included** in this repository for security reasons.

### Getting Your API Key

1. Visit the [National Highways Developer Portal](https://developer.data.nationalhighways.co.uk/)
2. Register for a free account
3. Subscribe to the "Road and Lane Closures v2" API
4. Copy your Primary Key

### Creating the API Key File

Create a file named `api_primary_key.txt` in the root directory of this project:

```bash
echo "your-api-key-here" > api_primary_key.txt
```

Replace `your-api-key-here` with your actual API key from the National Highways developer portal.

**Important:** This file is ignored by git and should never be committed to version control.

---

## Features

- 🟢 Real-time bridge status (OPEN/CLOSED/RESTRICTED)
- ⚠️ Active closure notifications
- 📅 Upcoming planned closures with countdown timers
- 🌤️ Weather information including wind conditions
- 🔄 Auto-refresh every 30 seconds
- 🐛 Debug mode for testing scenarios

## Building the App

1. Ensure you have created the `api_primary_key.txt` file as described above
2. Open the `BridgeMonitor` folder in Android Studio
3. Build and run the app on your Android device or emulator

### Build Requirements

- Android Studio Hedgehog or later
- Android SDK 24 or higher
- Kotlin 1.9+

## References

- [Severn Bridges Travel Updates](https://nationalhighways.co.uk/travel-updates/the-severn-bridges/)
- [National Highways API Documentation](https://developer.data.nationalhighways.co.uk/api-details#api=road-and-lane-closures-v2&operation=RoadClosures)

## License

See [LICENSE](LICENSE) file for details.

