package com.severn.bridgemonitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * API client for National Highways bridge status
 */
class BridgeApiClient {
    
    companion object {
        // API key is loaded from BuildConfig (injected at compile time from local.properties)
        // Never hardcode API keys in source code!
        private val API_KEY = BuildConfig.API_KEY
        private const val BASE_URL = "https://api.data.nationalhighways.co.uk/roads/v2.0/closures"
        
        // Severn Bridge coordinates (approximate)
        private const val LAT_MIN = 51.55
        private const val LAT_MAX = 51.65
        private const val LON_MIN = -2.75
        private const val LON_MAX = -2.55
        
        // Cache last response to avoid re-parsing same data
        private var lastResponseXml: String? = null
        private var lastParsedData: BridgeData? = null
        private var lastFetchTime: Long = 0
        private const val CACHE_DURATION_MS = 30_000 // 30 seconds
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    suspend fun fetchBridgeStatus(): Result<BridgeData> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            
            // Return cached data if still fresh
            if (lastParsedData != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
                return@withContext Result.success(lastParsedData!!.copy(lastUpdated = now))
            }
            
            // Validate API key is not empty
            if (API_KEY.isEmpty()) {
                return@withContext Result.failure(
                    Exception("API key not configured. Check local.properties file.")
                )
            }
            
            val request = Request.Builder()
                .url(BASE_URL)
                .addHeader("Ocp-Apim-Subscription-Key", API_KEY)
                .addHeader("Accept", "application/xml")
                .addHeader("User-Agent", "BridgeMonitor/0.1.0 Android")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                // Don't expose API key in error messages
                val errorMsg = when (response.code) {
                    401 -> "Authentication failed. Check API key configuration."
                    429 -> "Rate limit exceeded. Please try again later."
                    500, 502, 503 -> "Service temporarily unavailable. Please try again."
                    else -> "Unable to fetch data (Error ${response.code})"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val xmlData = response.body?.string() ?: ""
            
            if (xmlData.isEmpty()) {
                return@withContext Result.failure(Exception("Received empty response"))
            }
            
            // Check if data has actually changed
            if (xmlData == lastResponseXml && lastParsedData != null) {
                // Data unchanged, return cached parsed result
                return@withContext Result.success(lastParsedData!!.copy(lastUpdated = now))
            }
            
            // Data changed or first fetch - parse it
            val closures = parseXmlClosures(xmlData)
            val bridgeData = analyzeBridgeStatus(closures)
            
            // Cache the results
            lastResponseXml = xmlData
            lastParsedData = bridgeData
            lastFetchTime = now
            
            Result.success(bridgeData)
            
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No internet connection"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Connection timeout. Please try again."))
        } catch (e: javax.net.ssl.SSLException) {
            Result.failure(Exception("Secure connection failed"))
        } catch (e: Exception) {
            // Don't expose internal details in production
            Result.failure(Exception("Unable to load bridge status"))
        }
    }
    
    private fun parseXmlClosures(xmlData: String): List<ClosureRecord> {
        val closures = mutableListOf<ClosureRecord>()
        
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlData))
            
            var currentRecord = ClosureRecord()
            var currentTag = ""
            var skipCurrentRecord = false // Early exit flag for irrelevant records
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                    }
                    XmlPullParser.TEXT -> {
                        if (!skipCurrentRecord) {
                            val text = parser.text?.trim() ?: ""
                            if (text.isNotEmpty()) {
                                when (currentTag) {
                                    "roadName" -> {
                                        currentRecord.roadName = text
                                        // Early filtering: skip if not M4 or M48
                                        if (text != "M4" && text != "M48") {
                                            skipCurrentRecord = true
                                        }
                                    }
                                    "locationDescription" -> currentRecord.location = text
                                    "comment" -> currentRecord.description = text
                                    "validityStatus" -> currentRecord.validityStatus = text
                                    "overallStartTime" -> currentRecord.startTime = text
                                    "overallEndTime" -> currentRecord.endTime = text
                                    "causeType" -> currentRecord.cause = text
                                    "probabilityOfOccurrence" -> currentRecord.probability = text
                                    "posList" -> currentRecord.coordinates = text
                                    "directionOnLinearSection" -> currentRecord.direction = text
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "sitRoadOrCarriagewayOrLaneManagement") {
                            // End of a closure record
                            if (!skipCurrentRecord && isSeverBridgeRelevant(currentRecord)) {
                                closures.add(currentRecord.copy())
                            }
                            currentRecord = ClosureRecord()
                            skipCurrentRecord = false // Reset for next record
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return closures
    }
    
    private fun isSeverBridgeRelevant(record: ClosureRecord): Boolean {
        val location = record.location.lowercase()
        val road = record.roadName
        
        // M48 Severn Bridge - Junction 1 and 2
        if (road == "M48" && (location.contains("j1") || location.contains("j2") || 
                               location.contains("junction 1") || location.contains("junction 2") ||
                               location.contains("severn"))) {
            return true
        }
        
        // M4 Second Severn Crossing - J21-J24
        if (road == "M4" && (location.contains("j21") || location.contains("j22") || 
                              location.contains("j23") || location.contains("j24") ||
                              location.contains("junction 21") || location.contains("junction 22") ||
                              location.contains("junction 23") || location.contains("junction 24") ||
                              location.contains("severn") || location.contains("wales"))) {
            return true
        }
        
        // Check coordinates
        if (record.coordinates.isNotEmpty()) {
            try {
                val coords = record.coordinates.split(" ")
                for (i in 0 until coords.size - 1 step 2) {
                    val lat = coords[i].toDoubleOrNull() ?: continue
                    val lon = coords[i + 1].toDoubleOrNull() ?: continue
                    
                    if (lat in LAT_MIN..LAT_MAX && lon in LON_MIN..LON_MAX) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // Ignore coordinate parsing errors
            }
        }
        
        return false
    }
    
    private fun analyzeBridgeStatus(closures: List<ClosureRecord>): BridgeData {
        val now = System.currentTimeMillis()
        
        val m48Closures = mutableListOf<Closure>()
        val m4Closures = mutableListOf<Closure>()
        
        for (record in closures) {
            val isActive = isCurrentlyActive(record)
            val direction = parseDirection(record.direction)
            val closure = Closure(
                location = record.location,
                description = cleanDescription(record.description),
                isActive = isActive,
                reason = if (isActive) "ACTIVE" else "Planned",
                validityStatus = record.validityStatus,
                cause = record.cause,
                startTime = record.startTime,
                endTime = record.endTime,
                direction = direction
            )
            
            if (record.roadName == "M48" || record.location.contains("M48", ignoreCase = true)) {
                m48Closures.add(closure)
            } else if (record.roadName == "M4" || record.location.contains("M4", ignoreCase = true)) {
                m4Closures.add(closure)
            }
        }
        
        val m48Status = determineBridgeStatus(m48Closures)
        val m4Status = determineBridgeStatus(m4Closures)
        
        val m48Eastbound = analyzeDirectionalStatus(m48Closures, Direction.EASTBOUND)
        val m48Westbound = analyzeDirectionalStatus(m48Closures, Direction.WESTBOUND)
        val m4Eastbound = analyzeDirectionalStatus(m4Closures, Direction.EASTBOUND)
        val m4Westbound = analyzeDirectionalStatus(m4Closures, Direction.WESTBOUND)
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = m48Status.first,
                statusMessage = m48Status.second,
                closures = m48Closures,
                eastbound = m48Eastbound,
                westbound = m48Westbound
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = m4Status.first,
                statusMessage = m4Status.second,
                closures = m4Closures,
                eastbound = m4Eastbound,
                westbound = m4Westbound
            ),
            lastUpdated = now,
            totalClosuresFound = closures.size
        )
    }
    
    private fun isCurrentlyActive(record: ClosureRecord): Boolean {
        val status = record.validityStatus.lowercase()
        
        // If explicitly active
        if (status == "active") {
            return true
        }
        
        // If suspended, not active
        if (status == "suspended") {
            return false
        }
        
        // For planned status, check time window
        if (status == "planned" && record.startTime.isNotEmpty() && record.endTime.isNotEmpty()) {
            try {
                val now = Instant.now()
                val start = ZonedDateTime.parse(record.startTime, DateTimeFormatter.ISO_DATE_TIME).toInstant()
                val end = ZonedDateTime.parse(record.endTime, DateTimeFormatter.ISO_DATE_TIME).toInstant()
                
                return now.isAfter(start) && now.isBefore(end)
            } catch (e: Exception) {
                // If parsing fails, assume not active
            }
        }
        
        return false
    }
    
    private fun parseDirection(directionStr: String): Direction {
        return when (directionStr.lowercase()) {
            "eastbound" -> Direction.EASTBOUND
            "westbound" -> Direction.WESTBOUND
            "bothdirections", "both" -> Direction.BOTH
            else -> Direction.UNKNOWN
        }
    }
    
    private fun analyzeDirectionalStatus(closures: List<Closure>, targetDirection: Direction): DirectionalStatus {
        val directionalClosures = closures.filter { 
            it.direction == targetDirection || it.direction == Direction.BOTH
        }
        
        val activeClosures = directionalClosures.filter { it.isActive }
        
        val status = when {
            activeClosures.isEmpty() -> BridgeStatus.OPEN
            activeClosures.any { 
                it.description.contains("carriageway closure", ignoreCase = true) ||
                it.description.contains("bridge closed", ignoreCase = true)
            } -> BridgeStatus.CLOSED
            else -> BridgeStatus.RESTRICTED
        }
        
        return DirectionalStatus(
            direction = targetDirection,
            status = status,
            closures = directionalClosures
        )
    }
    
    private fun determineBridgeStatus(closures: List<Closure>): Pair<BridgeStatus, String> {
        val activeClosures = closures.filter { it.isActive }
        
        if (activeClosures.isEmpty()) {
            val upcoming = closures.filter { !it.isActive && it.validityStatus == "planned" }
            return if (upcoming.isNotEmpty()) {
                Pair(BridgeStatus.OPEN, "Open - ${upcoming.size} planned closure(s)")
            } else {
                Pair(BridgeStatus.OPEN, "Open - No restrictions")
            }
        }
        
        // Check if full closure
        val hasFullClosure = activeClosures.any { 
            it.description.contains("carriageway closure", ignoreCase = true) ||
            it.description.contains("bridge closed", ignoreCase = true)
        }
        
        return if (hasFullClosure) {
            Pair(BridgeStatus.CLOSED, "CLOSED - ${activeClosures.size} active closure(s)")
        } else {
            Pair(BridgeStatus.RESTRICTED, "Restricted - ${activeClosures.size} lane closure(s)")
        }
    }
    
    private fun cleanDescription(description: String): String {
        // Remove mile marker references like "201/5-196/0" or "201/5"
        // This regex matches common patterns without being too aggressive
        return description
            .replace(Regex("\\s*\\d+/\\d+-\\d+/\\d+\\s*"), " ") // e.g., "201/5-196/0"
            .replace(Regex("\\s*\\d+/\\d+\\s*$"), "")             // e.g., "201/5" at end
            .trim()
            .replace(Regex("\\s+"), " ")                          // Clean up extra spaces
    }
    
    private data class ClosureRecord(
        var roadName: String = "",
        var location: String = "",
        var description: String = "",
        var validityStatus: String = "",
        var startTime: String = "",
        var endTime: String = "",
        var cause: String = "",
        var probability: String = "",
        var coordinates: String = "",
        var direction: String = ""
    )
}
