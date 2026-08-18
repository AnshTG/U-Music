package com.example.data.repository

/**
 * Legal Endpoint Ad & Privacy Filter
 * Filters ad-network domains, tracking telemetry, promotional injection beacons,
 * and commercial interstitial triggers at the network/metadata endpoint level
 * in full compliance with lawful network privacy filtering standards.
 */
object AdBlockEndpointFilter {

    // Known ad serving, tracking telemetry, and promotional beacon endpoint hosts
    private val BLOCKED_AD_ENDPOINTS = setOf(
        "adservice.google.com",
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "securepubads.g.doubleclick.net",
        "ads.youtube.com",
        "ad.doubleclick.net",
        "telemetry.adzerk.net",
        "ad-delivery.net",
        "track.adform.net",
        "analytics.admob.com",
        "adserver.adtech.de",
        "events.adnexus.com",
        "stats.g.doubleclick.net",
        "adlog.flashtalking.com",
        "promoted.media.net"
    )

    private val AD_URL_PATTERNS = listOf(
        "/pagead/",
        "/ptracking",
        "/api/stats/ads",
        "/ad_break",
        "&ad_type=",
        "&adformat=",
        "doubleclick.net",
        "adunit",
        "sponsor_interstitial"
    )

    /**
     * Checks whether an endpoint URL is an ad, telemetry tracker, or sponsored interstitial.
     */
    fun isAdEndpoint(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()

        for (endpoint in BLOCKED_AD_ENDPOINTS) {
            if (lower.contains(endpoint)) return true
        }

        for (pattern in AD_URL_PATTERNS) {
            if (lower.contains(pattern)) return true
        }

        return false
    }

    /**
     * Filters out ad or sponsored audio tracks from a music stream queue.
     */
    fun sanitizeStreamingQueue(urls: List<String>): List<String> {
        return urls.filterNot { isAdEndpoint(it) }
    }
}
