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

data class Bridge(
    val name: String,
    val fullName: String,
    val status: BridgeStatus,
    val statusMessage: String,
    val closures: List<Closure>
)

data class Closure(
    val location: String,
    val description: String,
    val isActive: Boolean,
    val reason: String,
    val validityStatus: String,
    val cause: String,
    val startTime: String?,
    val endTime: String?
)

data class BridgeData(
    val m48Bridge: Bridge,
    val m4Bridge: Bridge,
    val lastUpdated: Long,
    val totalClosuresFound: Int
)
