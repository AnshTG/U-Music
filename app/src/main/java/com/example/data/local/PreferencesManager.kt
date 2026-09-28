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

    private val _isAdaptiveQualityEnabled = MutableStateFlow(prefs.getBoolean("adaptive_streaming_enabled", true))
    val isAdaptiveQualityEnabled: StateFlow<Boolean> = _isAdaptiveQualityEnabled.asStateFlow()

    private val _streamingQuality = MutableStateFlow(prefs.getString("streaming_quality", "High (256 kbps)") ?: "High (256 kbps)")
    val streamingQuality: StateFlow<String> = _streamingQuality.asStateFlow()

    private val _searchGenreRadioEnabled = MutableStateFlow(prefs.getBoolean("search_genre_radio_enabled", true))
    val searchGenreRadioEnabled: StateFlow<Boolean> = _searchGenreRadioEnabled.asStateFlow()

    private val _excludeSearchRemixes = MutableStateFlow(prefs.getBoolean("exclude_search_remixes", true))
    val excludeSearchRemixes: StateFlow<Boolean> = _excludeSearchRemixes.asStateFlow()

    private val _autoDownloadOfflinePlaylists = MutableStateFlow(prefs.getBoolean("auto_download_offline_playlists", true))
    val autoDownloadOfflinePlaylists: StateFlow<Boolean> = _autoDownloadOfflinePlaylists.asStateFlow()

    private val _searchTimeoutSeconds = MutableStateFlow(prefs.getInt("search_timeout_seconds", 3))
    val searchTimeoutSeconds: StateFlow<Int> = _searchTimeoutSeconds.asStateFlow()

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

    private val _equalizerFeatureEnabled = MutableStateFlow(prefs.getBoolean("feature_equalizer", true))
    val equalizerFeatureEnabled: StateFlow<Boolean> = _equalizerFeatureEnabled.asStateFlow()

    private val _volumeBoosterFeatureEnabled = MutableStateFlow(prefs.getBoolean("feature_volume_booster", true))
    val volumeBoosterFeatureEnabled: StateFlow<Boolean> = _volumeBoosterFeatureEnabled.asStateFlow()

    private val _tagEditorFeatureEnabled = MutableStateFlow(prefs.getBoolean("feature_tag_editor", true))
    val tagEditorFeatureEnabled: StateFlow<Boolean> = _tagEditorFeatureEnabled.asStateFlow()

    private val _audioTrimmerFeatureEnabled = MutableStateFlow(prefs.getBoolean("feature_audio_trimmer", true))
    val audioTrimmerFeatureEnabled: StateFlow<Boolean> = _audioTrimmerFeatureEnabled.asStateFlow()

    private val _localScannerFeatureEnabled = MutableStateFlow(prefs.getBoolean("feature_local_scanner", true))
    val localScannerFeatureEnabled: StateFlow<Boolean> = _localScannerFeatureEnabled.asStateFlow()

    private val _filterShortAudio = MutableStateFlow(prefs.getBoolean("filter_short_audio", true))
    val filterShortAudio: StateFlow<Boolean> = _filterShortAudio.asStateFlow()

    private val _waveformVisualizerEnabled = MutableStateFlow(prefs.getBoolean("waveform_visualizer", true))
    val waveformVisualizerEnabled: StateFlow<Boolean> = _waveformVisualizerEnabled.asStateFlow()

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        val name = prefs.getString("user_name", "Music Explorer") ?: "Music Explorer"
        val age = prefs.getInt("user_age", 22)
        val country = prefs.getString("user_country", "India") ?: "India"
        val completed = prefs.getBoolean("onboarding_complete", false)
        val genresStr = prefs.getString("user_genres", "Bollywood,Pop,Electronic,Lo-Fi,Rock,R&B") ?: "Bollywood,Pop,Electronic,Lo-Fi,Rock,R&B"
        val genres = genresStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val rawMinutes = prefs.getInt("total_minutes_listened", 0)
        val rawSongsPlayed = prefs.getInt("songs_played_count", 0)
        // Reset legacy hardcoded 342 and 89 counters to start cleanly from 0
        val minutes = if (rawMinutes == 342) 0 else rawMinutes
        val songsPlayed = if (rawSongsPlayed == 89) 0 else rawSongsPlayed
        
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

    fun setAdaptiveQualityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("adaptive_streaming_enabled", enabled).apply()
        _isAdaptiveQualityEnabled.value = enabled
    }

    fun setSearchGenreRadioEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("search_genre_radio_enabled", enabled).apply()
        _searchGenreRadioEnabled.value = enabled
    }

    fun setExcludeSearchRemixes(enabled: Boolean) {
        prefs.edit().putBoolean("exclude_search_remixes", enabled).apply()
        _excludeSearchRemixes.value = enabled
    }

    fun setAutoDownloadOfflinePlaylists(enabled: Boolean) {
        prefs.edit().putBoolean("auto_download_offline_playlists", enabled).apply()
        _autoDownloadOfflinePlaylists.value = enabled
    }

    fun setSearchTimeoutSeconds(seconds: Int) {
        prefs.edit().putInt("search_timeout_seconds", seconds).apply()
        _searchTimeoutSeconds.value = seconds
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

    fun setEqualizerFeatureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("feature_equalizer", enabled).apply()
        _equalizerFeatureEnabled.value = enabled
    }

    fun setVolumeBoosterFeatureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("feature_volume_booster", enabled).apply()
        _volumeBoosterFeatureEnabled.value = enabled
    }

    fun setTagEditorFeatureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("feature_tag_editor", enabled).apply()
        _tagEditorFeatureEnabled.value = enabled
    }

    fun setAudioTrimmerFeatureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("feature_audio_trimmer", enabled).apply()
        _audioTrimmerFeatureEnabled.value = enabled
    }

    fun setLocalScannerFeatureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("feature_local_scanner", enabled).apply()
        _localScannerFeatureEnabled.value = enabled
    }

    fun setFilterShortAudio(enabled: Boolean) {
        prefs.edit().putBoolean("filter_short_audio", enabled).apply()
        _filterShortAudio.value = enabled
    }

    fun setWaveformVisualizerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("waveform_visualizer", enabled).apply()
        _waveformVisualizerEnabled.value = enabled
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

    fun resetListeningCounters() {
        val current = _userProfile.value
        prefs.edit()
            .putInt("songs_played_count", 0)
            .putInt("total_minutes_listened", 0)
            .putString("played_genres_history", "")
            .apply()

        _userProfile.value = current.copy(
            songsPlayedCount = 0,
            totalMinutesListened = 0
        )
    }
}
