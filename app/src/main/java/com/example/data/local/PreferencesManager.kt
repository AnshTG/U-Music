package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("u_music_settings", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(prefs.getString("theme", "Dark") ?: "Dark")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _themeAccent = MutableStateFlow(prefs.getString("theme_accent", "Violet") ?: "Violet")
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    private val _isAdBlockEnabled = MutableStateFlow(prefs.getBoolean("ad_block", true))
    val isAdBlockEnabled: StateFlow<Boolean> = _isAdBlockEnabled.asStateFlow()

    private val _streamingQuality = MutableStateFlow(prefs.getString("streaming_quality", "High (256 kbps)") ?: "High (256 kbps)")
    val streamingQuality: StateFlow<String> = _streamingQuality.asStateFlow()

    private val _downloadQuality = MutableStateFlow(prefs.getString("download_quality", "High (320 kbps)") ?: "High (320 kbps)")
    val downloadQuality: StateFlow<String> = _downloadQuality.asStateFlow()

    private val _crossfadeSec = MutableStateFlow(prefs.getInt("crossfade_sec", 3))
    val crossfadeSec: StateFlow<Int> = _crossfadeSec.asStateFlow()

    private val _gaplessPlayback = MutableStateFlow(prefs.getBoolean("gapless", true))
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()

    private val _dataSaver = MutableStateFlow(prefs.getBoolean("data_saver", false))
    val dataSaver: StateFlow<Boolean> = _dataSaver.asStateFlow()

    private val _normalizeVolume = MutableStateFlow(prefs.getBoolean("normalize_vol", true))
    val normalizeVolume: StateFlow<Boolean> = _normalizeVolume.asStateFlow()

    private val _ytBackgroundPlayback = MutableStateFlow(prefs.getBoolean("yt_background_playback", true))
    val ytBackgroundPlayback: StateFlow<Boolean> = _ytBackgroundPlayback.asStateFlow()

    private val _ytPreferStream = MutableStateFlow(prefs.getBoolean("yt_prefer_stream", true))
    val ytPreferStream: StateFlow<Boolean> = _ytPreferStream.asStateFlow()

    private val _ytAutoMatch = MutableStateFlow(prefs.getBoolean("yt_auto_match", true))
    val ytAutoMatch: StateFlow<Boolean> = _ytAutoMatch.asStateFlow()

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        val name = prefs.getString("user_name", "Music Explorer") ?: "Music Explorer"
        val age = prefs.getInt("user_age", 22)
        val country = prefs.getString("user_country", "India") ?: "India"
        val completed = prefs.getBoolean("onboarding_complete", false)
        val genresStr = prefs.getString("user_genres", "Bollywood,Pop,Electronic,Lo-Fi,Rock,R&B") ?: "Bollywood,Pop,Electronic,Lo-Fi,Rock,R&B"
        val genres = genresStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val minutes = prefs.getInt("total_minutes_listened", 0)
        val songsPlayed = prefs.getInt("songs_played_count", 0)
        
        return UserProfile(
            name = name,
            age = age,
            country = country,
            favoriteGenres = genres,
            isOnboardingCompleted = completed,
            totalMinutesListened = minutes,
            songsPlayedCount = songsPlayed,
            topGenre = genres.firstOrNull() ?: "Bollywood",
            topArtist = prefs.getString("last_played_artist", "Arijit Singh") ?: "Arijit Singh"
        )
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _theme.value = theme
    }

    fun setThemeAccent(accent: String) {
        prefs.edit().putString("theme_accent", accent).apply()
        _themeAccent.value = accent
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ad_block", enabled).apply()
        _isAdBlockEnabled.value = enabled
    }

    fun setStreamingQuality(quality: String) {
        prefs.edit().putString("streaming_quality", quality).apply()
        _streamingQuality.value = quality
    }

    fun setDownloadQuality(quality: String) {
        prefs.edit().putString("download_quality", quality).apply()
        _downloadQuality.value = quality
    }

    fun setCrossfadeSec(sec: Int) {
        prefs.edit().putInt("crossfade_sec", sec).apply()
        _crossfadeSec.value = sec
    }

    fun setGaplessPlayback(enabled: Boolean) {
        prefs.edit().putBoolean("gapless", enabled).apply()
        _gaplessPlayback.value = enabled
    }

    fun setDataSaver(enabled: Boolean) {
        prefs.edit().putBoolean("data_saver", enabled).apply()
        _dataSaver.value = enabled
    }

    fun setNormalizeVolume(enabled: Boolean) {
        prefs.edit().putBoolean("normalize_vol", enabled).apply()
        _normalizeVolume.value = enabled
    }

    fun setYtBackgroundPlayback(enabled: Boolean) {
        prefs.edit().putBoolean("yt_background_playback", enabled).apply()
        _ytBackgroundPlayback.value = enabled
    }

    fun setYtPreferStream(enabled: Boolean) {
        prefs.edit().putBoolean("yt_prefer_stream", enabled).apply()
        _ytPreferStream.value = enabled
    }

    fun setYtAutoMatch(enabled: Boolean) {
        prefs.edit().putBoolean("yt_auto_match", enabled).apply()
        _ytAutoMatch.value = enabled
    }

    fun updateUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString("user_name", profile.name)
            .putInt("user_age", profile.age)
            .putString("user_country", profile.country)
            .putBoolean("onboarding_complete", profile.isOnboardingCompleted)
            .putString("user_genres", profile.favoriteGenres.joinToString(","))
            .putInt("total_minutes_listened", profile.totalMinutesListened)
            .putInt("songs_played_count", profile.songsPlayedCount)
            .apply()
        _userProfile.value = profile
    }

    fun recordSongPlay(genre: String, artist: String) {
        val current = _userProfile.value
        val historyStr = prefs.getString("played_genres_history", "") ?: ""
        val historyList = if (historyStr.isNotEmpty()) historyStr.split(",").toMutableList() else mutableListOf()
        if (genre.isNotBlank()) {
            historyList.add(0, genre)
            if (historyList.size > 50) historyList.removeAt(historyList.lastIndex)
        }
        val updatedHistory = historyList.joinToString(",")
        
        // Find most played genre
        val topGenre = historyList.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: current.topGenre
        
        prefs.edit()
            .putString("played_genres_history", updatedHistory)
            .putString("last_played_artist", artist)
            .putInt("songs_played_count", current.songsPlayedCount + 1)
            .putInt("total_minutes_listened", current.totalMinutesListened + 3)
            .apply()

        _userProfile.value = current.copy(
            songsPlayedCount = current.songsPlayedCount + 1,
            totalMinutesListened = current.totalMinutesListened + 3,
            topGenre = topGenre,
            topArtist = if (artist.isNotBlank()) artist else current.topArtist
        )
    }
}
