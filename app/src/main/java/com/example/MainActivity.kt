package com.example

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.Song
import com.example.service.MusicPlaybackService
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AddTracksToPlaylistDialog
import com.example.ui.components.AudioTrimmerDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.MoveSongToPlaylistDialog
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SongActionMenuSheet
import com.example.ui.components.TagEditorDialog
import com.example.ui.components.VolumeBoosterDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.DownloadedSongsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocalMediaScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.YouScreen
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.UMusicTheme
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()
    private var playbackService: MusicPlaybackService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlaybackService.MusicBinder
            playbackService = binder.getService()
            playbackService?.playerController = viewModel.playerController
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isBound = false
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshLocalMedia()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bind Playback Service
        val intent = Intent(this, MusicPlaybackService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Request storage/audio permissions
        requestRequiredPermissions()

        setContent {
            val theme by viewModel.theme.collectAsState()
            val themeAccent by viewModel.themeAccent.collectAsState()

            UMusicTheme(themePreference = theme, accentPreference = themeAccent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userProfile by viewModel.userProfile.collectAsState()

                    if (!userProfile.isOnboardingCompleted) {
                        OnboardingScreen(
                            onComplete = { newProfile ->
                                viewModel.completeOnboarding(newProfile)
                            }
                        )
                    } else {
                        MainAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}

@Composable
fun MainAppContent(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val likedSongs by viewModel.likedSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val localSongs by viewModel.localDeviceSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val allOnlineSongs by viewModel.allOnlineSongs.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsState()
    val isAdBlockEnabled by viewModel.isAdBlockEnabled.collectAsState()

    var showSearchOverlay by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showAdminPanelScreen by remember { mutableStateOf(false) }
    var showDownloadedSongsScreen by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddTracksToPlaylist by remember { mutableStateOf(false) }

    // Dialog and Sheet States
    val showEqualizerSheet by viewModel.showEqualizerSheet.collectAsState()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
    val showVolumeBoosterDialog by viewModel.showVolumeBoosterDialog.collectAsState()
    val songForActionMenu by viewModel.songForActionMenu.collectAsState()
    val songForTagEditor by viewModel.songForTagEditor.collectAsState()
    val songForTrimmer by viewModel.songForTrimmer.collectAsState()
    val showAddToPlaylistDialog by viewModel.showAddToPlaylistDialog.collectAsState()
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val localSortBy by viewModel.localMediaSortBy.collectAsState()

    val streamingQuality by viewModel.streamingQuality.collectAsState()
    val downloadQuality by viewModel.downloadQuality.collectAsState()
    val crossfadeSec by viewModel.crossfadeSec.collectAsState()
    val gaplessPlayback by viewModel.gaplessPlayback.collectAsState()
    val dataSaver by viewModel.dataSaver.collectAsState()
    val normalizeVolume by viewModel.normalizeVolume.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    val themeAccent by viewModel.themeAccent.collectAsState()
    val ytBackgroundPlayback by viewModel.ytBackgroundPlayback.collectAsState()
    val ytPreferStream by viewModel.ytPreferStream.collectAsState()
    val ytAutoMatch by viewModel.ytAutoMatch.collectAsState()
    val equalizerFeatureEnabled by viewModel.equalizerFeatureEnabled.collectAsState()
    val volumeBoosterFeatureEnabled by viewModel.volumeBoosterFeatureEnabled.collectAsState()
    val tagEditorFeatureEnabled by viewModel.tagEditorFeatureEnabled.collectAsState()
    val audioTrimmerFeatureEnabled by viewModel.audioTrimmerFeatureEnabled.collectAsState()
    val localScannerFeatureEnabled by viewModel.localScannerFeatureEnabled.collectAsState()
    val filterShortAudio by viewModel.filterShortAudio.collectAsState()
    val waveformVisualizerEnabled by viewModel.waveformVisualizerEnabled.collectAsState()
    val isAdaptiveQualityEnabled by viewModel.isAdaptiveQualityEnabled.collectAsState()
    val effectiveStreamingQuality by viewModel.effectiveStreamingQuality.collectAsState()
    val detectedBandwidthKbps by viewModel.detectedBandwidthKbps.collectAsState()
    val networkTypeName by viewModel.networkTypeName.collectAsState()
    val simulatedBandwidthKbps by viewModel.simulatedBandwidthKbps.collectAsState()
    val searchGenreRadioEnabled by viewModel.searchGenreRadioEnabled.collectAsState()
    val excludeSearchRemixes by viewModel.excludeSearchRemixes.collectAsState()
    val currentPlaylistSongs by viewModel.currentPlaylistSongs.collectAsState()
    val songToMove by viewModel.songToMoveBetweenPlaylists.collectAsState()
    val searchTimeoutSeconds by viewModel.searchTimeoutSeconds.collectAsState()
    val autoDownloadOfflinePlaylists by viewModel.autoDownloadOfflinePlaylists.collectAsState()

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Step-by-step Hierarchical Back Navigation Handler
    BackHandler(enabled = true) {
        if (showAdminPanelScreen) {
            showAdminPanelScreen = false
            return@BackHandler
        }
        if (showSettingsScreen) {
            showSettingsScreen = false
            return@BackHandler
        }
        if (showDownloadedSongsScreen) {
            showDownloadedSongsScreen = false
            return@BackHandler
        }
        if (showSearchOverlay) {
            showSearchOverlay = false
            return@BackHandler
        }
        if (showEditProfileDialog) {
            showEditProfileDialog = false
            return@BackHandler
        }
        val handled = viewModel.navigateBackStep()
        if (!handled) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!isPlayerExpanded && !showSettingsScreen && !showDownloadedSongsScreen && !showAdminPanelScreen && selectedPlaylistId == null) {
                Column {
                    // Mini Player
                    if (playbackState.currentSong != null) {
                        MiniPlayer(
                            playbackState = playbackState,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onSkipNextClick = { viewModel.skipNext() },
                            onLikeClick = { playbackState.currentSong?.let { viewModel.toggleLike(it) } },
                            onPlayerClick = { viewModel.setPlayerExpanded(true) },
                            isWaveformEnabled = waveformVisualizerEnabled
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.HOME && !showSearchOverlay,
                            onClick = {
                                showSearchOverlay = false
                                viewModel.selectTab(ScreenTab.HOME)
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.SEARCH || showSearchOverlay,
                            onClick = {
                                showSearchOverlay = false
                                viewModel.selectTab(ScreenTab.SEARCH)
                            },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.EXPLORE && !showSearchOverlay,
                            onClick = {
                                showSearchOverlay = false
                                viewModel.selectTab(ScreenTab.EXPLORE)
                            },
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
                            label = { Text("Explore", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.YOU && !showSearchOverlay,
                            onClick = {
                                showSearchOverlay = false
                                viewModel.selectTab(ScreenTab.YOU)
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = "You") },
                            label = { Text("You", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Views
            when {
                showSettingsScreen -> {
                    SettingsScreen(
                        currentTheme = currentTheme,
                        currentAccent = themeAccent,
                        isAdBlockEnabled = isAdBlockEnabled,
                        isAdaptiveQualityEnabled = isAdaptiveQualityEnabled,
                        effectiveStreamingQuality = effectiveStreamingQuality,
                        detectedBandwidthKbps = detectedBandwidthKbps,
                        networkTypeName = networkTypeName,
                        streamingQuality = streamingQuality,
                        downloadQuality = downloadQuality,
                        crossfadeSec = crossfadeSec,
                        gaplessPlayback = gaplessPlayback,
                        dataSaver = dataSaver,
                        normalizeVolume = normalizeVolume,
                        ytBackgroundPlayback = ytBackgroundPlayback,
                        ytPreferStream = ytPreferStream,
                        ytAutoMatch = ytAutoMatch,
                        onBack = { showSettingsScreen = false },
                        onOpenAdminPanel = {
                            showSettingsScreen = false
                            showAdminPanelScreen = true
                        },
                        onThemeChange = { viewModel.setTheme(it) },
                        onAccentChange = { viewModel.setThemeAccent(it) },
                        onAdBlockChange = { viewModel.setAdBlockEnabled(it) },
                        onAdaptiveQualityEnabledChange = { viewModel.setAdaptiveQualityEnabled(it) },
                        onStreamingQualityChange = { viewModel.setStreamingQuality(it) },
                        onDownloadQualityChange = { viewModel.setDownloadQuality(it) },
                        onCrossfadeChange = { viewModel.setCrossfadeSec(it) },
                        onGaplessChange = { viewModel.setGaplessPlayback(it) },
                        onDataSaverChange = { viewModel.setDataSaver(it) },
                        onNormalizeVolumeChange = { viewModel.setNormalizeVolume(it) },
                        onYtBackgroundPlaybackChange = { viewModel.setYtBackgroundPlayback(it) },
                        onYtPreferStreamChange = { viewModel.setYtPreferStream(it) },
                        onYtAutoMatchChange = { viewModel.setYtAutoMatch(it) }
                    )
                }
                showAdminPanelScreen -> {
                    AdminPanelScreen(
                        equalizerEnabled = equalizerFeatureEnabled,
                        volumeBoosterEnabled = volumeBoosterFeatureEnabled,
                        tagEditorEnabled = tagEditorFeatureEnabled,
                        audioTrimmerEnabled = audioTrimmerFeatureEnabled,
                        localScannerEnabled = localScannerFeatureEnabled,
                        filterShortAudio = filterShortAudio,
                        waveformVisualizerEnabled = waveformVisualizerEnabled,
                        isAdBlockEnabled = isAdBlockEnabled,
                        ytBackgroundPlayback = ytBackgroundPlayback,
                        ytPreferStream = ytPreferStream,
                        ytAutoMatch = ytAutoMatch,
                        dataSaver = dataSaver,
                        normalizeVolume = normalizeVolume,
                        gaplessPlayback = gaplessPlayback,
                        crossfadeSec = crossfadeSec,
                        streamingQuality = streamingQuality,
                        downloadQuality = downloadQuality,
                        currentTheme = currentTheme,
                        currentAccent = themeAccent,
                        isAdaptiveQualityEnabled = isAdaptiveQualityEnabled,
                        effectiveStreamingQuality = effectiveStreamingQuality,
                        detectedBandwidthKbps = detectedBandwidthKbps,
                        networkTypeName = networkTypeName,
                        simulatedBandwidthKbps = simulatedBandwidthKbps,
                        searchGenreRadioEnabled = searchGenreRadioEnabled,
                        excludeSearchRemixes = excludeSearchRemixes,
                        totalMinutesListened = userProfile.totalMinutesListened,
                        songsPlayedCount = userProfile.songsPlayedCount,
                        onEqualizerEnabledChange = { viewModel.setEqualizerFeatureEnabled(it) },
                        onVolumeBoosterEnabledChange = { viewModel.setVolumeBoosterFeatureEnabled(it) },
                        onTagEditorEnabledChange = { viewModel.setTagEditorFeatureEnabled(it) },
                        onAudioTrimmerEnabledChange = { viewModel.setAudioTrimmerFeatureEnabled(it) },
                        onLocalScannerEnabledChange = { viewModel.setLocalScannerFeatureEnabled(it) },
                        onFilterShortAudioChange = { viewModel.setFilterShortAudio(it) },
                        onWaveformVisualizerChange = { viewModel.setWaveformVisualizerEnabled(it) },
                        onAdBlockChange = { viewModel.setAdBlockEnabled(it) },
                        onYtBackgroundPlaybackChange = { viewModel.setYtBackgroundPlayback(it) },
                        onYtPreferStreamChange = { viewModel.setYtPreferStream(it) },
                        onYtAutoMatchChange = { viewModel.setYtAutoMatch(it) },
                        onDataSaverChange = { viewModel.setDataSaver(it) },
                        onNormalizeVolumeChange = { viewModel.setNormalizeVolume(it) },
                        onGaplessChange = { viewModel.setGaplessPlayback(it) },
                        onCrossfadeChange = { viewModel.setCrossfadeSec(it) },
                        onStreamingQualityChange = { viewModel.setStreamingQuality(it) },
                        onDownloadQualityChange = { viewModel.setDownloadQuality(it) },
                        onAdaptiveQualityEnabledChange = { viewModel.setAdaptiveQualityEnabled(it) },
                        onSimulateSpeed = { viewModel.simulateNetworkBandwidth(it) },
                        onSearchGenreRadioEnabledChange = { viewModel.setSearchGenreRadioEnabled(it) },
                        onExcludeSearchRemixesChange = { viewModel.setExcludeSearchRemixes(it) },
                        searchTimeoutSeconds = searchTimeoutSeconds,
                        autoDownloadOfflinePlaylists = autoDownloadOfflinePlaylists,
                        downloadedSongsCount = downloadedSongs.size,
                        onSearchTimeoutSecondsChange = { viewModel.setSearchTimeoutSeconds(it) },
                        onAutoDownloadOfflinePlaylistsChange = { viewModel.setAutoDownloadOfflinePlaylists(it) },
                        onClearAllDownloads = { viewModel.clearAllDownloads() },
                        onClearAllPlaylists = { viewModel.clearAllPlaylists() },
                        onGetCacheSize = { viewModel.getOfflineCacheSizeBytes() },
                        onClearCache = { onDone -> viewModel.clearOfflineCache(onDone) },
                        onResetStats = { viewModel.resetListeningHistory() },
                        onReseedCatalog = { viewModel.reseedCatalog() },
                        onBack = { showAdminPanelScreen = false }
                    )
                }
                showDownloadedSongsScreen -> {
                    DownloadedSongsScreen(
                        downloadedSongs = downloadedSongs,
                        playbackState = playbackState,
                        onBack = { showDownloadedSongsScreen = false },
                        onPlayAll = { songs ->
                            if (songs.isNotEmpty()) viewModel.playSong(songs.first(), songs)
                        },
                        onShuffle = { songs ->
                            if (songs.isNotEmpty()) {
                                viewModel.toggleShuffle()
                                viewModel.playSong(songs.random(), songs)
                            }
                        },
                        onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                        onSongLike = { viewModel.toggleLike(it) },
                        onSongMore = { viewModel.showSongMenu(it) },
                        onDeleteSong = { viewModel.deleteDownload(it) },
                        onClearAll = { viewModel.clearAllDownloads() }
                    )
                }
                selectedPlaylistId != null -> {
                    val activePlaylist = playlists.find { it.id == selectedPlaylistId }
                    if (activePlaylist != null) {
                        PlaylistDetailScreen(
                            playlist = activePlaylist,
                            playlistSongs = currentPlaylistSongs,
                            playbackState = playbackState,
                            onBack = { viewModel.closePlaylist() },
                            onPlayAll = { songs ->
                                if (songs.isNotEmpty()) viewModel.playSong(songs.first(), songs)
                            },
                            onShuffle = { songs ->
                                if (songs.isNotEmpty()) {
                                    viewModel.toggleShuffle()
                                    viewModel.playSong(songs.random(), songs)
                                }
                            },
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onSongLike = { viewModel.toggleLike(it) },
                            onSongMore = { viewModel.showSongMenu(it) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onAddTracksClick = { showAddTracksToPlaylist = true },
                            onRemoveSong = { song -> viewModel.removeSongFromPlaylist(activePlaylist.id, song.id) },
                            onMoveSong = { song -> viewModel.showMoveSongDialog(song, activePlaylist.id) }
                        )
                    }
                }
                showSearchOverlay -> {
                    SearchScreen(
                        searchState = searchState,
                        playbackState = playbackState,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onFilterSelected = { viewModel.onSearchFilterSelected(it) },
                        onClearQuery = { viewModel.clearSearch() },
                        onSongClick = { song, _ -> viewModel.playSongFromSearch(song) },
                        onSongLike = { viewModel.toggleLike(it) },
                        onSongMore = { viewModel.showSongMenu(it) }
                    )
                }
                else -> {
                    when (currentTab) {
                        ScreenTab.HOME -> HomeScreen(
                            userProfile = userProfile,
                            playbackState = playbackState,
                            allSongs = viewModel.repository.getOnlineCatalog(),
                            playlists = playlists,
                            isAdBlockEnabled = isAdBlockEnabled,
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist.id) },
                            onSongLike = { viewModel.toggleLike(it) },
                            onSongMore = { viewModel.showSongMenu(it) },
                            onSearchClick = { viewModel.selectTab(ScreenTab.SEARCH) }
                        )
                        ScreenTab.SEARCH -> SearchScreen(
                            searchState = searchState,
                            playbackState = playbackState,
                            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            onFilterSelected = { viewModel.onSearchFilterSelected(it) },
                            onClearQuery = { viewModel.clearSearch() },
                            onSongClick = { song, _ -> viewModel.playSongFromSearch(song) },
                            onSongLike = { viewModel.toggleLike(it) },
                            onSongMore = { viewModel.showSongMenu(it) }
                        )
                        ScreenTab.EXPLORE -> ExploreScreen(
                            playbackState = playbackState,
                            allSongs = viewModel.repository.getOnlineCatalog(),
                            playlists = playlists,
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist.id) },
                            onGenreClick = { genre ->
                                viewModel.onSearchQueryChanged(genre)
                                viewModel.selectTab(ScreenTab.SEARCH)
                            },
                            onSongLike = { viewModel.toggleLike(it) },
                            onSongMore = { viewModel.showSongMenu(it) }
                        )
                        ScreenTab.YOU -> YouScreen(
                            userProfile = userProfile,
                            likedSongs = likedSongs,
                            downloadedSongs = downloadedSongs,
                            playlists = playlists,
                            playbackState = playbackState,
                            onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                            onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist.id) },
                            onCreatePlaylistClick = { viewModel.showCreatePlaylist(true) },
                            onOpenDownloadedSongs = { showDownloadedSongsScreen = true },
                            onSongLike = { viewModel.toggleLike(it) },
                            onSongMore = { viewModel.showSongMenu(it) },
                            onOpenSettings = { showSettingsScreen = true },
                            onEditProfile = { showEditProfileDialog = true },
                            onOpenAdminPanel = { showAdminPanelScreen = true }
                        )
                    }
                }
            }
        }
    }

    // Animated Full Screen Player Overlay positioned cleanly on top of Scaffold
    AnimatedVisibility(
        visible = isPlayerExpanded,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        FullScreenPlayer(
            playbackState = playbackState,
            onCollapse = { viewModel.setPlayerExpanded(false) },
            onPlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { viewModel.seekTo(it) },
            onSkipNext = { viewModel.skipNext() },
            onSkipPrevious = { viewModel.skipPrevious() },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onCycleRepeat = { viewModel.cycleRepeatMode() },
            onToggleLike = { viewModel.toggleLike(it) },
            onDownload = { viewModel.downloadSong(it) },
            onOpenEqualizer = { viewModel.showEqualizer(true) },
            onOpenSleepTimer = { viewModel.showSleepTimer(true) },
            onOpenVolumeBooster = { viewModel.showVolumeBooster(true) },
            onRemoveFromQueue = { viewModel.removeFromQueue(it) },
            onSelectQueueSong = { viewModel.playSong(it) },
            onOpenInYoutubeMusic = { },
            onToggleYoutubeStream = { viewModel.toggleCloudStreamMode() }
        )
    }
}

    // Modal Sheets & Dialogs
    if (showEqualizerSheet) {
        EqualizerSheet(
            equalizerState = playbackState.equalizerState,
            onEnableChange = { viewModel.setEqualizerEnabled(it) },
            onPresetSelect = { viewModel.setEqualizerPreset(it) },
            onBandGainChange = { band, gain -> viewModel.setBandGain(band, gain) },
            onBassBoostChange = { viewModel.setBassBoost(it) },
            onVirtualizerChange = { viewModel.setVirtualizer(it) },
            onSpeedChange = { viewModel.setPlaybackSpeed(it) },
            onPitchChange = { viewModel.setPitch(it) },
            onDismiss = { viewModel.showEqualizer(false) }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentOption = playbackState.sleepTimerOption,
            remainingSeconds = playbackState.sleepTimerRemainingSeconds,
            onOptionSelected = { viewModel.setSleepTimer(it) },
            onDismiss = { viewModel.showSleepTimer(false) }
        )
    }

    if (showVolumeBoosterDialog) {
        VolumeBoosterDialog(
            currentMultiplier = playbackState.equalizerState.volumeBoostMultiplier,
            onApplyMultiplier = { viewModel.setVolumeMultiplier(it) },
            onDismiss = { viewModel.showVolumeBooster(false) }
        )
    }

    songForActionMenu?.let { song ->
        SongActionMenuSheet(
            song = song,
            onDismiss = { viewModel.showSongMenu(null) },
            onAddToQueue = { viewModel.addToQueue(song) },
            onAddToPlaylist = { viewModel.showAddToPlaylist(song) },
            onToggleLike = { viewModel.toggleLike(song) },
            onDownload = { viewModel.downloadSong(song) },
            onDeleteDownload = if (song.isDownloaded) { { viewModel.deleteDownload(song) } } else null,
            onOpenTagEditor = { viewModel.showTagEditor(song) },
            onOpenTrimmer = { viewModel.showTrimmer(song) },
            onOpenInYoutubeMusic = { },
            onMatchWithYoutube = { viewModel.matchSongWithCloud(song) },
            onRemoveFromCurrentPlaylist = if (selectedPlaylistId != null) { { viewModel.removeSongFromPlaylist(selectedPlaylistId!!, song.id) } } else null,
            onMoveToAnotherPlaylist = if (selectedPlaylistId != null) { { viewModel.showMoveSongDialog(song, selectedPlaylistId!!) } } else null,
            isTagEditorEnabled = tagEditorFeatureEnabled,
            isAudioTrimmerEnabled = audioTrimmerFeatureEnabled
        )
    }

    songForTagEditor?.let { song ->
        TagEditorDialog(
            song = song,
            onSave = { title, artist, album, genre, year ->
                viewModel.saveSongMetadata(song.id, title, artist, album, genre, year)
            },
            onDismiss = { viewModel.showTagEditor(null) }
        )
    }

    songForTrimmer?.let { song ->
        AudioTrimmerDialog(
            song = song,
            onSetRingtone = { startMs, endMs ->
                viewModel.playerController.setABRepeat(startMs, endMs)
            },
            onDismiss = { viewModel.showTrimmer(null) }
        )
    }

    showAddToPlaylistDialog?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onSelectPlaylist = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, song.id)
            },
            onCreateNewPlaylist = {
                viewModel.showCreatePlaylist(true)
            },
            onDismiss = { viewModel.showAddToPlaylist(null) }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onCreate = { title, desc, isOffline ->
                viewModel.createNewPlaylist(title, desc, isOffline)
            },
            onDismiss = { viewModel.showCreatePlaylist(false) }
        )
    }

    songToMove?.let { pair ->
        MoveSongToPlaylistDialog(
            song = pair.first,
            fromPlaylistId = pair.second,
            playlists = playlists,
            onSelectTargetPlaylist = { target ->
                viewModel.moveSongBetweenPlaylists(pair.second, target.id, pair.first.id)
            },
            onDismiss = { viewModel.showMoveSongDialog(null, null) }
        )
    }

    if (showAddTracksToPlaylist && selectedPlaylistId != null) {
        val activePlaylist = playlists.find { it.id == selectedPlaylistId }
        if (activePlaylist != null) {
            AddTracksToPlaylistDialog(
                playlist = activePlaylist,
                allSongs = viewModel.repository.getOnlineCatalog(),
                existingSongIds = currentPlaylistSongs.map { it.id }.toSet(),
                onAddSong = { song ->
                    viewModel.addSongToPlaylist(activePlaylist.id, song.id)
                },
                onDismiss = { showAddTracksToPlaylist = false }
            )
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onSave = { updated ->
                viewModel.updateUserProfile(updated)
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}
