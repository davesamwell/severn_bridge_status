#!/usr/bin/env kotlin

/**
 * Severn Bridge Monitor - Proof of Concept
 * 
 * This script queries the National Highways API to retrieve closure information
 * for the Severn Bridges (M4 and M48 crossings).
 * 
 * Usage: kotlinc -script BridgeMonitor.kt
 * Or compile and run as a Kotlin application
 */

import java.net.URL
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    println("=== Severn Bridge Monitor ===")
    println("Fetching road closure data from National Highways API...\n")
    
    try {
        val closureData = fetchRoadClosures()
        
        // Parse and display the results
        if (closureData.isNotEmpty()) {
            println("Successfully retrieved data (${closureData.length} characters)")
            println("\nFiltering for Severn Bridge (M4/M48) closures...\n")
            
            // Display raw data for now (we'll parse it better in the Android app)
            // In production, we'd use a JSON library like Gson or kotlinx.serialization
            val severFilter = listOf("M4", "M48", "Severn", "severn")
            val lines = closureData.lines()
            
            var foundRelevant = false
            for (line in lines) {
                if (severFilter.any { keyword -> line.contains(keyword) }) {
                    println(line.trim())
                    foundRelevant = true
                }
            }
            
            if (!foundRelevant) {
                println("No closures found for M4/M48 near Severn Bridges")
                println("This likely means the bridges are OPEN with no restrictions!")
            }
            
            println("\n=== Summary ===")
            println("API call successful")
            println("Current time: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
            
        } else {
            println("ERROR: Empty response from API")
        }
        
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
        e.printStackTrace()
    }
}

fun fetchRoadClosures(): String {
    // API Configuration
    val apiKey = readApiKey()
    val baseUrl = "https://api.data.nationalhighways.co.uk/roads/v2.0/closures"
    
    // Create connection
    val url = URL(baseUrl)
    val connection = url.openConnection() as HttpURLConnection
    
    try {
        // Set up request
        connection.requestMethod = "GET"
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", apiKey)
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        // Check response code
        val responseCode = connection.responseCode
        println("Response Code: $responseCode")
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read response
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
                response.append('\n')
            }
            reader.close()
            
            return response.toString()
        } else {
            // Read error response
            val errorReader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
            val errorResponse = StringBuilder()
            var line: String?
            
            while (errorReader.readLine().also { line = it } != null) {
                errorResponse.append(line)
            }
            errorReader.close()
            
            throw Exception("HTTP $responseCode: $errorResponse")
        }
        
    } finally {
        connection.disconnect()
    }
}

fun readApiKey(): String {
    // Read API key from file
    val keyFile = java.io.File("api_primary_key.txt")
    if (!keyFile.exists()) {
        throw Exception("API key file not found: api_primary_key.txt")
    }
    return keyFile.readText().trim()
}

// Run the main function
main()
