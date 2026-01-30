package com.severn.bridgemonitor

/**
 * Data models for bridge status
 */

enum class BridgeStatus {
    OPEN,
    RESTRICTED,
    CLOSED,
    UNKNOWN
}

enum class Direction {
    EASTBOUND,  // England -> Wales
    WESTBOUND,  // Wales -> England
    BOTH,
    UNKNOWN
}

data class DirectionalStatus(
    val direction: Direction,
    val status: BridgeStatus,
    val closures: List<Closure>
)

data class Bridge(
    val name: String,
    val fullName: String,
    val status: BridgeStatus,
    val statusMessage: String,
    val closures: List<Closure>,
    val eastbound: DirectionalStatus,
    val westbound: DirectionalStatus
)

data class Closure(
    val location: String,
    val description: String,
    val isActive: Boolean,
    val reason: String,
    val validityStatus: String,
    val cause: String,
    val startTime: String?,
    val endTime: String?,
    val direction: Direction
)

data class BridgeData(
    val m48Bridge: Bridge,
    val m4Bridge: Bridge,
    val lastUpdated: Long,
    val totalClosuresFound: Int
)

enum class WindRiskLevel {
    SAFE,       // 0-25 mph
    MONITOR,    // 26-40 mph
    HIGH_RISK   // 41+ mph
}

data class WeatherData(
    val currentTemperature: Double?,       // °C
    val currentWindSpeed: Double?,         // mph
    val currentRainProbability: Int?,      // 0-100% for current hour
    val highTemperature: Double?,          // °C - today's high
    val highTempTime: String?,             // HH:mm format
    val lowTemperature: Double?,           // °C - today's low
    val lowTempTime: String?,              // HH:mm format
    val maxRainProbability: Int?,          // 0-100%
    val rainTime: String?,                 // HH:mm format
    val maxWindGust: Double?,              // mph
    val gustTime: String?,                 // HH:mm format
    val windRiskLevel: WindRiskLevel,
    val lastUpdated: Long
) {
    companion object {
        fun getWindRiskLevel(windMph: Double?): WindRiskLevel {
            return when {
                windMph == null -> WindRiskLevel.SAFE
                windMph >= 41 -> WindRiskLevel.HIGH_RISK
                windMph >= 26 -> WindRiskLevel.MONITOR
                else -> WindRiskLevel.SAFE
            }
        }
    }
}
