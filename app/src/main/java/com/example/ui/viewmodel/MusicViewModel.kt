package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.data.model.Playlist
import com.example.data.model.RepeatMode
import com.example.data.model.SleepTimerOption
import com.example.data.model.Song
import com.example.data.model.UserProfile
import com.example.data.repository.MusicRepository
import com.example.player.MusicPlayerController
import com.example.player.PlaybackUiState
import com.example.util.NetworkSpeedMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class ScreenTab {
    HOME, SEARCH, EXPLORE, YOU
}

data class SearchUiState(
    val query: String = "",
    val activeFilter: String = "All",
    val searchResults: List<Song> = emptyList(),
    val recentSearches: List<String> = listOf("Kesariya", "Arijit Singh", "Midnight Horizon", "Lo-Fi Beats", "Pop Hits"),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = MusicRepository(application, database)
    val preferencesManager = PreferencesManager(application)
    val playerController = MusicPlayerController(application)
    val networkSpeedMonitor = NetworkSpeedMonitor(application)

    // Navigation & Tabs with history stack
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()
    private val tabBackStack = mutableListOf<ScreenTab>()

    private val _selectedPlaylistId = MutableStateFlow<String?>(null)
    val selectedPlaylistId: StateFlow<String?> = _selectedPlaylistId.asStateFlow()

    // Status / User Message Toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun showMessage(message: String) {
        _userMessage.value = message
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Active Dialogs & Sheets
    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _showEqualizerSheet = MutableStateFlow(false)
    val showEqualizerSheet: StateFlow<Boolean> = _showEqualizerSheet.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showVolumeBoosterDialog = MutableStateFlow(false)
    val showVolumeBoosterDialog: StateFlow<Boolean> = _showVolumeBoosterDialog.asStateFlow()

    private val _songForActionMenu = MutableStateFlow<Song?>(null)
    val songForActionMenu: StateFlow<Song?> = _songForActionMenu.asStateFlow()

    private val _songForTagEditor = MutableStateFlow<Song?>(null)
    val songForTagEditor: StateFlow<Song?> = _songForTagEditor.asStateFlow()

    private val _songForTrimmer = MutableStateFlow<Song?>(null)
    val songForTrimmer: StateFlow<Song?> = _songForTrimmer.asStateFlow()

    private val _showAddToPlaylistDialog = MutableStateFlow<Song?>(null)
    val showAddToPlaylistDialog: StateFlow<Song?> = _showAddToPlaylistDialog.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _songToMoveBetweenPlaylists = MutableStateFlow<Pair<Song, String>?>(null)
    val songToMoveBetweenPlaylists: StateFlow<Pair<Song, String>?> = _songToMoveBetweenPlaylists.asStateFlow()

    // Streams & Data
    val playbackState: StateFlow<PlaybackUiState> = playerController.uiState

    val likedSongs: StateFlow<List<Song>> = repository.likedSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allOnlineSongs: StateFlow<List<Song>> = repository.onlineCatalogFlow

    val downloadedSongs: StateFlow<List<Song>> = repository.downloadedSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val localDeviceSongs: StateFlow<List<Song>> = repository.localDeviceSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylistId
        .flatMapLatest { id ->
            if (id != null) repository.getSongsForPlaylist(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = preferencesManager.userProfile
    val theme: StateFlow<String> = preferencesManager.theme
    val themeAccent: StateFlow<String> = preferencesManager.themeAccent
    val isAdBlockEnabled: StateFlow<Boolean> = preferencesManager.isAdBlockEnabled
    val isAdaptiveQualityEnabled: StateFlow<Boolean> = preferencesManager.isAdaptiveQualityEnabled
    val streamingQuality: StateFlow<String> = preferencesManager.streamingQuality
    val detectedBandwidthKbps: StateFlow<Int> = networkSpeedMonitor.detectedBandwidthKbps
    val networkTypeName: StateFlow<String> = networkSpeedMonitor.networkTypeName
    val simulatedBandwidthKbps: StateFlow<Int?> = networkSpeedMonitor.simulatedBandwidthKbps

    val effectiveStreamingQuality: StateFlow<String> = combine(
        preferencesManager.isAdaptiveQualityEnabled,
        preferencesManager.streamingQuality,
        networkSpeedMonitor.adaptiveBitrate
    ) { adaptiveEnabled, manualQuality, autoQuality ->
        if (adaptiveEnabled) autoQuality else manualQuality
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NetworkSpeedMonitor.BITRATE_256K)

    val searchGenreRadioEnabled: StateFlow<Boolean> = preferencesManager.searchGenreRadioEnabled
    val excludeSearchRemixes: StateFlow<Boolean> = preferencesManager.excludeSearchRemixes
    val autoDownloadOfflinePlaylists: StateFlow<Boolean> = preferencesManager.autoDownloadOfflinePlaylists
    val searchTimeoutSeconds: StateFlow<Int> = preferencesManager.searchTimeoutSeconds

    val downloadQuality: StateFlow<String> = preferencesManager.downloadQuality
    val crossfadeSec: StateFlow<Int> = preferencesManager.crossfadeSec
    val gaplessPlayback: StateFlow<Boolean> = preferencesManager.gaplessPlayback
    val dataSaver: StateFlow<Boolean> = preferencesManager.dataSaver
    val normalizeVolume: StateFlow<Boolean> = preferencesManager.normalizeVolume
    val ytBackgroundPlayback: StateFlow<Boolean> = preferencesManager.ytBackgroundPlayback
    val ytPreferStream: StateFlow<Boolean> = preferencesManager.ytPreferStream
    val ytAutoMatch: StateFlow<Boolean> = preferencesManager.ytAutoMatch
    val equalizerFeatureEnabled: StateFlow<Boolean> = preferencesManager.equalizerFeatureEnabled
    val volumeBoosterFeatureEnabled: StateFlow<Boolean> = preferencesManager.volumeBoosterFeatureEnabled
    val tagEditorFeatureEnabled: StateFlow<Boolean> = preferencesManager.tagEditorFeatureEnabled
    val audioTrimmerFeatureEnabled: StateFlow<Boolean> = preferencesManager.audioTrimmerFeatureEnabled
    val localScannerFeatureEnabled: StateFlow<Boolean> = preferencesManager.localScannerFeatureEnabled
    val filterShortAudio: StateFlow<Boolean> = preferencesManager.filterShortAudio
    val waveformVisualizerEnabled: StateFlow<Boolean> = preferencesManager.waveformVisualizerEnabled

    // Search state
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // Local media sorting and filter
    private val _localMediaSortBy = MutableStateFlow("Title")
    val localMediaSortBy: StateFlow<String> = _localMediaSortBy.asStateFlow()

    init {
        viewModelScope.launch {
            effectiveStreamingQuality.collect { quality ->
                playerController.activeStreamingQuality = quality
            }
        }

        viewModelScope.launch {
            val profile = preferencesManager.userProfile.value
            repository.initializeCatalog(profile.country, profile.age)
            repository.scanLocalMedia()
            _searchState.value = _searchState.value.copy(
                searchResults = repository.getOnlineCatalog()
            )
            // Fetch live online suggestions for the user's region
            repository.refreshCatalogForRegion(profile.country, profile.age)
        }
    }

    fun selectTab(tab: ScreenTab) {
        if (_currentTab.value != tab) {
            tabBackStack.add(_currentTab.value)
            _currentTab.value = tab
        }
    }

    /**
     * Hierarchical step-by-step back navigation.
     * Navigates one step back:
     * 1. Collapse full-screen player if expanded.
     * 2. Close active modal sheets and dialogs.
     * 3. Close playlist detail view.
     * 4. Clear search query if active.
     * 5. Pop tab history, stepping back toward Home tab.
     * Returns true if handled, or false if already at root Home tab.
     */
    fun navigateBackStep(): Boolean {
        if (_isPlayerExpanded.value) {
            _isPlayerExpanded.value = false
            return true
        }
        if (_showEqualizerSheet.value) {
            _showEqualizerSheet.value = false
            return true
        }
        if (_showSleepTimerDialog.value) {
            _showSleepTimerDialog.value = false
            return true
        }
        if (_showVolumeBoosterDialog.value) {
            _showVolumeBoosterDialog.value = false
            return true
        }
        if (_songForActionMenu.value != null) {
            _songForActionMenu.value = null
            return true
        }
        if (_songForTagEditor.value != null) {
            _songForTagEditor.value = null
            return true
        }
        if (_songForTrimmer.value != null) {
            _songForTrimmer.value = null
            return true
        }
        if (_showAddToPlaylistDialog.value != null) {
            _showAddToPlaylistDialog.value = null
            return true
        }
        if (_showCreatePlaylistDialog.value) {
            _showCreatePlaylistDialog.value = false
            return true
        }
        if (_selectedPlaylistId.value != null) {
            _selectedPlaylistId.value = null
            return true
        }
        if (_searchState.value.query.isNotEmpty()) {
            clearSearch()
            return true
        }
        if (tabBackStack.isNotEmpty()) {
            val prevTab = tabBackStack.removeAt(tabBackStack.lastIndex)
            _currentTab.value = prevTab
            return true
        }
        if (_currentTab.value != ScreenTab.HOME) {
            _currentTab.value = ScreenTab.HOME
            return true
        }
        return false
    }

    fun openPlaylist(playlistId: String) {
        _selectedPlaylistId.value = playlistId
    }

    fun closePlaylist() {
        _selectedPlaylistId.value = null
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    // Playback actions
    fun playSong(song: Song, queue: List<Song>? = null) {
        playerController.playSong(song, queue)
        preferencesManager.recordSongPlay(song.genre, song.artist)
    }

    /**
     * Plays a song initiated from Search.
     * Rather than enqueuing repetitive remixes or alterations of the same searched track,
     * builds a diverse genre radio mix starting from the selected song's genre.
     */
    fun playSongFromSearch(selectedSong: Song) {
        val allSongs = repository.getOnlineCatalog()
        val targetGenre = selectedSong.genre.ifBlank { "Pop" }
        val isRadioEnabled = preferencesManager.searchGenreRadioEnabled.value
        val excludeRemixes = preferencesManager.excludeSearchRemixes.value

        val baseTitle = cleanBaseTitle(selectedSong.title)

        val queue = if (isRadioEnabled) {
            // Find songs from the catalog matching this genre, excluding remixes/variations of the same title
            val matchingGenreSongs = allSongs.filter { other ->
                other.id != selectedSong.id &&
                (other.genre.contains(targetGenre, ignoreCase = true) || targetGenre.contains(other.genre, ignoreCase = true)) &&
                (!excludeRemixes || !isSameSongVariation(baseTitle, other.title))
            }.shuffled()

            val diverseList = mutableListOf<Song>()
            diverseList.add(selectedSong)
            diverseList.addAll(matchingGenreSongs)

            // If genre songs are sparse, top up with other diverse hits excluding variations
            if (diverseList.size < 10) {
                val supplemental = allSongs.filter { other ->
                    diverseList.none { it.id == other.id } &&
                    (!excludeRemixes || !isSameSongVariation(baseTitle, other.title))
                }.shuffled().take(15 - diverseList.size)
                diverseList.addAll(supplemental)
            }
            diverseList
        } else {
            listOf(selectedSong)
        }

        playSong(selectedSong, queue)
        showMessage("Playing ${selectedSong.title} • Starting ${targetGenre} Radio Mix")
    }

    private fun cleanBaseTitle(title: String): String {
        return title
            .replace(Regex("(?i)\\s*[\\[\\(].*?[\\]\\)]"), "")
            .replace(Regex("(?i)\\s*-\\s*(remix|lo-?fi|acoustic|version|mix|edit).*"), "")
            .trim()
            .lowercase()
    }

    private fun isSameSongVariation(baseTitle: String, otherTitle: String): Boolean {
        if (baseTitle.isBlank()) return false
        val cleanOther = cleanBaseTitle(otherTitle)
        return cleanOther == baseTitle || cleanOther.contains(baseTitle) || baseTitle.contains(cleanOther)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun skipNext() {
        playerController.skipNext()
    }

    fun skipPrevious() {
        playerController.skipPrevious()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerController.cycleRepeatMode()
    }

    fun toggleLike(song: Song) {
        viewModelScope.launch {
            repository.toggleLike(song)
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            repository.downloadSong(song, downloadQuality.value)
            showMessage("Added \"${song.title}\" to Downloaded Songs")
        }
    }

    fun deleteDownload(song: Song) {
        viewModelScope.launch {
            repository.deleteDownload(song)
            showMessage("Removed \"${song.title}\" from Downloaded Songs")
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearAllDownloads()
            showMessage("Cleared all Downloaded Songs")
        }
    }

    // Search Actions
    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        val currentFilter = _searchState.value.activeFilter
        val initialMatches = repository.searchSongs(query, currentFilter)
        
        _searchState.value = _searchState.value.copy(
            query = query,
            searchResults = if (initialMatches.isNotEmpty()) initialMatches else if (query.isEmpty()) repository.getOnlineCatalog() else emptyList(),
            isSearching = query.isNotEmpty(),
            isLoading = query.trim().length >= 2
        )

        searchJob?.cancel()
        if (query.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                delay(200)
                try {
                    val timeoutMs = (searchTimeoutSeconds.value.coerceAtLeast(2) * 1000L)
                    val onlineResults = withTimeoutOrNull(timeoutMs) {
                        repository.searchOnlineDirect(query.trim())
                    }
                    val finalResults = if (!onlineResults.isNullOrEmpty()) {
                        onlineResults
                    } else {
                        repository.searchSongs(query.trim(), _searchState.value.activeFilter)
                    }
                    _searchState.value = _searchState.value.copy(
                        searchResults = finalResults,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _searchState.value = _searchState.value.copy(
                        searchResults = repository.searchSongs(query.trim(), _searchState.value.activeFilter),
                        isLoading = false
                    )
                } finally {
                    _searchState.value = _searchState.value.copy(isLoading = false)
                }
            }
        } else {
            _searchState.value = _searchState.value.copy(
                isLoading = false,
                searchResults = if (query.isEmpty()) repository.getOnlineCatalog() else repository.searchSongs(query, currentFilter)
            )
        }
    }

    fun onSearchFilterSelected(filter: String) {
        _searchState.value = _searchState.value.copy(
            activeFilter = filter,
            searchResults = repository.searchSongs(_searchState.value.query, filter)
        )
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = _searchState.value.copy(
            query = "",
            isSearching = false,
            isLoading = false,
            searchResults = repository.getOnlineCatalog()
        )
    }

    // Equalizer & FX
    fun setEqualizerEnabled(enabled: Boolean) {
        val current = playbackState.value.equalizerState
        val updated = current.copy(isEnabled = enabled)
        playerController.applyEqualizerSettings(updated)
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        val current = playbackState.value.equalizerState
        val updatedBands = current.bands.mapIndexed { idx, band ->
            val gain = if (idx < preset.gains.size) preset.gains[idx] else 0f
            band.copy(gainDb = gain)
        }
        val updated = current.copy(
            currentPreset = preset.name,
            bands = updatedBands
        )
        playerController.applyEqualizerSettings(updated)
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        val current = playbackState.value.equalizerState
        val updatedBands = current.bands.toMutableList()
        if (bandIndex in updatedBands.indices) {
            updatedBands[bandIndex] = updatedBands[bandIndex].copy(gainDb = gainDb)
            val updated = current.copy(
                currentPreset = "Custom",
                bands = updatedBands
            )
            playerController.applyEqualizerSettings(updated)
        }
    }

    fun setBassBoost(strength: Float) {
        val current = playbackState.value.equalizerState
        val updated = current.copy(bassBoost = strength)
        playerController.applyEqualizerSettings(updated)
    }

    fun setVirtualizer(strength: Float) {
        val current = playbackState.value.equalizerState
        val updated = current.copy(virtualizer = strength)
        playerController.applyEqualizerSettings(updated)
    }

    fun setVolumeMultiplier(multiplier: Float) {
        val current = playbackState.value.equalizerState
        val updated = current.copy(volumeBoostMultiplier = multiplier)
        playerController.applyEqualizerSettings(updated)
    }

    fun setPlaybackSpeed(speed: Float) {
        val current = playbackState.value.equalizerState
        val updated = current.copy(playbackSpeed = speed)
        playerController.applyEqualizerSettings(updated)
    }

    fun setPitch(pitch: Float) {
        val current = playbackState.value.equalizerState
        val updated = current.copy(pitch = pitch)
        playerController.applyEqualizerSettings(updated)
    }

    // Sleep Timer
    fun setSleepTimer(option: SleepTimerOption) {
        playerController.setSleepTimer(option)
    }

    fun cancelSleepTimer() {
        playerController.cancelSleepTimer()
    }

    // Queue
    fun reorderQueue(from: Int, to: Int) {
        playerController.reorderQueue(from, to)
    }

    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }

    fun addToQueue(song: Song) {
        playerController.addToQueue(song)
    }

    // Dialog & Sheet Controls
    fun showEqualizer(show: Boolean) { _showEqualizerSheet.value = show }
    fun showSleepTimer(show: Boolean) { _showSleepTimerDialog.value = show }
    fun showVolumeBooster(show: Boolean) { _showVolumeBoosterDialog.value = show }
    fun showSongMenu(song: Song?) { _songForActionMenu.value = song }
    fun showTagEditor(song: Song?) { _songForTagEditor.value = song }
    fun showTrimmer(song: Song?) { _songForTrimmer.value = song }
    fun showAddToPlaylist(song: Song?) { _showAddToPlaylistDialog.value = song }
    fun showCreatePlaylist(show: Boolean) { _showCreatePlaylistDialog.value = show }

    // Playlist Management
    fun createNewPlaylist(name: String, desc: String = "", isOffline: Boolean = false) {
        viewModelScope.launch {
            val id = repository.createPlaylist(name, desc, isOffline)
            _showAddToPlaylistDialog.value?.let { song ->
                repository.addSongToPlaylist(id, song.id)
                if (isOffline) {
                    showMessage("Created offline playlist & downloaded \"${song.title}\"")
                } else {
                    showMessage("Created playlist \"$name\"")
                }
            } ?: run {
                showMessage(if (isOffline) "Created empty Offline Playlist" else "Created empty Online Playlist")
            }
            _showCreatePlaylistDialog.value = false
            _showAddToPlaylistDialog.value = null
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            val playlist = allPlaylists.value.find { it.id == playlistId }
            if (playlist != null && playlist.isOffline) {
                showMessage("Added to Offline Playlist & downloaded for offline playback")
            } else {
                showMessage("Added song to playlist")
            }
            _showAddToPlaylistDialog.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            showMessage("Removed track from playlist")
        }
    }

    fun moveSongBetweenPlaylists(fromPlaylistId: String, toPlaylistId: String, songId: String) {
        viewModelScope.launch {
            repository.moveSongBetweenPlaylists(fromPlaylistId, toPlaylistId, songId)
            val target = allPlaylists.value.find { it.id == toPlaylistId }
            if (target != null && target.isOffline) {
                showMessage("Moved track & downloaded for offline playback")
            } else {
                showMessage("Moved track to new playlist")
            }
            _songToMoveBetweenPlaylists.value = null
        }
    }

    fun showMoveSongDialog(song: Song?, fromPlaylistId: String?) {
        _songToMoveBetweenPlaylists.value = if (song != null && fromPlaylistId != null) Pair(song, fromPlaylistId) else null
    }

    fun clearAllPlaylists() {
        viewModelScope.launch {
            repository.clearAllPlaylists()
            _selectedPlaylistId.value = null
            showMessage("All playlists deleted and reset")
        }
    }

    fun setAutoDownloadOfflinePlaylists(enabled: Boolean) {
        preferencesManager.setAutoDownloadOfflinePlaylists(enabled)
    }

    fun setSearchTimeoutSeconds(seconds: Int) {
        preferencesManager.setSearchTimeoutSeconds(seconds)
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylistId.value == playlistId) {
                _selectedPlaylistId.value = null
            }
            showMessage("Playlist deleted")
        }
    }

    // Local Tag Editor
    fun saveSongMetadata(songId: String, title: String, artist: String, album: String, genre: String, year: Int) {
        viewModelScope.launch {
            repository.updateSongMetadata(songId, title, artist, album, genre, year)
            _songForTagEditor.value = null
            repository.scanLocalMedia()
        }
    }

    // Local Scanner
    fun refreshLocalMedia() {
        viewModelScope.launch {
            repository.scanLocalMedia(filterShortAudio.value)
        }
    }

    fun setLocalMediaSort(sort: String) {
        _localMediaSortBy.value = sort
    }

    // Onboarding & Profile
    fun completeOnboarding(profile: UserProfile) {
        val updated = profile.copy(isOnboardingCompleted = true)
        preferencesManager.updateUserProfile(updated)
        viewModelScope.launch {
            repository.initializeCatalog(updated.country, updated.age)
            _searchState.value = _searchState.value.copy(
                searchResults = repository.getOnlineCatalog()
            )
            repository.refreshCatalogForRegion(updated.country, updated.age)
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        preferencesManager.updateUserProfile(profile)
        viewModelScope.launch {
            repository.refreshCatalogForRegion(profile.country, profile.age)
        }
    }

    fun setTheme(theme: String) {
        preferencesManager.setTheme(theme)
    }

    fun setThemeAccent(accent: String) {
        preferencesManager.setThemeAccent(accent)
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        preferencesManager.setAdBlockEnabled(enabled)
    }

    fun setStreamingQuality(quality: String) {
        preferencesManager.setStreamingQuality(quality)
    }

    fun setDownloadQuality(quality: String) {
        preferencesManager.setDownloadQuality(quality)
    }

    fun setCrossfadeSec(sec: Int) {
        preferencesManager.setCrossfadeSec(sec)
    }

    fun setGaplessPlayback(enabled: Boolean) {
        preferencesManager.setGaplessPlayback(enabled)
    }

    fun setDataSaver(enabled: Boolean) {
        preferencesManager.setDataSaver(enabled)
    }

    fun setNormalizeVolume(enabled: Boolean) {
        preferencesManager.setNormalizeVolume(enabled)
    }

    fun setYtBackgroundPlayback(enabled: Boolean) {
        preferencesManager.setYtBackgroundPlayback(enabled)
    }

    fun setYtPreferStream(enabled: Boolean) {
        preferencesManager.setYtPreferStream(enabled)
    }

    fun setYtAutoMatch(enabled: Boolean) {
        preferencesManager.setYtAutoMatch(enabled)
    }

    fun toggleCloudStreamMode() {
        playerController.toggleCloudStreamSource()
    }

    fun matchSongWithCloud(song: Song) {
        val matched = repository.matchSongWithYoutube(song)
        playSong(matched)
    }

    // Feature Flags for Admin Panel
    fun setEqualizerFeatureEnabled(enabled: Boolean) {
        preferencesManager.setEqualizerFeatureEnabled(enabled)
    }

    fun setVolumeBoosterFeatureEnabled(enabled: Boolean) {
        preferencesManager.setVolumeBoosterFeatureEnabled(enabled)
    }

    fun setTagEditorFeatureEnabled(enabled: Boolean) {
        preferencesManager.setTagEditorFeatureEnabled(enabled)
    }

    fun setAudioTrimmerFeatureEnabled(enabled: Boolean) {
        preferencesManager.setAudioTrimmerFeatureEnabled(enabled)
    }

    fun setLocalScannerFeatureEnabled(enabled: Boolean) {
        preferencesManager.setLocalScannerFeatureEnabled(enabled)
    }

    fun setFilterShortAudio(enabled: Boolean) {
        preferencesManager.setFilterShortAudio(enabled)
    }

    fun setWaveformVisualizerEnabled(enabled: Boolean) {
        preferencesManager.setWaveformVisualizerEnabled(enabled)
    }

    // Admin Panel Diagnostics and Maintenance Actions
    fun getOfflineCacheSizeBytes(): Long {
        return repository.getOfflineCacheSizeBytes()
    }

    fun clearOfflineCache(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = repository.clearAllOfflineCache()
            showMessage("Cleared $count offline cached tracks")
            onComplete(count)
        }
    }

    fun setAdaptiveQualityEnabled(enabled: Boolean) {
        preferencesManager.setAdaptiveQualityEnabled(enabled)
        showMessage(if (enabled) "Adaptive stream quality enabled (Auto 48k floor)" else "Manual bitrate override active")
    }

    fun simulateNetworkBandwidth(kbps: Int?) {
        networkSpeedMonitor.simulateSpeed(kbps)
        showMessage(if (kbps != null) "Simulating ${kbps} kbps network condition" else "Reset to live network conditions")
    }

    fun setSearchGenreRadioEnabled(enabled: Boolean) {
        preferencesManager.setSearchGenreRadioEnabled(enabled)
        showMessage(if (enabled) "Search Genre Radio queue mix enabled" else "Search queue mix disabled")
    }

    fun setExcludeSearchRemixes(enabled: Boolean) {
        preferencesManager.setExcludeSearchRemixes(enabled)
        showMessage(if (enabled) "Duplicate titles & remix filtering active" else "Remix filtering disabled")
    }

    fun resetListeningHistory() {
        preferencesManager.resetListeningCounters()
        showMessage("Listening history and play counts reset to 0")
    }

    fun reseedCatalog() {
        viewModelScope.launch {
            val profile = userProfile.value
            repository.reseedDefaultCatalog(profile.country, profile.age)
            _searchState.value = _searchState.value.copy(
                searchResults = repository.getOnlineCatalog()
            )
            showMessage("Catalog reseeded with fresh releases and playlists")
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
