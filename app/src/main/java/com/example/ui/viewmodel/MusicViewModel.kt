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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // Navigation & Tabs
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

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

    // Streams & Data
    val playbackState: StateFlow<PlaybackUiState> = playerController.uiState

    val likedSongs: StateFlow<List<Song>> = repository.likedSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val downloadedSongs: StateFlow<List<Song>> = repository.downloadedSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val localDeviceSongs: StateFlow<List<Song>> = repository.localDeviceSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val userProfile: StateFlow<UserProfile> = preferencesManager.userProfile
    val theme: StateFlow<String> = preferencesManager.theme
    val themeAccent: StateFlow<String> = preferencesManager.themeAccent
    val isAdBlockEnabled: StateFlow<Boolean> = preferencesManager.isAdBlockEnabled
    val streamingQuality: StateFlow<String> = preferencesManager.streamingQuality
    val downloadQuality: StateFlow<String> = preferencesManager.downloadQuality
    val crossfadeSec: StateFlow<Int> = preferencesManager.crossfadeSec
    val gaplessPlayback: StateFlow<Boolean> = preferencesManager.gaplessPlayback
    val dataSaver: StateFlow<Boolean> = preferencesManager.dataSaver
    val normalizeVolume: StateFlow<Boolean> = preferencesManager.normalizeVolume
    val ytBackgroundPlayback: StateFlow<Boolean> = preferencesManager.ytBackgroundPlayback
    val ytPreferStream: StateFlow<Boolean> = preferencesManager.ytPreferStream
    val ytAutoMatch: StateFlow<Boolean> = preferencesManager.ytAutoMatch

    // Search state
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // Local media sorting and filter
    private val _localMediaSortBy = MutableStateFlow("Title")
    val localMediaSortBy: StateFlow<String> = _localMediaSortBy.asStateFlow()

    init {
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
        _currentTab.value = tab
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
            showMessage("Added \"${song.title}\" to Offline Cache")
        }
    }

    fun deleteDownload(song: Song) {
        viewModelScope.launch {
            repository.deleteDownload(song)
        }
    }

    // Search Actions
    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        val currentFilter = _searchState.value.activeFilter
        val initialMatches = repository.searchSongs(query, currentFilter)
        
        _searchState.value = _searchState.value.copy(
            query = query,
            searchResults = if (initialMatches.isNotEmpty()) initialMatches else repository.getOnlineCatalog(),
            isSearching = query.isNotEmpty(),
            isLoading = query.length >= 2
        )

        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(200)
                try {
                    val onlineResults = repository.searchOnlineDirect(query)
                    _searchState.value = _searchState.value.copy(
                        searchResults = onlineResults,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false
                    )
                }
            }
        } else {
            _searchState.value = _searchState.value.copy(isLoading = false)
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
    fun createNewPlaylist(name: String, desc: String = "") {
        viewModelScope.launch {
            val id = repository.createPlaylist(name, desc)
            _showAddToPlaylistDialog.value?.let { song ->
                repository.addSongToPlaylist(id, song.id)
            }
            _showCreatePlaylistDialog.value = false
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _showAddToPlaylistDialog.value = null
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylistId.value == playlistId) {
                _selectedPlaylistId.value = null
            }
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
            repository.scanLocalMedia()
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

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
