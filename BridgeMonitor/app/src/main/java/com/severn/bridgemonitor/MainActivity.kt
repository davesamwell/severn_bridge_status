package com.severn.bridgemonitor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.severn.bridgemonitor.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: BridgeViewModel
    private val handler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null
    private var currentView = "present" // Track which view is active
    private var debugMode = false
    private var debugData: BridgeData? = null // Store debug data for live updates
    
    // UK timezone (automatically handles BST/GMT)
    private val ukZone = ZoneId.of("Europe/London")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[BridgeViewModel::class.java]
        
        setupUI()
        observeViewModel()
        startCountdownTimer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopCountdownTimer()
    }
    
    private fun startCountdownTimer() {
        countdownRunnable = object : Runnable {
            override fun run() {
                updateCountdowns()
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(countdownRunnable!!)
    }
    
    private fun stopCountdownTimer() {
        countdownRunnable?.let { handler.removeCallbacks(it) }
    }
    
    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
        
        binding.presentPainButton.setOnClickListener {
            showPresentPain()
        }
        
        binding.futurePainButton.setOnClickListener {
            showFuturePain()
        }
        
        // National Highways link handler
        binding.nationalHighwaysLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nationalhighways.co.uk/travel-updates/the-severn-bridges/"))
            startActivity(intent)
        }
        // Long press on title to enable debug mode (DEBUG BUILDS ONLY)
        if (BuildConfig.DEBUG) {
            binding.appTitle.setOnLongClickListener {
                showDebugMenu()
                true
            }
        }
        
        // 
        // Start with Present Pain view
        showPresentPain()
    }
    
    private fun observeViewModel() {
        viewModel.bridgeData.observe(this) { data ->
            if (data != null) {
                // Data loaded - hide loading message and show content
                binding.loadingMessage.visibility = View.GONE
                binding.presentPainContainer.visibility = if (currentView == "present") View.VISIBLE else View.GONE
                binding.futurePainContainer.visibility = if (currentView == "future") View.VISIBLE else View.GONE
                
                updateUI(data)
                // Update Future Pain view if it's currently visible
                if (binding.futurePainContainer.visibility == View.VISIBLE) {
                    updateFuturePainView(data)
                }
            }
        }
        
        viewModel.weatherData.observe(this) { weather ->
            if (weather != null) {
                updateWeatherUI(weather)
                binding.weatherCard.visibility = View.VISIBLE
            } else {
                binding.weatherCard.visibility = View.GONE
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            
            // Show loading message only on initial load (when no data exists yet)
            if (isLoading && viewModel.bridgeData.value == null) {
                binding.loadingMessage.visibility = View.VISIBLE
                binding.presentPainContainer.visibility = View.GONE
                binding.futurePainContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
        
        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.errorText.text = error
                binding.errorText.visibility = View.VISIBLE
                binding.loadingMessage.visibility = View.GONE
            } else {
                binding.errorText.visibility = View.GONE
            }
        }
    }
    
    private fun showPresentPain() {
        currentView = "present"
        binding.presentPainContainer.visibility = View.VISIBLE
        binding.futurePainContainer.visibility = View.GONE
        
        // Update tab styles - modern underline indicator
        binding.presentPainText.apply {
            setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        binding.presentPainIndicator.visibility = View.VISIBLE
        
        binding.futurePainText.apply {
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        binding.futurePainIndicator.visibility = View.INVISIBLE
    }
    
    private fun showFuturePain() {
        currentView = "future"
        binding.presentPainContainer.visibility = View.GONE
        binding.futurePainContainer.visibility = View.VISIBLE
        
        // Update tab styles - modern underline indicator
        binding.presentPainText.apply {
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        binding.presentPainIndicator.visibility = View.INVISIBLE
        
        binding.futurePainText.apply {
            setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        binding.futurePainIndicator.visibility = View.VISIBLE
        
        // Update the view with current data
        viewModel.bridgeData.value?.let { updateFuturePainView(it) }
    }
    
    private fun updateUI(data: BridgeData) {
        // Update timestamp
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        binding.lastUpdated.text = "Last updated: ${dateFormat.format(Date(data.lastUpdated))}"
        
        // M48 Bridge
        updateBridgeDirectionalUI(
            bridge = data.m48Bridge,
            eastboundContainer = binding.m48EastboundContainer,
            eastboundStatus = binding.m48EastboundStatus,
            eastboundText = binding.m48EastboundText,
            westboundContainer = binding.m48WestboundContainer,
            westboundStatus = binding.m48WestboundStatus,
            westboundText = binding.m48WestboundText,
            overallContainer = binding.m48OverallStatusContainer,
            overallStatus = binding.m48OverallStatus,
            closuresText = binding.m48Closures
        )
        
        // M4 Bridge
        updateBridgeDirectionalUI(
            bridge = data.m4Bridge,
            eastboundContainer = binding.m4EastboundContainer,
            eastboundStatus = binding.m4EastboundStatus,
            eastboundText = binding.m4EastboundText,
            westboundContainer = binding.m4WestboundContainer,
            westboundStatus = binding.m4WestboundStatus,
            westboundText = binding.m4WestboundText,
            overallContainer = binding.m4OverallStatusContainer,
            overallStatus = binding.m4OverallStatus,
            closuresText = binding.m4Closures
        )
    }
    
    private fun updateBridgeDirectionalUI(
        bridge: Bridge,
        eastboundContainer: View,
        eastboundStatus: android.widget.TextView,
        eastboundText: android.widget.TextView,
        westboundContainer: View,
        westboundStatus: android.widget.TextView,
        westboundText: android.widget.TextView,
        overallContainer: View,
        overallStatus: android.widget.TextView,
        closuresText: android.widget.TextView
    ) {
        // Update Eastbound
        updateDirectionUI(
            bridge.eastbound,
            eastboundContainer,
            eastboundStatus,
            eastboundText
        )
        
        // Update Westbound
        updateDirectionUI(
            bridge.westbound,
            westboundContainer,
            westboundStatus,
            westboundText
        )
        
        // Show overall status if either direction has issues
        val hasRestrictions = bridge.eastbound.status != BridgeStatus.OPEN || 
                             bridge.westbound.status != BridgeStatus.OPEN
        
        if (hasRestrictions) {
            overallContainer.visibility = View.VISIBLE
            overallStatus.text = when {
                bridge.eastbound.status == BridgeStatus.CLOSED || bridge.westbound.status == BridgeStatus.CLOSED ->
                    "⚠️ Bridge closure in effect"
                else -> "⚠️ Lane restrictions in effect"
            }
        } else {
            overallContainer.visibility = View.GONE
        }
        
        // Display closure details
        if (bridge.closures.isEmpty()) {
            closuresText.visibility = View.GONE
        } else {
            closuresText.visibility = View.VISIBLE
            val closuresInfo = buildString {
                bridge.closures.forEachIndexed { index, closure ->
                    if (index > 0) append("\n\n")
                    
                    // Determine if it's a restriction or full closure
                    val isRestriction = closure.description.contains("lane closure", ignoreCase = true) ||
                                      closure.description.contains("lane restriction", ignoreCase = true)
                    
                    if (closure.isActive) {
                        val indicator = if (isRestriction) "🟠" else "🔴"
                        append("$indicator ACTIVE - ")
                    } else {
                        val typeLabel = if (isRestriction) "Restriction" else "Closure"
                        append("📅 Planned $typeLabel: ")
                    }
                    append(closure.description)
                    
                    if (!closure.isActive) {
                        if (closure.startTime != null) {
                            append("\nFrom: ${formatTime(closure.startTime)}")
                        }
                        if (closure.endTime != null) {
                            append("\nUntil: ${formatTime(closure.endTime)}")
                        }
                    } else {
                        if (closure.endTime != null) {
                            append("\nUntil: ${formatTime(closure.endTime)}")
                        }
                    }
                }
            }
            closuresText.text = closuresInfo
        }
    }
    
    private fun updateDirectionUI(
        directionalStatus: DirectionalStatus,
        container: View,
        statusIcon: android.widget.TextView,
        statusText: android.widget.TextView
    ) {
        val (backgroundColor, iconColor, textColor, statusString) = when (directionalStatus.status) {
            BridgeStatus.OPEN -> listOf(
                R.color.status_open, 
                R.color.status_open_icon, 
                R.color.status_open_text, 
                "OPEN"
            )
            BridgeStatus.RESTRICTED -> listOf(
                R.color.status_restricted, 
                R.color.status_restricted_icon, 
                R.color.status_restricted_text, 
                "RESTRICTED"
            )
            BridgeStatus.CLOSED -> listOf(
                R.color.status_closed, 
                R.color.status_closed_icon, 
                R.color.status_closed_text, 
                "CLOSED"
            )
            BridgeStatus.UNKNOWN -> listOf(
                R.color.status_unknown, 
                R.color.status_unknown_icon, 
                R.color.status_unknown_text, 
                "UNKNOWN"
            )
        }
        
        container.setBackgroundColor(ContextCompat.getColor(this, backgroundColor as Int))
        statusIcon.setTextColor(ContextCompat.getColor(this, iconColor as Int))
        statusText.setTextColor(ContextCompat.getColor(this, textColor as Int))
        statusText.text = statusString as String
    }
    
    private fun updateCountdowns() {
        // Use debug data if in debug mode, otherwise use ViewModel data
        val data = if (debugMode && debugData != null) {
            // Update debug data dynamically - transition planned closures to active
            updateDebugDataStatus(debugData!!)
        } else {
            viewModel.bridgeData.value
        } ?: return
        
        val now = System.currentTimeMillis()
        
        // Check if any planned closures should now be active (start time has passed)
        var shouldReEvaluate = false
        
        for (bridge in listOf(data.m48Bridge, data.m4Bridge)) {
            for (closure in bridge.closures) {
                if (!closure.isActive && closure.startTime != null) {
                    val startTime = parseIsoTime(closure.startTime)
                    // If start time has just passed (within last 5 seconds), re-evaluate status
                    if (startTime > 0 && now >= startTime && (now - startTime) < 5000) {
                        shouldReEvaluate = true
                        break
                    }
                }
            }
            if (shouldReEvaluate) break
        }
        
        // Re-evaluate status locally without API call - the time-based logic will pick up the change
        if (shouldReEvaluate) {
            viewModel.reEvaluateStatus()
        }
        
        // Update M48 countdown
        updateBridgeCountdown(
            data.m48Bridge,
            binding.m48CountdownContainer,
            binding.m48CountdownText,
            now
        )
        
        // Update M4 countdown
        updateBridgeCountdown(
            data.m4Bridge,
            binding.m4CountdownContainer,
            binding.m4CountdownText,
            now
        )
    }
    
    private fun updateBridgeCountdown(
        bridge: Bridge,
        container: View,
        textView: android.widget.TextView,
        now: Long
    ) {
        // Find next planned (non-active) closure
        val nextPlannedClosure = bridge.closures.firstOrNull { !it.isActive && it.startTime != null }
        
        if (nextPlannedClosure != null) {
            try {
                val startTime = parseIsoTime(nextPlannedClosure.startTime!!)
                val timeUntilStart = startTime - now
                
                // Show countdown if within 1 hour (3600000 ms)
                if (timeUntilStart > 0 && timeUntilStart <= 3600000) {
                    container.visibility = View.VISIBLE
                    
                    val totalSeconds = (timeUntilStart / 1000).toInt()
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    
                    // Determine closure type and direction for message
                    val isRestriction = nextPlannedClosure.description.contains("lane closure", ignoreCase = true) ||
                                       nextPlannedClosure.description.contains("lane restriction", ignoreCase = true)
                    
                    val isFullClosure = (nextPlannedClosure.description.contains("carriageway closure", ignoreCase = true) ||
                                        nextPlannedClosure.description.contains("bridge closed", ignoreCase = true)) &&
                                       nextPlannedClosure.direction == Direction.BOTH
                    
                    val message = when {
                        isFullClosure -> "Full closure in"
                        isRestriction && nextPlannedClosure.direction == Direction.EASTBOUND -> "Eastbound restriction in"
                        isRestriction && nextPlannedClosure.direction == Direction.WESTBOUND -> "Westbound restriction in"
                        nextPlannedClosure.direction == Direction.EASTBOUND -> "Eastbound closing in"
                        nextPlannedClosure.direction == Direction.WESTBOUND -> "Westbound closing in"
                        nextPlannedClosure.direction == Direction.BOTH -> "Full closure in"
                        else -> "Closing in"
                    }
                    
                    textView.text = String.format("$message: %02d:%02d", minutes, seconds)
                } else {
                    container.visibility = View.GONE
                }
            } catch (e: Exception) {
                container.visibility = View.GONE
            }
        } else {
            container.visibility = View.GONE
        }
    }
    
    private fun parseIsoTime(isoTime: String): Long {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoTime, DateTimeFormatter.ISO_DATE_TIME)
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun formatTime(isoTime: String): String {
        return try {
            // Parse ISO time and convert to UK timezone (handles BST/GMT automatically)
            val zonedDateTime = ZonedDateTime.parse(isoTime, DateTimeFormatter.ISO_DATE_TIME)
            val ukTime = zonedDateTime.withZoneSameInstant(ukZone)
            
            val now = ZonedDateTime.now(ukZone)
            val today = now.toLocalDate()
            val tomorrow = today.plusDays(1)
            val eventDate = ukTime.toLocalDate()
            
            // Format with smart date labels
            val dateLabel = when (eventDate) {
                today -> "Today"
                tomorrow -> "Tomorrow"
                else -> {
                    // Show full date if more than 1 day away
                    ukTime.format(DateTimeFormatter.ofPattern("dd MMM"))
                }
            }
            
            // Format: "20:00 Today" or "20:00 Tomorrow" or "20:00 on 15 Feb"
            val timeStr = ukTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            if (eventDate == today || eventDate == tomorrow) {
                "$timeStr $dateLabel"
            } else {
                "$timeStr on $dateLabel"
            }
        } catch (e: Exception) {
            isoTime
        }
    }
    
    private fun updateFuturePainView(data: BridgeData) {
        val plannedClosures = mutableListOf<Pair<String, Closure>>()
        
        // Collect ALL closures from both bridges (active and planned)
        // Active ones that are ongoing are still "future pain" in terms of when they'll end
        data.m48Bridge.closures.forEach { 
            plannedClosures.add(Pair("M48 Severn Bridge", it))
        }
        data.m4Bridge.closures.forEach { 
            plannedClosures.add(Pair("M4 Prince of Wales Bridge", it))
        }
        
        // Sort by start time
        plannedClosures.sortBy { it.second.startTime }
        
        if (plannedClosures.isEmpty()) {
            binding.futurePainEmpty.visibility = View.VISIBLE
            binding.futurePainList.visibility = View.GONE
        } else {
            binding.futurePainEmpty.visibility = View.GONE
            binding.futurePainList.visibility = View.VISIBLE
            
            // Clear existing views
            binding.futurePainList.removeAllViews()
            
            // Add a card for each closure (active or planned)
            plannedClosures.forEach { (bridgeName, closure) ->
                val cardView = layoutInflater.inflate(
                    R.layout.planned_closure_card, 
                    binding.futurePainList, 
                    false
                ) as androidx.cardview.widget.CardView
                
                val bridgeText = cardView.findViewById<android.widget.TextView>(R.id.plannedBridgeName)
                val locationText = cardView.findViewById<android.widget.TextView>(R.id.plannedLocation)
                val descriptionText = cardView.findViewById<android.widget.TextView>(R.id.plannedDescription)
                val timeText = cardView.findViewById<android.widget.TextView>(R.id.plannedTime)
                val statusBadge = cardView.findViewById<android.widget.TextView>(R.id.plannedStatusBadge)
                
                bridgeText.text = bridgeName
                locationText.text = closure.location
                descriptionText.text = closure.description
                
                // Show status badge
                if (closure.isActive) {
                    // Determine if it's a restriction or full closure
                    val isRestriction = closure.description.contains("lane closure", ignoreCase = true) ||
                                      closure.description.contains("lane restriction", ignoreCase = true)
                    
                    if (isRestriction) {
                        statusBadge.text = "🟠 ACTIVE NOW"
                        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_restricted))
                    } else {
                        statusBadge.text = "🔴 ACTIVE NOW"
                        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_closed))
                    }
                    statusBadge.visibility = View.VISIBLE
                } else {
                    statusBadge.text = "📅 PLANNED"
                    statusBadge.visibility = View.VISIBLE
                    // Keep yellow background for planned
                }
                
                val timeInfo = buildString {
                    if (closure.startTime != null) {
                        append("From: ${formatTime(closure.startTime)}")
                    }
                    if (closure.endTime != null) {
                        if (closure.startTime != null) append("\n")
                        append("Until: ${formatTime(closure.endTime)}")
                    }
                }
                timeText.text = timeInfo
                
                binding.futurePainList.addView(cardView)
            }
        }
    }
    
    private fun showDebugMenu() {
        val options = listOf(
            "=== Status Scenarios ===" to null,
            "All Open" to DebugDataProvider.Scenario.ALL_OPEN,
            "" to null,
            "=== M48 Active ===" to null,
            "M48 Eastbound Closed" to DebugDataProvider.Scenario.M48_EASTBOUND_CLOSED,
            "M48 Westbound Closed" to DebugDataProvider.Scenario.M48_WESTBOUND_CLOSED,
            "M48 Full Closed" to DebugDataProvider.Scenario.M48_FULL_CLOSED,
            "M48 Eastbound Restricted" to DebugDataProvider.Scenario.M48_EASTBOUND_RESTRICTED,
            "M48 Westbound Restricted" to DebugDataProvider.Scenario.M48_WESTBOUND_RESTRICTED,
            "" to null,
            "=== M4 Active ===" to null,
            "M4 Eastbound Closed" to DebugDataProvider.Scenario.M4_EASTBOUND_CLOSED,
            "M4 Westbound Closed" to DebugDataProvider.Scenario.M4_WESTBOUND_CLOSED,
            "M4 Full Closed" to DebugDataProvider.Scenario.M4_FULL_CLOSED,
            "M4 Eastbound Restricted" to DebugDataProvider.Scenario.M4_EASTBOUND_RESTRICTED,
            "M4 Westbound Restricted" to DebugDataProvider.Scenario.M4_WESTBOUND_RESTRICTED,
            "" to null,
            "=== M48 Countdown (5s) ===" to null,
            "M48: Eastbound Restriction" to DebugDataProvider.Scenario.M48_CD_EASTBOUND_RESTRICTION,
            "M48: Westbound Restriction" to DebugDataProvider.Scenario.M48_CD_WESTBOUND_RESTRICTION,
            "M48: Eastbound Closure" to DebugDataProvider.Scenario.M48_CD_EASTBOUND_CLOSURE,
            "M48: Westbound Closure" to DebugDataProvider.Scenario.M48_CD_WESTBOUND_CLOSURE,
            "M48: Full Closure" to DebugDataProvider.Scenario.M48_CD_FULL_CLOSURE,
            "" to null,
            "=== M4 Countdown (5s) ===" to null,
            "M4: Eastbound Restriction" to DebugDataProvider.Scenario.M4_CD_EASTBOUND_RESTRICTION,
            "M4: Westbound Restriction" to DebugDataProvider.Scenario.M4_CD_WESTBOUND_RESTRICTION,
            "M4: Eastbound Closure" to DebugDataProvider.Scenario.M4_CD_EASTBOUND_CLOSURE,
            "M4: Westbound Closure" to DebugDataProvider.Scenario.M4_CD_WESTBOUND_CLOSURE,
            "M4: Full Closure" to DebugDataProvider.Scenario.M4_CD_FULL_CLOSURE,
            "" to null,
            "=== Other ===" to null,
            "Both Bridges Closed" to DebugDataProvider.Scenario.BOTH_BRIDGES_CLOSED,
            "Multiple Closures" to DebugDataProvider.Scenario.MULTIPLE_CLOSURES,
            "Future Works Only" to DebugDataProvider.Scenario.FUTURE_WORKS_ONLY,
            "" to null,
            "=== Weather Scenarios ===" to null,
            "☀️ Safe Conditions" to DebugDataProvider.WeatherScenario.SAFE_CONDITIONS,
            "🌤️ Monitor Winds" to DebugDataProvider.WeatherScenario.MONITOR_WINDS,
            "⚠️ High Wind Risk" to DebugDataProvider.WeatherScenario.HIGH_WIND_RISK,
            "🌧️ Rainy Day" to DebugDataProvider.WeatherScenario.RAINY_DAY,
            "⛈️ Storm Incoming" to DebugDataProvider.WeatherScenario.STORM_INCOMING
        )
        
        val items = options.map { it.first }.toTypedArray()
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🛠️ Debug Mode - Test Scenarios")
        builder.setItems(items) { _, which ->
            val selected = options[which]
            
            // Skip if it's a header or separator
            if (selected.first.startsWith("===") || selected.first.isEmpty()) {
                return@setItems
            }
            
            debugMode = true
            
            // Check if it's a weather scenario or bridge scenario
            when (val value = selected.second) {
                is DebugDataProvider.Scenario -> {
                    loadDebugData(value)
                }
                is DebugDataProvider.WeatherScenario -> {
                    loadDebugWeather(value)
                }
            }
        }
        
        if (debugMode) {
            builder.setNeutralButton("Exit Debug Mode") { _, _ ->
                debugMode = false
                debugData = null
                binding.appTitle.text = "🌉 Severn Bridges"
                viewModel.refreshData() // Load real data
                android.widget.Toast.makeText(this, "Debug mode disabled", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    
    private fun loadDebugWeather(scenario: DebugDataProvider.WeatherScenario) {
        val weatherData = DebugDataProvider.getWeatherData(scenario)
        
        // Update weather UI directly
        updateWeatherUI(weatherData)
        binding.weatherCard.visibility = View.VISIBLE
        
        // Show debug indicator
        binding.appTitle.text = "🛠️ Severn Bridges (DEBUG)"
        android.widget.Toast.makeText(
            this, 
            "Loaded: ${scenario.name.replace("_", " ")}", 
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
    
    private fun loadDebugData(scenario: DebugDataProvider.Scenario) {
        debugData = DebugDataProvider.getBridgeData(scenario)
        
        // Directly update the UI with debug data
        binding.loadingMessage.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.presentPainContainer.visibility = if (currentView == "present") View.VISIBLE else View.GONE
        binding.futurePainContainer.visibility = if (currentView == "future") View.VISIBLE else View.GONE
        
        updateUI(debugData!!)
        if (binding.futurePainContainer.visibility == View.VISIBLE) {
            updateFuturePainView(debugData!!)
        }
        
        // Show debug indicator
        binding.appTitle.text = "🛠️ Severn Bridge Status (DEBUG)"
        android.widget.Toast.makeText(
            this, 
            "Loaded: ${scenario.name.replace("_", " ")}", 
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
    
    private fun updateDebugDataStatus(data: BridgeData): BridgeData {
        val now = System.currentTimeMillis()
        
        // Function to update closure status based on current time
        fun updateClosure(closure: Closure): Closure {
            if (closure.startTime == null) return closure
            
            val startTime = parseIsoTime(closure.startTime)
            val endTime = if (closure.endTime != null) parseIsoTime(closure.endTime) else Long.MAX_VALUE
            
            // If current time is between start and end, mark as active
            val shouldBeActive = now >= startTime && now < endTime
            
            return if (shouldBeActive != closure.isActive) {
                closure.copy(
                    isActive = shouldBeActive,
                    reason = if (shouldBeActive) "ACTIVE" else "Planned"
                )
            } else {
                closure
            }
        }
        
        // Update closures for both bridges
        val updatedM48Closures = data.m48Bridge.closures.map { updateClosure(it) }
        val updatedM4Closures = data.m4Bridge.closures.map { updateClosure(it) }
        
        // Recalculate bridge status based on updated closures
        fun recalculateStatus(closures: List<Closure>): Pair<BridgeStatus, String> {
            val activeClosures = closures.filter { it.isActive }
            
            if (activeClosures.isEmpty()) {
                val upcoming = closures.filter { !it.isActive && it.validityStatus == "planned" }
                return if (upcoming.isNotEmpty()) {
                    Pair(BridgeStatus.OPEN, "Open - ${upcoming.size} planned closure(s)")
                } else {
                    Pair(BridgeStatus.OPEN, "Open - No restrictions")
                }
            }
            
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
        
        fun recalculateDirectionalStatus(closures: List<Closure>, targetDirection: Direction): DirectionalStatus {
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
        
        val m48Status = recalculateStatus(updatedM48Closures)
        val m4Status = recalculateStatus(updatedM4Closures)
        
        val updatedData = data.copy(
            m48Bridge = data.m48Bridge.copy(
                status = m48Status.first,
                statusMessage = m48Status.second,
                closures = updatedM48Closures,
                eastbound = recalculateDirectionalStatus(updatedM48Closures, Direction.EASTBOUND),
                westbound = recalculateDirectionalStatus(updatedM48Closures, Direction.WESTBOUND)
            ),
            m4Bridge = data.m4Bridge.copy(
                status = m4Status.first,
                statusMessage = m4Status.second,
                closures = updatedM4Closures,
                eastbound = recalculateDirectionalStatus(updatedM4Closures, Direction.EASTBOUND),
                westbound = recalculateDirectionalStatus(updatedM4Closures, Direction.WESTBOUND)
            ),
            lastUpdated = now
        )
        
        // Store the updated debug data and update UI
        debugData = updatedData
        updateUI(updatedData)
        
        return updatedData
    }
    
    private fun updateWeatherUI(weather: WeatherData) {
        // Current temperature
        binding.weatherTemperature.text = weather.currentTemperature?.let {
            String.format("%.1f°C", it)
        } ?: "--°C"
        
        // Current rain probability
        binding.weatherCurrentRain.text = weather.currentRainProbability?.let {
            "$it%"
        } ?: "--%"
        
        // Current wind speed
        binding.weatherCurrentWind.text = weather.currentWindSpeed?.let {
            String.format("%.0f mph", it)
        } ?: "-- mph"
        
        // Wind status indicator (based on current wind)
        val currentWindRisk = WeatherData.getWindRiskLevel(weather.currentWindSpeed)
        val currentWindColor = when (currentWindRisk) {
            WindRiskLevel.SAFE -> R.color.wind_safe
            WindRiskLevel.MONITOR -> R.color.wind_monitor
            WindRiskLevel.HIGH_RISK -> R.color.wind_high_risk
        }
        binding.weatherWindStatus.setTextColor(ContextCompat.getColor(this, currentWindColor))
        
        // High temperature with time
        if (weather.highTemperature != null) {
            binding.weatherHighTemp.text = String.format("%.1f°C", weather.highTemperature)
            binding.weatherHighTempTime.text = weather.highTempTime?.let { "at $it" } ?: ""
            binding.weatherHighTempTime.visibility = if (weather.highTempTime != null) View.VISIBLE else View.GONE
        } else {
            binding.weatherHighTemp.text = "--°C"
            binding.weatherHighTempTime.visibility = View.GONE
        }
        
        // Low temperature with time
        if (weather.lowTemperature != null) {
            binding.weatherLowTemp.text = String.format("%.1f°C", weather.lowTemperature)
            binding.weatherLowTempTime.text = weather.lowTempTime?.let { "at $it" } ?: ""
            binding.weatherLowTempTime.visibility = if (weather.lowTempTime != null) View.VISIBLE else View.GONE
        } else {
            binding.weatherLowTemp.text = "--°C"
            binding.weatherLowTempTime.visibility = View.GONE
        }
        
        // Rain probability with time
        if (weather.maxRainProbability != null) {
            binding.weatherRainProb.text = "${weather.maxRainProbability}%"
            binding.weatherRainTime.text = weather.rainTime?.let { "at $it" } ?: ""
            binding.weatherRainTime.visibility = if (weather.rainTime != null) View.VISIBLE else View.GONE
        } else {
            binding.weatherRainProb.text = "--"
            binding.weatherRainTime.visibility = View.GONE
        }
        
        // Max wind gust with time and status
        if (weather.maxWindGust != null) {
            binding.weatherMaxGust.text = String.format("%.0f mph", weather.maxWindGust)
            binding.weatherGustTime.text = weather.gustTime?.let { "at $it" } ?: ""
            binding.weatherGustTime.visibility = if (weather.gustTime != null) View.VISIBLE else View.GONE
            
            // Gust status indicator (critical for bridge closures)
            val gustRiskColor = when (weather.windRiskLevel) {
                WindRiskLevel.SAFE -> R.color.wind_safe
                WindRiskLevel.MONITOR -> R.color.wind_monitor
                WindRiskLevel.HIGH_RISK -> R.color.wind_high_risk
            }
            binding.weatherGustStatus.setTextColor(ContextCompat.getColor(this, gustRiskColor))
            binding.weatherGustStatus.visibility = View.VISIBLE
        } else {
            binding.weatherMaxGust.text = "-- mph"
            binding.weatherGustTime.visibility = View.GONE
            binding.weatherGustStatus.visibility = View.GONE
        }
    }
}
