package com.severn.bridgemonitor

import java.time.Instant

/**
 * Provides fake data for visual testing and debugging
 */
object DebugDataProvider {
    
    enum class Scenario {
        // Status scenarios
        ALL_OPEN,
        
        // M48 Active scenarios
        M48_EASTBOUND_CLOSED,
        M48_WESTBOUND_CLOSED,
        M48_FULL_CLOSED,
        M48_EASTBOUND_RESTRICTED,
        M48_WESTBOUND_RESTRICTED,
        
        // M4 Active scenarios
        M4_EASTBOUND_CLOSED,
        M4_WESTBOUND_CLOSED,
        M4_FULL_CLOSED,
        M4_EASTBOUND_RESTRICTED,
        M4_WESTBOUND_RESTRICTED,
        
        // Combined scenarios
        BOTH_BRIDGES_CLOSED,
        
        // M48 Countdown scenarios (10 seconds)
        M48_CD_EASTBOUND_RESTRICTION,
        M48_CD_WESTBOUND_RESTRICTION,
        M48_CD_EASTBOUND_CLOSURE,
        M48_CD_WESTBOUND_CLOSURE,
        M48_CD_FULL_CLOSURE,
        
        // M4 Countdown scenarios (10 seconds)
        M4_CD_EASTBOUND_RESTRICTION,
        M4_CD_WESTBOUND_RESTRICTION,
        M4_CD_EASTBOUND_CLOSURE,
        M4_CD_WESTBOUND_CLOSURE,
        M4_CD_FULL_CLOSURE,
        
        // Other scenarios
        MULTIPLE_CLOSURES,
        FUTURE_WORKS_ONLY
    }
    
    enum class WeatherScenario {
        SAFE_CONDITIONS,
        MONITOR_WINDS,
        HIGH_WIND_RISK,
        RAINY_DAY,
        STORM_INCOMING
    }
    
    fun getWeatherData(scenario: WeatherScenario): WeatherData {
        val now = System.currentTimeMillis()
        
        return when (scenario) {
            WeatherScenario.SAFE_CONDITIONS -> WeatherData(
                currentTemperature = 12.5,
                currentWindSpeed = 15.0,
                currentRainProbability = 10,
                highTemperature = 15.2,
                highTempTime = "14:00",
                lowTemperature = 8.5,
                lowTempTime = "06:00",
                maxRainProbability = 20,
                rainTime = "14:00",
                maxWindGust = 22.0,
                gustTime = "15:00",
                windRiskLevel = WindRiskLevel.SAFE,
                lastUpdated = now
            )
            
            WeatherScenario.MONITOR_WINDS -> WeatherData(
                currentTemperature = 8.2,
                currentWindSpeed = 24.0,
                currentRainProbability = 45,
                highTemperature = 10.8,
                highTempTime = "13:00",
                lowTemperature = 6.1,
                lowTempTime = "05:00",
                maxRainProbability = 60,
                rainTime = "18:00",
                maxWindGust = 35.0,
                gustTime = "19:00",
                windRiskLevel = WindRiskLevel.MONITOR,
                lastUpdated = now
            )
            
            WeatherScenario.HIGH_WIND_RISK -> WeatherData(
                currentTemperature = 6.5,
                currentWindSpeed = 38.0,
                currentRainProbability = 70,
                highTemperature = 8.3,
                highTempTime = "12:00",
                lowTemperature = 4.2,
                lowTempTime = "04:00",
                maxRainProbability = 85,
                rainTime = "16:00",
                maxWindGust = 48.0,
                gustTime = "17:00",
                windRiskLevel = WindRiskLevel.HIGH_RISK,
                lastUpdated = now
            )
            
            WeatherScenario.RAINY_DAY -> WeatherData(
                currentTemperature = 10.0,
                currentWindSpeed = 12.0,
                currentRainProbability = 80,
                highTemperature = 11.5,
                highTempTime = "15:00",
                lowTemperature = 8.8,
                lowTempTime = "07:00",
                maxRainProbability = 95,
                rainTime = "11:00",
                maxWindGust = 18.0,
                gustTime = "12:00",
                windRiskLevel = WindRiskLevel.SAFE,
                lastUpdated = now
            )
            
            WeatherScenario.STORM_INCOMING -> WeatherData(
                currentTemperature = 7.8,
                currentWindSpeed = 32.0,
                currentRainProbability = 85,
                highTemperature = 9.2,
                highTempTime = "11:00",
                lowTemperature = 5.5,
                lowTempTime = "03:00",
                maxRainProbability = 100,
                rainTime = "20:00",
                maxWindGust = 52.0,
                gustTime = "21:00",
                windRiskLevel = WindRiskLevel.HIGH_RISK,
                lastUpdated = now
            )
        }
    }
    
    fun getBridgeData(scenario: Scenario): BridgeData {
        val now = System.currentTimeMillis()
        val in5Seconds = Instant.now().plusSeconds(5).toString()
        val in8Hours = Instant.now().plusSeconds(28800).toString()
        val tomorrow = Instant.now().plusSeconds(86400).toString()
        
        return when (scenario) {
            // Status scenarios
            Scenario.ALL_OPEN -> createAllOpenData(now)
            
            // M48 Active scenarios
            Scenario.M48_EASTBOUND_CLOSED -> createM48EastboundClosedData(now)
            Scenario.M48_WESTBOUND_CLOSED -> createM48WestboundClosedData(now)
            Scenario.M48_FULL_CLOSED -> createM48FullClosedData(now)
            Scenario.M48_EASTBOUND_RESTRICTED -> createM48EastboundRestrictedData(now)
            Scenario.M48_WESTBOUND_RESTRICTED -> createM48WestboundRestrictedData(now)
            
            // M4 Active scenarios
            Scenario.M4_EASTBOUND_CLOSED -> createM4EastboundClosedData(now)
            Scenario.M4_WESTBOUND_CLOSED -> createM4WestboundClosedData(now)
            Scenario.M4_FULL_CLOSED -> createM4FullClosedData(now)
            Scenario.M4_EASTBOUND_RESTRICTED -> createM4EastboundRestrictedData(now)
            Scenario.M4_WESTBOUND_RESTRICTED -> createM4WestboundRestrictedData(now)
            
            // Combined scenarios
            Scenario.BOTH_BRIDGES_CLOSED -> createBothBridgesClosedData(now)
            
            // M48 Countdown scenarios (5 seconds)
            Scenario.M48_CD_EASTBOUND_RESTRICTION -> createM48CountdownEastboundRestriction(now, in5Seconds, in8Hours)
            Scenario.M48_CD_WESTBOUND_RESTRICTION -> createM48CountdownWestboundRestriction(now, in5Seconds, in8Hours)
            Scenario.M48_CD_EASTBOUND_CLOSURE -> createM48CountdownEastboundClosure(now, in5Seconds, in8Hours)
            Scenario.M48_CD_WESTBOUND_CLOSURE -> createM48CountdownWestboundClosure(now, in5Seconds, in8Hours)
            Scenario.M48_CD_FULL_CLOSURE -> createM48CountdownFullClosure(now, in5Seconds, in8Hours)
            
            // M4 Countdown scenarios (5 seconds)
            Scenario.M4_CD_EASTBOUND_RESTRICTION -> createM4CountdownEastboundRestriction(now, in5Seconds, in8Hours)
            Scenario.M4_CD_WESTBOUND_RESTRICTION -> createM4CountdownWestboundRestriction(now, in5Seconds, in8Hours)
            Scenario.M4_CD_EASTBOUND_CLOSURE -> createM4CountdownEastboundClosure(now, in5Seconds, in8Hours)
            Scenario.M4_CD_WESTBOUND_CLOSURE -> createM4CountdownWestboundClosure(now, in5Seconds, in8Hours)
            Scenario.M4_CD_FULL_CLOSURE -> createM4CountdownFullClosure(now, in5Seconds, in8Hours)
            
            // Other scenarios
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
            description = "M48 both directions carriageway closure due to emergency repairs",
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
    
    // ========== M48 ACTIVE SCENARIOS ==========
    
    private fun createM48WestboundClosedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound carriageway closure due to high winds",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "poorEnvironment",
            startTime = Instant.now().minusSeconds(3600).toString(),
            endTime = Instant.now().plusSeconds(7200).toString(),
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(closure))
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
    
    private fun createM48FullClosedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 both directions carriageway closure due to accident",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "accident",
            startTime = Instant.now().minusSeconds(1800).toString(),
            endTime = Instant.now().plusSeconds(3600).toString(),
            direction = Direction.BOTH
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(closure))
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
    
    private fun createM48EastboundRestrictedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound lane closure for roadworks",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(1800).toString(),
            endTime = Instant.now().plusSeconds(10800).toString(),
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.RESTRICTED,
                statusMessage = "Restricted - 1 lane closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.RESTRICTED, listOf(closure)),
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
    
    private fun createM48WestboundRestrictedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound lane closure for inspection",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(2400).toString(),
            endTime = Instant.now().plusSeconds(9600).toString(),
            direction = Direction.WESTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.RESTRICTED,
                statusMessage = "Restricted - 1 lane closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.RESTRICTED, listOf(closure))
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
    
    // ========== M4 ACTIVE SCENARIOS ==========
    
    private fun createM4EastboundClosedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound carriageway closure due to incident",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "accident",
            startTime = Instant.now().minusSeconds(900).toString(),
            endTime = Instant.now().plusSeconds(5400).toString(),
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
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM4WestboundClosedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 westbound carriageway closure for emergency repairs",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(1200).toString(),
            endTime = Instant.now().plusSeconds(6000).toString(),
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
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.OPEN, emptyList()),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(closure))
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM4FullClosedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 both directions carriageway closure due to severe weather",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "poorEnvironment",
            startTime = Instant.now().minusSeconds(1500).toString(),
            endTime = Instant.now().plusSeconds(4500).toString(),
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
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(closure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(closure))
            ),
            lastUpdated = now,
            totalClosuresFound = 1
        )
    }
    
    private fun createM4EastboundRestrictedData(now: Long): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound lane 1 & 2 closure for roadworks",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(2100).toString(),
            endTime = Instant.now().plusSeconds(8400).toString(),
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
                status = BridgeStatus.RESTRICTED,
                statusMessage = "Restricted - 1 lane closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.RESTRICTED, listOf(closure)),
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
        // M48: Eastbound restricted, Westbound closed
        val m48EastboundRestriction = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound lane restriction due to roadworks",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(1800).toString(),
            endTime = tonight,
            direction = Direction.EASTBOUND
        )
        
        val m48WestboundClosure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound carriageway closure due to incident",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "accident",
            startTime = Instant.now().minusSeconds(900).toString(),
            endTime = Instant.now().plusSeconds(3600).toString(),
            direction = Direction.WESTBOUND
        )
        
        // M4: Eastbound closed, Westbound open
        val m4EastboundClosure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound carriageway closure for emergency repairs",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(3600).toString(),
            endTime = tonight,
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s), 1 restriction(s)",
                closures = listOf(m48EastboundRestriction, m48WestboundClosure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.RESTRICTED, listOf(m48EastboundRestriction)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.CLOSED, listOf(m48WestboundClosure))
            ),
            m4Bridge = Bridge(
                name = "M4",
                fullName = "M4 Prince of Wales Bridge (Second Severn Crossing, 1996)",
                status = BridgeStatus.CLOSED,
                statusMessage = "CLOSED - 1 active closure(s)",
                closures = listOf(m4EastboundClosure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.CLOSED, listOf(m4EastboundClosure)),
                westbound = DirectionalStatus(Direction.WESTBOUND, BridgeStatus.OPEN, emptyList())
            ),
            lastUpdated = now,
            totalClosuresFound = 3
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
    
    // ========== M48 COUNTDOWN SCENARIOS (10 seconds) ==========
    
    private fun createM48CountdownEastboundRestriction(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound lane closure for inspection",
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
    
    private fun createM48CountdownWestboundRestriction(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 westbound lane closure for roadworks",
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
    
    private fun createM48CountdownEastboundClosure(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound carriageway closure for emergency repairs",
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
    
    private fun createM48CountdownWestboundClosure(now: Long, startTime: String, endTime: String): BridgeData {
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
    
    private fun createM48CountdownFullClosure(now: Long, startTime: String, endTime: String): BridgeData {
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
    
    // ========== M4 COUNTDOWN SCENARIOS (10 seconds) ==========
    
    private fun createM4CountdownEastboundRestriction(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound lane closure for inspection",
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
    
    private fun createM4CountdownWestboundRestriction(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 westbound lane closure for roadworks",
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
    
    private fun createM4CountdownEastboundClosure(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 eastbound carriageway closure for emergency repairs",
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
    
    private fun createM4CountdownWestboundClosure(now: Long, startTime: String, endTime: String): BridgeData {
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
    
    private fun createM4CountdownFullClosure(now: Long, startTime: String, endTime: String): BridgeData {
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
    
    // Active restriction scenarios
    private fun createM48EastboundRestrictionData(now: Long): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound lane closure for roadworks",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(1800).toString(),
            endTime = Instant.now().plusSeconds(10800).toString(),
            direction = Direction.EASTBOUND
        )
        
        return BridgeData(
            m48Bridge = Bridge(
                name = "M48",
                fullName = "M48 Severn Bridge (Original Bridge, 1966)",
                status = BridgeStatus.RESTRICTED,
                statusMessage = "Restricted - 1 lane closure(s)",
                closures = listOf(closure),
                eastbound = DirectionalStatus(Direction.EASTBOUND, BridgeStatus.RESTRICTED, listOf(closure)),
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
    
    private fun createM4WestboundRestrictionActiveData(now: Long): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 westbound lane 1 & 2 closure for maintenance",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadMaintenance",
            startTime = Instant.now().minusSeconds(3600).toString(),
            endTime = Instant.now().plusSeconds(7200).toString(),
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
    
    // Countdown restriction scenarios
    private fun createM48CountdownRestriction30Min(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M48 J1-J2",
            description = "M48 eastbound lane closure for inspection",
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
    
    private fun createM4CountdownFullClosure10Min(now: Long, startTime: String, endTime: String): BridgeData {
        val closure = Closure(
            location = "M4 J22-J23",
            description = "M4 westbound carriageway closure for emergency repairs",
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
}
