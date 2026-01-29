package com.severn.bridgemonitor

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
        
        binding.refreshButton.setOnClickListener {
            viewModel.refreshData()
        }
        
        binding.presentPainButton.setOnClickListener {
            showPresentPain()
        }
        
        binding.futurePainButton.setOnClickListener {
            showFuturePain()
        }
        
        // Start with Present Pain view
        showPresentPain()
    }
    
    private fun observeViewModel() {
        viewModel.bridgeData.observe(this) { data ->
            if (data != null) {
                // Data loaded - hide loading message and show content
                binding.loadingMessage.visibility = View.GONE
                binding.presentPainContainer.visibility = if (binding.presentPainButton.isEnabled.not()) View.VISIBLE else View.GONE
                binding.futurePainContainer.visibility = if (binding.futurePainButton.isEnabled.not()) View.VISIBLE else View.GONE
                
                updateUI(data)
                // Update Future Pain view if it's currently visible
                if (binding.futurePainContainer.visibility == View.VISIBLE) {
                    updateFuturePainView(data)
                }
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
        binding.presentPainContainer.visibility = View.VISIBLE
        binding.futurePainContainer.visibility = View.GONE
        
        // Update button styles
        binding.presentPainButton.isEnabled = false
        binding.futurePainButton.isEnabled = true
    }
    
    private fun showFuturePain() {
        binding.presentPainContainer.visibility = View.GONE
        binding.futurePainContainer.visibility = View.VISIBLE
        
        // Update button styles
        binding.presentPainButton.isEnabled = true
        binding.futurePainButton.isEnabled = false
        
        // Update the view with current data
        viewModel.bridgeData.value?.let { updateFuturePainView(it) }
    }
    
    private fun updateUI(data: BridgeData) {
        // Update timestamp
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        binding.lastUpdated.text = "Last updated: ${dateFormat.format(Date(data.lastUpdated))}"
        
        // M48 Bridge
        updateBridgeUI(
            bridge = data.m48Bridge,
            statusCard = binding.m48StatusCard,
            nameText = binding.m48Name,
            statusText = binding.m48Status,
            messageText = binding.m48Message,
            closuresText = binding.m48Closures
        )
        
        // M4 Bridge
        updateBridgeUI(
            bridge = data.m4Bridge,
            statusCard = binding.m4StatusCard,
            nameText = binding.m4Name,
            statusText = binding.m4Status,
            messageText = binding.m4Message,
            closuresText = binding.m4Closures
        )
    }
    
    private fun updateBridgeUI(
        bridge: Bridge,
        statusCard: View,
        nameText: android.widget.TextView,
        statusText: android.widget.TextView,
        messageText: android.widget.TextView,
        closuresText: android.widget.TextView
    ) {
        nameText.text = bridge.fullName
        statusText.text = bridge.status.name
        messageText.text = bridge.statusMessage
        
        // Set status color
        val (backgroundColor, textColor) = when (bridge.status) {
            BridgeStatus.OPEN -> Pair(R.color.status_open, R.color.status_open_text)
            BridgeStatus.RESTRICTED -> Pair(R.color.status_restricted, R.color.status_restricted_text)
            BridgeStatus.CLOSED -> Pair(R.color.status_closed, R.color.status_closed_text)
            BridgeStatus.UNKNOWN -> Pair(R.color.status_unknown, R.color.status_unknown_text)
        }
        
        statusCard.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        statusText.setTextColor(ContextCompat.getColor(this, textColor))
        
        // Display closures
        if (bridge.closures.isEmpty()) {
            closuresText.visibility = View.GONE
        } else {
            closuresText.visibility = View.VISIBLE
            val closuresInfo = buildString {
                bridge.closures.forEachIndexed { index, closure ->
                    if (index > 0) append("\n\n")
                    if (closure.isActive) {
                        append("🔴 ACTIVE: ")
                    } else {
                        append("📅 Planned: ")
                    }
                    append(closure.description)
                    
                    // Show both start and end times for planned closures
                    if (!closure.isActive) {
                        if (closure.startTime != null) {
                            append("\nFrom: ${formatTime(closure.startTime)}")
                        }
                        if (closure.endTime != null) {
                            append("\nUntil: ${formatTime(closure.endTime)}")
                        }
                    } else {
                        // For active closures, just show end time
                        if (closure.endTime != null) {
                            append("\nUntil: ${formatTime(closure.endTime)}")
                        }
                    }
                }
            }
            closuresText.text = closuresInfo
        }
    }
    
    private fun updateCountdowns() {
        val data = viewModel.bridgeData.value ?: return
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
            binding.m48CountdownIndicator,
            now
        )
        
        // Update M4 countdown
        updateBridgeCountdown(
            data.m4Bridge,
            binding.m4CountdownContainer,
            binding.m4CountdownText,
            binding.m4CountdownIndicator,
            now
        )
    }
    
    private fun updateBridgeCountdown(
        bridge: Bridge,
        container: View,
        textView: android.widget.TextView,
        indicator: View,
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
                    
                    textView.text = String.format("Closing in: %02d:%02d", minutes, seconds)
                    
                    // Start blinking animation if not already animating
                    if (indicator.animation == null) {
                        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink_animation)
                        indicator.startAnimation(blinkAnimation)
                    }
                } else {
                    container.visibility = View.GONE
                    indicator.clearAnimation()
                }
            } catch (e: Exception) {
                container.visibility = View.GONE
                indicator.clearAnimation()
            }
        } else {
            container.visibility = View.GONE
            indicator.clearAnimation()
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
            
            // Format as: "20:00 on 28 Jan" or just "20:00" if today
            val now = ZonedDateTime.now(ukZone)
            val formatter = if (ukTime.toLocalDate() == now.toLocalDate()) {
                DateTimeFormatter.ofPattern("HH:mm")
            } else {
                DateTimeFormatter.ofPattern("HH:mm 'on' dd MMM")
            }
            
            ukTime.format(formatter)
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
                    statusBadge.text = "🔴 ACTIVE NOW"
                    statusBadge.visibility = View.VISIBLE
                    // Use red background for active closures
                    cardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
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
}
