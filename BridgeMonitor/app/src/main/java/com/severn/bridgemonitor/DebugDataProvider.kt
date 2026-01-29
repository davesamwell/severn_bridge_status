package com.severn.bridgemonitor

import java.time.Instant

/**
 * Provides fake data for visual testing and debugging
 */
object DebugDataProvider {
    
    enum class Scenario {
        ALL_OPEN,
        M48_EASTBOUND_CLOSED,
        M4_WESTBOUND_RESTRICTED,
        BOTH_BRIDGES_CLOSED,
        COUNTDOWN_2_MINUTES,
        COUNTDOWN_30_MINUTES,
        M48_COUNTDOWN_1_HOUR,
        M48_COUNTDOWN_1_MINUTE,
        M48_COUNTDOWN_10_SECONDS,
        M4_COUNTDOWN_1_HOUR,
        M4_COUNTDOWN_1_MINUTE,
        M4_COUNTDOWN_10_SECONDS,
        MULTIPLE_CLOSURES,
        FUTURE_WORKS_ONLY
    }
    
    fun getBridgeData(scenario: Scenario): BridgeData {
        val now = System.currentTimeMillis()
        val in10Seconds = Instant.now().plusSeconds(10).toString()
        val in1Minute = Instant.now().plusSeconds(60).toString()
        val in2Minutes = Instant.now().plusSeconds(120).toString()
        val in30Minutes = Instant.now().plusSeconds(1800).toString()
        val in1Hour = Instant.now().plusSeconds(3600).toString()
        val in8Hours = Instant.now().plusSeconds(28800).toString()
        val tomorrow = Instant.now().plusSeconds(86400).toString()
        
        return when (scenario) {
            Scenario.ALL_OPEN -> createAllOpenData(now)
            Scenario.M48_EASTBOUND_CLOSED -> createM48EastboundClosedData(now)
            Scenario.M4_WESTBOUND_RESTRICTED -> createM4WestboundRestrictedData(now)
            Scenario.BOTH_BRIDGES_CLOSED -> createBothBridgesClosedData(now)
            Scenario.COUNTDOWN_2_MINUTES -> createCountdown2MinutesData(now, in2Minutes, in8Hours)
            Scenario.COUNTDOWN_30_MINUTES -> createCountdown30MinutesData(now, in30Minutes, in8Hours)
            Scenario.M48_COUNTDOWN_1_HOUR -> createM48Countdown1Hour(now, in1Hour, in8Hours)
            Scenario.M48_COUNTDOWN_1_MINUTE -> createM48Countdown1Minute(now, in1Minute, in8Hours)
            Scenario.M48_COUNTDOWN_10_SECONDS -> createM48Countdown10Seconds(now, in10Seconds, in8Hours)
            Scenario.M4_COUNTDOWN_1_HOUR -> createM4Countdown1Hour(now, in1Hour, in8Hours)
            Scenario.M4_COUNTDOWN_1_MINUTE -> createM4Countdown1Minute(now, in1Minute, in8Hours)
            Scenario.M4_COUNTDOWN_10_SECONDS -> createM4Countdown10Seconds(now, in10Seconds, in8Hours)
            Scenario.MULTIPLE_CLOSURES -> createMultipleClosuresData(now, in8Hours, tomorrow)
            Scenario.FUTURE_WORKS_ONLY -> createFutureWorksData(now, in8Hours, tomorrow)
        }
    }
    
    private fun createAllOpenData(now: Long): BridgeData {
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 0
        )
    }
    
    private fun createM48EastboundClosedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound carriageway closure due to high winds",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "poorEnvironment",
            startTime = Instant.now().minusSeconds(3600).toString(),
            endTime = Instant.now().plusSeconds(7200).toString(),
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM4WestboundRestrictedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 westbound lane closure for roadworks",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(1800).toString(),
            endTime = Instant.now().plusSeconds(10800).toString(),
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.RESTRICTED,
                statusMessage = "Restricted - 1 lane closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.RESTRICTED, listOf(closure))
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createBothBridgesClosedData(now: Long): BridgeData {
        val m48Closure = Closure(
            location = "M48 J1-J2",
            description = "M48 both directions closed due to emergency repairs",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(900).toString(),
            endTime = Instant.now().plusSeconds(5400).toString(),
            direction = Direction.BOTH
        )
        
        val m4Closure = Closure(
            location = "M4 J22-J23",
            description = "M4 both directions carriageway closure due to incident",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "accident",
            startTime = Instant.now().minusSeconds(600).toString(),
            endTime = Instant.now().plusSeconds(3600).toString(),
            direction = Direction.BOTH
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(m48Closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(m48Closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(m48Closure))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(m4Closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(m4Closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(m4Closure))
            ),
            lastUpdated = now,
            totalClosuresFound = 2
        )
    }
    
    private fun createCountdown2MinutesData(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound lane closure for maintenance",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createCountdown30MinutesData(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound lane closure for inspection",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(closure))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createMultipleClosuresData(now: Long, tonight: String, tomorrow: String): BridgeData {
        val activeClosure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound lane 1 closure for roadworks",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(3600).toString(),
            endTime = tonight,
            direction = Direction.EASTBOUND
        )
        
        val plannedClosure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound lane closure for resurfacing",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = tonight,
            endTime = tomorrow,
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(plannedClosure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(plannedClosure))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.RESTRICTED,
                statusMessage = "Restricted - 1 lane closure(s)",
                closures = listOf(activeClosure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.RESTRICTED, listOf(activeClosure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 2
        )
    }
    
    private fun createFutureWorksData(now: Long, tonight: String, tomorrow: String): BridgeData {
        val futureClosure1 = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound lane closure for bridge inspection",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = tonight,
            endTime = tomorrow,
            direction = Direction.EASTBOUND
        )
        
        val futureClosure2 = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound lane closure for cable replacement",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = tomorrow,
            endTime = Instant.now().plusSeconds(172800).toString(),
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(futureClosure2),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(futureClosure2))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(futureClosure1),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, listOf(futureClosure1)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 2
        )
    }
    
    // M48 Countdown Scenarios
    private fun createM48Countdown1Hour(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound carriageway closure for maintenance",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM48Countdown1Minute(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound carriageway closure for maintenance",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(closure))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM48Countdown10Seconds(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 both directions carriageway closure for inspection",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.BOTH
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(closure))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    // M4 Countdown Scenarios
    private fun createM4Countdown1Hour(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound carriageway closure for maintenance",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM4Countdown1Minute(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 westbound carriageway closure for maintenance",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(closure))
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM4Countdown10Seconds(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 both directions carriageway closure for inspection",
            isActive = false,
            reason = "Planned",
            validityStatus = "planned",
            cause = "roadMaintenance",
            startTime = startTime,
            endTime = endTime,
            direction = Direction.BOTH
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - No restrictions",
                closures = emptyList(),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.OPEN,
                statusMessage = "Open - 1 planned closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, listOf(closure))
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
}
