package com.severn.bridgemonitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * API client for Open-Meteo weather data
 * Provides weather conditions at Severn Bridge location
 */
class WeatherApiClient {
    
    companion object {
        // M48 Severn Bridge coordinates
        private const val LATITUDE = 51.61
        private const val LONGITUDE = -2.64
        
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$LATITUDE&longitude=$LONGITUDE" +
                "&hourly=temperature_2m,precipitation_probability,windgusts_10m" +
                "&timezone=Europe/London" +
                "&forecast_days=1" +
                "&current_weather=true"
        
        // Cache weather data for 30 minutes
        private var cachedWeatherData: WeatherData? = null
        private var lastFetchTime: Long = 0
        private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    suspend fun fetchWeather(): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            
            // Return cached data if still fresh
            if (cachedWeatherData != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
                return@withContext Result.success(cachedWeatherData!!.copy(lastUpdated = now))
            }
            
            val request = Request.Builder()
                .url(BASE_URL)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Weather API returned ${response.code}")
                )
            }
            
            val json = JSONObject(response.body?.string() ?: "")
            val weatherData = parseWeatherData(json, now)
            
            // Cache the result
            cachedWeatherData = weatherData
            lastFetchTime = now
            
            Result.success(weatherData)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseWeatherData(json: JSONObject, timestamp: Long): WeatherData {
        // Get current conditions from current_weather field
        var currentTemp: Double? = null
        var currentWindSpeedKmh: Double? = null
        
        if (json.has("current_weather")) {
            val currentWeather = json.getJSONObject("current_weather")
            currentTemp = currentWeather.optDouble("temperature", Double.NaN)
            currentWindSpeedKmh = currentWeather.optDouble("windspeed", Double.NaN)
            
            if (currentTemp.isNaN()) currentTemp = null
            if (currentWindSpeedKmh.isNaN()) currentWindSpeedKmh = null
        }
        
        // Convert wind speed from km/h to mph
        val currentWindMph = currentWindSpeedKmh?.let { it * 0.621371 }
        
        // Get hourly forecast data
        var currentRainProb: Int? = null
        var highTemp: Double? = null
        var highTempTime: String? = null
        var lowTemp: Double? = null
        var lowTempTime: String? = null
        var maxRainProb: Int? = null
        var rainTimeStr: String? = null
        var maxGustMph: Double? = null
        var gustTimeStr: String? = null
        
        if (json.has("hourly")) {
            val hourly = json.getJSONObject("hourly")
            
            // Get current hour rain probability (first entry)
            if (hourly.has("precipitation_probability")) {
                val rainProbs = hourly.getJSONArray("precipitation_probability")
                if (rainProbs.length() > 0) {
                    currentRainProb = rainProbs.optInt(0, 0)
                }
            }
            
            // Get high and low temperatures with times
            if (hourly.has("temperature_2m")) {
                val temps = hourly.getJSONArray("temperature_2m")
                val times = hourly.optJSONArray("time")
                
                var maxTemp = Double.NEGATIVE_INFINITY
                var minTemp = Double.POSITIVE_INFINITY
                var maxTempIndex = -1
                var minTempIndex = -1
                
                for (i in 0 until temps.length()) {
                    val temp = temps.optDouble(i, Double.NaN)
                    if (!temp.isNaN()) {
                        if (temp > maxTemp) {
                            maxTemp = temp
                            maxTempIndex = i
                        }
                        if (temp < minTemp) {
                            minTemp = temp
                            minTempIndex = i
                        }
                    }
                }
                
                if (maxTemp != Double.NEGATIVE_INFINITY) {
                    highTemp = maxTemp
                    if (times != null && maxTempIndex >= 0 && maxTempIndex < times.length()) {
                        highTempTime = extractTime(times.getString(maxTempIndex))
                    }
                }
                
                if (minTemp != Double.POSITIVE_INFINITY) {
                    lowTemp = minTemp
                    if (times != null && minTempIndex >= 0 && minTempIndex < times.length()) {
                        lowTempTime = extractTime(times.getString(minTempIndex))
                    }
                }
            }
            
            // Get max rain probability and its time
            if (hourly.has("precipitation_probability")) {
                val rainProbs = hourly.getJSONArray("precipitation_probability")
                val times = hourly.optJSONArray("time")
                
                var maxProb = 0
                var maxIndex = -1
                
                for (i in 0 until rainProbs.length()) {
                    val prob = rainProbs.optInt(i, 0)
                    if (prob > maxProb) {
                        maxProb = prob
                        maxIndex = i
                    }
                }
                
                if (maxProb > 0) {
                    maxRainProb = maxProb
                    
                    // Extract time (format: "2026-01-29T14:00" -> "14:00")
                    if (times != null && maxIndex >= 0 && maxIndex < times.length()) {
                        val timeString = times.getString(maxIndex)
                        rainTimeStr = extractTime(timeString)
                    }
                }
            }
            
            // Get max wind gust and its time
            if (hourly.has("windgusts_10m")) {
                val gusts = hourly.getJSONArray("windgusts_10m")
                val times = hourly.optJSONArray("time")
                
                var maxGustKmh = 0.0
                var maxIndex = -1
                
                for (i in 0 until gusts.length()) {
                    val gust = gusts.optDouble(i, 0.0)
                    if (gust > maxGustKmh) {
                        maxGustKmh = gust
                        maxIndex = i
                    }
                }
                
                if (maxGustKmh > 0) {
                    maxGustMph = maxGustKmh * 0.621371
                    
                    // Extract time
                    if (times != null && maxIndex >= 0 && maxIndex < times.length()) {
                        val timeString = times.getString(maxIndex)
                        gustTimeStr = extractTime(timeString)
                    }
                }
            }
        }
        
        // Determine wind risk level based on max gust (more critical than current wind)
        val riskLevel = WeatherData.getWindRiskLevel(maxGustMph ?: currentWindMph)
        
        return WeatherData(
            currentTemperature = currentTemp,
            currentWindSpeed = currentWindMph,
            currentRainProbability = currentRainProb,
            highTemperature = highTemp,
            highTempTime = highTempTime,
            lowTemperature = lowTemp,
            lowTempTime = lowTempTime,
            maxRainProbability = maxRainProb,
            rainTime = rainTimeStr,
            maxWindGust = maxGustMph,
            gustTime = gustTimeStr,
            windRiskLevel = riskLevel,
            lastUpdated = timestamp
        )
    }
    
    private fun extractTime(isoTimestamp: String): String {
        // Extract time from ISO format "2026-01-29T14:00" -> "14:00"
        return if (isoTimestamp.contains("T")) {
            isoTimestamp.split("T")[1]
        } else {
            isoTimestamp
        }
    }
}
