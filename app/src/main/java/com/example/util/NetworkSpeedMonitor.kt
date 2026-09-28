package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors device network conditions and downstream bandwidth in real time.
 * Automatically classifies network capabilities and adapts audio streaming bitrate,
 * enforcing a strict floor of 48 kbps for weak connections.
 */
class NetworkSpeedMonitor(context: Context) {
    companion object {
        private const val TAG = "NetworkSpeedMonitor"
        const val BITRATE_48K = "Low (48 kbps)"
        const val BITRATE_128K = "Medium (128 kbps)"
        const val BITRATE_256K = "High (256 kbps)"
        const val BITRATE_320K = "Ultra (320 kbps)"
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // Real detected downstream bandwidth in kbps
    private val _detectedBandwidthKbps = MutableStateFlow(2500)
    val detectedBandwidthKbps: StateFlow<Int> = _detectedBandwidthKbps.asStateFlow()

    // Network type description (e.g., "Wi-Fi", "Cellular LTE", "Cellular 2G/3G", "Offline")
    private val _networkTypeName = MutableStateFlow("Wi-Fi")
    val networkTypeName: StateFlow<String> = _networkTypeName.asStateFlow()

    // Simulated bandwidth override for Admin testing
    private val _simulatedBandwidthKbps = MutableStateFlow<Int?>(null)
    val simulatedBandwidthKbps: StateFlow<Int?> = _simulatedBandwidthKbps.asStateFlow()

    // Effective adaptive bitrate based on active conditions with lowest 48k floor
    private val _adaptiveBitrate = MutableStateFlow(BITRATE_256K)
    val adaptiveBitrate: StateFlow<String> = _adaptiveBitrate.asStateFlow()

    init {
        updateNetworkState()
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        try {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            connectivityManager?.registerNetworkCallback(
                builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        evaluateCapabilities(networkCapabilities)
                    }

                    override fun onLost(network: Network) {
                        _networkTypeName.value = "Offline / No Signal"
                        _detectedBandwidthKbps.value = 48
                        updateAdaptiveBitrate(48)
                    }

                    override fun onAvailable(network: Network) {
                        connectivityManager.getNetworkCapabilities(network)?.let {
                            evaluateCapabilities(it)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not register NetworkCallback: ${e.message}")
        }
    }

    private fun updateNetworkState() {
        val cm = connectivityManager ?: return
        val activeNet = cm.activeNetwork
        val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null
        if (caps != null) {
            evaluateCapabilities(caps)
        } else {
            // Default baseline
            _detectedBandwidthKbps.value = 2000
            _networkTypeName.value = "Cellular / Mobile"
            updateAdaptiveBitrate(2000)
        }
    }

    private fun evaluateCapabilities(caps: NetworkCapabilities) {
        val downstreamBandwidth = caps.linkDownstreamBandwidthKbps
        val effectiveSpeed = if (downstreamBandwidth > 0) downstreamBandwidth else 2000
        _detectedBandwidthKbps.value = effectiveSpeed

        val type = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                if (effectiveSpeed < 200) "Cellular (2G/Poor)"
                else if (effectiveSpeed < 800) "Cellular (3G)"
                else "Cellular (4G/5G)"
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Mobile Network"
        }
        _networkTypeName.value = type

        val activeBandwidth = _simulatedBandwidthKbps.value ?: effectiveSpeed
        updateAdaptiveBitrate(activeBandwidth)
    }

    private fun updateAdaptiveBitrate(bandwidthKbps: Int) {
        // Enforce adaptive rules with strictly lowest 48k floor:
        // Bandwidth < 250 kbps: 48k (Lowest floor)
        // Bandwidth 250..800 kbps: 128k
        // Bandwidth 801..2500 kbps: 256k
        // Bandwidth > 2500 kbps: 320k
        val bitrate = when {
            bandwidthKbps < 250 -> BITRATE_48K
            bandwidthKbps in 250..800 -> BITRATE_128K
            bandwidthKbps in 801..2500 -> BITRATE_256K
            else -> BITRATE_320K
        }
        _adaptiveBitrate.value = bitrate
    }

    /**
     * Admin tool: simulate a network speed condition in kbps, or null to revert to real hardware.
     */
    fun simulateSpeed(simulatedKbps: Int?) {
        _simulatedBandwidthKbps.value = simulatedKbps
        val targetSpeed = simulatedKbps ?: _detectedBandwidthKbps.value
        updateAdaptiveBitrate(targetSpeed)
    }
}
