package com.severn.bridgemonitor

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BridgeApiClient
 */
class BridgeApiClientTest {

    @Test
    fun testDirectionParsing() {
        val client = TestableApiClient()
        
        assertEquals(Direction.EASTBOUND, client.parseDirectionPublic("eastBound"))
        assertEquals(Direction.WESTBOUND, client.parseDirectionPublic("westBound"))
        assertEquals(Direction.BOTH, client.parseDirectionPublic("bothDirections"))
        assertEquals(Direction.UNKNOWN, client.parseDirectionPublic("unknown"))
        assertEquals(Direction.UNKNOWN, client.parseDirectionPublic(""))
    }

    @Test
    fun testDescriptionCleaning() {
        val client = TestableApiClient()
        
        // Test mile marker removal
        val desc1 = "M4 lane closure 201/5-196/0"
        assertEquals("M4 lane closure", client.cleanDescriptionPublic(desc1))
        
        val desc2 = "Bridge work 150/3"
        assertEquals("Bridge work", client.cleanDescriptionPublic(desc2))
        
        // Test no change when no markers
        val desc3 = "M48 closed for maintenance"
        assertEquals("M48 closed for maintenance", client.cleanDescriptionPublic(desc3))
    }

    @Test
    fun testDirectionalStatusAnalysis() {
        val client = TestableApiClient()
        
        // No closures = OPEN
        val openStatus = client.analyzeDirectionalStatusPublic(emptyList(), Direction.EASTBOUND)
        assertEquals(BridgeStatus.OPEN, openStatus.status)
        
        // Active lane closure = RESTRICTED
        val restrictedClosure = Closure(
            location = "M4 J22-J23",
            description = "lane closure",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadworks",
            startTime = null,
            endTime = null,
            direction = Direction.EASTBOUND
        )
        val restrictedStatus = client.analyzeDirectionalStatusPublic(
            listOf(restrictedClosure), 
            Direction.EASTBOUND
        )
        assertEquals(BridgeStatus.RESTRICTED, restrictedStatus.status)
        
        // Carriageway closure = CLOSED
        val closedClosure = Closure(
            location = "M4 J22-J23",
            description = "carriageway closure",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadworks",
            startTime = null,
            endTime = null,
            direction = Direction.EASTBOUND
        )
        val closedStatus = client.analyzeDirectionalStatusPublic(
            listOf(closedClosure),
            Direction.EASTBOUND
        )
        assertEquals(BridgeStatus.CLOSED, closedStatus.status)
    }

    @Test
    fun testDirectionalStatusBothDirections() {
        val client = TestableApiClient()
        
        // BOTH direction closure should affect both eastbound and westbound
        val bothClosure = Closure(
            location = "M4 Bridge",
            description = "lane closure",
            isActive = true,
            reason = "ACTIVE",
            validityStatus = "active",
            cause = "roadworks",
            startTime = null,
            endTime = null,
            direction = Direction.BOTH
        )
        
        val eastStatus = client.analyzeDirectionalStatusPublic(
            listOf(bothClosure),
            Direction.EASTBOUND
        )
        val westStatus = client.analyzeDirectionalStatusPublic(
            listOf(bothClosure),
            Direction.WESTBOUND
        )
        
        assertEquals(BridgeStatus.RESTRICTED, eastStatus.status)
        assertEquals(BridgeStatus.RESTRICTED, westStatus.status)
    }
}

/**
 * Testable version of BridgeApiClient that exposes private methods for testing
 */
class TestableApiClient : BridgeApiClient() {
    fun parseDirectionPublic(directionStr: String) = parseDirection(directionStr)
    fun cleanDescriptionPublic(description: String) = cleanDescription(description)
    fun analyzeDirectionalStatusPublic(closures: List<Closure>, targetDirection: Direction) = 
        analyzeDirectionalStatus(closures, targetDirection)
}
