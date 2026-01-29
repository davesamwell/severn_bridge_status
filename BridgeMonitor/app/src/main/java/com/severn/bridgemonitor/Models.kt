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
