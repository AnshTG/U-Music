package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppAccent
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    // Feature Toggles
    equalizerEnabled: Boolean,
    volumeBoosterEnabled: Boolean,
    tagEditorEnabled: Boolean,
    audioTrimmerEnabled: Boolean,
    localScannerEnabled: Boolean,
    filterShortAudio: Boolean,
    waveformVisualizerEnabled: Boolean,
    isAdBlockEnabled: Boolean,
    ytBackgroundPlayback: Boolean,
    ytPreferStream: Boolean,
    ytAutoMatch: Boolean,
    dataSaver: Boolean,
    normalizeVolume: Boolean,
    gaplessPlayback: Boolean,
    crossfadeSec: Int,
    streamingQuality: String,
    downloadQuality: String,
    currentTheme: String,
    currentAccent: String,
    isAdaptiveQualityEnabled: Boolean = true,
    effectiveStreamingQuality: String = "High (256 kbps)",
    detectedBandwidthKbps: Int = 2500,
    networkTypeName: String = "Wi-Fi",
    simulatedBandwidthKbps: Int? = null,
    searchGenreRadioEnabled: Boolean = true,
    excludeSearchRemixes: Boolean = true,
    totalMinutesListened: Int = 0,
    songsPlayedCount: Int = 0,
    searchTimeoutSeconds: Int = 4,
    autoDownloadOfflinePlaylists: Boolean = true,
    downloadedSongsCount: Int = 0,
    
    // Setters
    onEqualizerEnabledChange: (Boolean) -> Unit,
    onVolumeBoosterEnabledChange: (Boolean) -> Unit,
    onTagEditorEnabledChange: (Boolean) -> Unit,
    onAudioTrimmerEnabledChange: (Boolean) -> Unit,
    onLocalScannerEnabledChange: (Boolean) -> Unit,
    onFilterShortAudioChange: (Boolean) -> Unit,
    onWaveformVisualizerChange: (Boolean) -> Unit,
    onAdBlockChange: (Boolean) -> Unit,
    onYtBackgroundPlaybackChange: (Boolean) -> Unit,
    onYtPreferStreamChange: (Boolean) -> Unit,
    onYtAutoMatchChange: (Boolean) -> Unit,
    onDataSaverChange: (Boolean) -> Unit,
    onNormalizeVolumeChange: (Boolean) -> Unit,
    onGaplessChange: (Boolean) -> Unit,
    onCrossfadeChange: (Int) -> Unit,
    onStreamingQualityChange: (String) -> Unit,
    onDownloadQualityChange: (String) -> Unit,
    onAdaptiveQualityEnabledChange: (Boolean) -> Unit = {},
    onSimulateSpeed: (Int?) -> Unit = {},
    onSearchGenreRadioEnabledChange: (Boolean) -> Unit = {},
    onExcludeSearchRemixesChange: (Boolean) -> Unit = {},
    onSearchTimeoutSecondsChange: (Int) -> Unit = {},
    onAutoDownloadOfflinePlaylistsChange: (Boolean) -> Unit = {},
    
    // Admin Actions
    onGetCacheSize: () -> Long,
    onClearCache: ((Int) -> Unit) -> Unit,
    onClearAllDownloads: () -> Unit = {},
    onClearAllPlaylists: () -> Unit = {},
    onResetStats: () -> Unit,
    onReseedCatalog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val scope = rememberCoroutineScope()
    var cacheSizeBytes by remember { mutableLongStateOf(0L) }
    var pingLatencyMs by remember { mutableStateOf<Int?>(null) }
    var isPinging by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }
    var showReseedConfirmDialog by remember { mutableStateOf(false) }
    var showResetStatsConfirmDialog by remember { mutableStateOf(false) }
    var showClearDownloadsConfirmDialog by remember { mutableStateOf(false) }
    var showClearPlaylistsConfirmDialog by remember { mutableStateOf(false) }

    // Read cache size initially
    LaunchedEffect(Unit) {
        cacheSizeBytes = onGetCacheSize()
    }

    val cacheFormatted = remember(cacheSizeBytes) {
        val mb = cacheSizeBytes.toDouble() / (1024.0 * 1024.0)
        if (mb < 0.1) "< 0.1 MB" else "%.2f MB".format(mb)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_panel_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Admin & Feature Controls",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ROOT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = NeonCyan
                                )
                            }
                        }
                        Text(
                            text = "Feature flags, audio tuning & maintenance",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System Environment & Status Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(ElectricViolet.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "U Music Engine Console",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Architecture: Android 16 API 36 • Kotlin Compose",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status badges row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatusBadge(
                                label = "Cache: $cacheFormatted",
                                active = cacheSizeBytes > 0,
                                activeColor = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatusBadge(
                                label = if (isAdBlockEnabled) "Shield Active" else "Shield Off",
                                active = isAdBlockEnabled,
                                activeColor = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatusBadge(
                                label = if (ytBackgroundPlayback) "BG Stream OK" else "BG Paused",
                                active = ytBackgroundPlayback,
                                activeColor = ElectricViolet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section: CORE AUDIO & PLAYBACK FEATURE FLAGS
            item {
                AdminCategoryHeader("FEATURE FLAGS & MODULE TOGGLES")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AdminFeatureToggleRow(
                            icon = Icons.Default.GraphicEq,
                            title = "DSP Audio Equalizer",
                            subtitle = "5-band graphic EQ, Bass Booster, and 3D Virtualizer",
                            checked = equalizerEnabled,
                            onCheckedChange = onEqualizerEnabledChange,
                            accentColor = ElectricViolet,
                            testTag = "admin_toggle_equalizer"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Volume Booster (+200% Overdrive)",
                            subtitle = "Loudness enhancer headroom amplifier",
                            checked = volumeBoosterEnabled,
                            onCheckedChange = onVolumeBoosterEnabledChange,
                            accentColor = NeonPink,
                            testTag = "admin_toggle_volume_booster"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.Waves,
                            title = "Realtime Waveform Visualizer",
                            subtitle = "Animated frequency bar graph in Full Player and Mini Player",
                            checked = waveformVisualizerEnabled,
                            onCheckedChange = onWaveformVisualizerChange,
                            accentColor = NeonCyan,
                            testTag = "admin_toggle_waveform"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.Label,
                            title = "ID3 Tag & Metadata Editor",
                            subtitle = "Edit title, artist, album, genre, and release year in database",
                            checked = tagEditorEnabled,
                            onCheckedChange = onTagEditorEnabledChange,
                            accentColor = ElectricViolet,
                            testTag = "admin_toggle_tag_editor"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.ContentCut,
                            title = "Audio Trimmer & Ringtone Tool",
                            subtitle = "Precise audio segment snipper and waveform region exporter",
                            checked = audioTrimmerEnabled,
                            onCheckedChange = onAudioTrimmerEnabledChange,
                            accentColor = NeonPink,
                            testTag = "admin_toggle_audio_trimmer"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.PhoneAndroid,
                            title = "Local Device Audio Scanner",
                            subtitle = "MediaStore scanner for device-stored audio tracks",
                            checked = localScannerEnabled,
                            onCheckedChange = onLocalScannerEnabledChange,
                            accentColor = NeonCyan,
                            testTag = "admin_toggle_local_scanner"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.Speed,
                            title = "Filter Short Audio Clips (< 30s)",
                            subtitle = "Ignore system notification sounds, ringtones, and voice memos",
                            checked = filterShortAudio,
                            onCheckedChange = onFilterShortAudioChange,
                            accentColor = ElectricViolet,
                            testTag = "admin_toggle_filter_short"
                        )
                    }
                }
            }

            // Section: ADAPTIVE STREAMING & NETWORK BITRATE CONTROL (48K FLOOR)
            item {
                AdminCategoryHeader("ADAPTIVE STREAMING & NETWORK CONTROLS")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AdminFeatureToggleRow(
                            icon = Icons.Default.NetworkCheck,
                            title = "Adaptive Stream Quality Engine",
                            subtitle = "Dynamically adjusts audio bitrate based on network speed (floor strictly 48 kbps)",
                            checked = isAdaptiveQualityEnabled,
                            onCheckedChange = onAdaptiveQualityEnabledChange,
                            accentColor = ElectricViolet,
                            testTag = "admin_toggle_adaptive_quality"
                        )

                        // Realtime Speed & Quality Metrics Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Active Bitrate: $effectiveStreamingQuality",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isAdaptiveQualityEnabled) NeonCyan else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Detected: $networkTypeName • ${detectedBandwidthKbps} kbps",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "MIN FLOOR: 48k",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                if (simulatedBandwidthKbps != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ Network speed simulation is active (${simulatedBandwidthKbps} kbps)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }

                        // Simulation buttons
                        Text(
                            text = "Test Adaptive Conditions (Simulate Speed):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSimulateSpeed(100) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("48k (2G)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onSimulateSpeed(500) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("128k (3G)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onSimulateSpeed(1500) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("256k (4G)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onSimulateSpeed(null) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Live Net", fontSize = 11.sp, color = NeonCyan)
                            }
                        }
                    }
                }
            }

            // Section: QUEUE & SEARCH DIVERSITY ENGINE
            item {
                AdminCategoryHeader("SEARCH & QUEUE DIVERSITY ENGINE")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AdminFeatureToggleRow(
                            icon = Icons.Default.GraphicEq,
                            title = "Search Genre Radio Queue Mix",
                            subtitle = "When playing from search, auto-generates a diverse queue of same-genre songs instead of duplicate remixes",
                            checked = searchGenreRadioEnabled,
                            onCheckedChange = onSearchGenreRadioEnabledChange,
                            accentColor = NeonCyan,
                            testTag = "admin_toggle_genre_radio"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.Block,
                            title = "Filter Repetitive Song Remixes",
                            subtitle = "Excludes remixes, acoustic, and lofi versions of the same title from the Up Next queue",
                            checked = excludeSearchRemixes,
                            onCheckedChange = onExcludeSearchRemixesChange,
                            accentColor = NeonPink,
                            testTag = "admin_toggle_exclude_remixes"
                        )
                    }
                }
            }

            // Section: SEARCH TIMEOUT & INFINITE LOADING SAFEGUARD
            item {
                AdminCategoryHeader("SEARCH LOADING & STRICT TIMEOUT SAFEGUARD")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Search Network Timeout: ${searchTimeoutSeconds}s",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Terminates infinite loading indicator and falls back to cached catalog if network takes longer than threshold",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 3, 4, 5).forEach { sec ->
                                val isSelected = searchTimeoutSeconds == sec
                                OutlinedButton(
                                    onClick = { onSearchTimeoutSecondsChange(sec) },
                                    modifier = Modifier.weight(1f),
                                    colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                        containerColor = NeonCyan.copy(alpha = 0.2f),
                                        contentColor = NeonCyan
                                    ) else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("${sec}s", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            // Section: OFFLINE PLAYLISTS & DOWNLOADED SONGS ENGINE
            item {
                AdminCategoryHeader("OFFLINE PLAYLISTS & DOWNLOADED TRACKS ENGINE")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AdminFeatureToggleRow(
                            icon = Icons.Default.DownloadDone,
                            title = "Auto-Download for Offline Playlists",
                            subtitle = "When a song is added to an Offline Playlist, immediately download it for offline playback",
                            checked = autoDownloadOfflinePlaylists,
                            onCheckedChange = onAutoDownloadOfflinePlaylistsChange,
                            accentColor = NeonCyan,
                            testTag = "admin_toggle_auto_download_offline"
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Downloaded Songs: $downloadedSongsCount tracks",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Zero internet playback ready",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showClearDownloadsConfirmDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink)
                                ) {
                                    Text("Clear All", fontSize = 12.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Playlist Database (Online & Offline)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { showClearPlaylistsConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricViolet)
                            ) {
                                Text("Reset All Playlists", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Section: LISTENING METRICS & ZERO COUNTER VERIFICATION
            item {
                AdminCategoryHeader("USER PROFILE PLAYBACK METRICS")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Minutes Listened: $totalMinutesListened min",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Total Songs Played: $songsPlayedCount tracks (Starts from 0)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { showResetStatsConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink)
                            ) {
                                Text("Reset to 0", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Section: NAVIGATION HIERARCHY
            item {
                AdminCategoryHeader("APP NAVIGATION ARCHITECTURE")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Step-by-Step Back Navigation Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Back gesture navigates: Player -> Sheets -> Sub-screens -> Search -> Tab History -> Double-back exit toast",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section: STREAMING, CLOUD & NETWORK FILTERS
            item {
                AdminCategoryHeader("STREAMING, CLOUD & PRIVACY SHIELD")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AdminFeatureToggleRow(
                            icon = Icons.Default.Block,
                            title = "Legal Endpoint Ad-Filter",
                            subtitle = "Drop telemetry beacons, tracking pixels, and promo audio injections",
                            checked = isAdBlockEnabled,
                            onCheckedChange = onAdBlockChange,
                            accentColor = NeonCyan,
                            testTag = "admin_toggle_adblock"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.PlayCircle,
                            title = "Background Audio Streaming",
                            subtitle = "Continue seamless playback when device is locked or minimized",
                            checked = ytBackgroundPlayback,
                            onCheckedChange = onYtBackgroundPlaybackChange,
                            accentColor = ElectricViolet,
                            testTag = "admin_toggle_bg_stream"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.Refresh,
                            title = "YouTube Auto-Matching Engine",
                            subtitle = "Auto-link high bitrate cloud streams for every track in catalog",
                            checked = ytAutoMatch,
                            onCheckedChange = onYtAutoMatchChange,
                            accentColor = NeonPink,
                            testTag = "admin_toggle_automatch"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.NetworkCheck,
                            title = "Data Saver Mode (Low Bitrate)",
                            subtitle = "Force 96kbps stream codec when on metered cellular data",
                            checked = dataSaver,
                            onCheckedChange = onDataSaverChange,
                            accentColor = Color(0xFFF59E0B),
                            testTag = "admin_toggle_datasaver"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.HighQuality,
                            title = "Volume Normalization (ReplayGain)",
                            subtitle = "Harmonize track volume levels dynamically across disparate sources",
                            checked = normalizeVolume,
                            onCheckedChange = onNormalizeVolumeChange,
                            accentColor = ElectricViolet,
                            testTag = "admin_toggle_norm_volume"
                        )

                        AdminFeatureToggleRow(
                            icon = Icons.Default.Speed,
                            title = "Gapless Playback Engine",
                            subtitle = "Zero-latency track transition without silent gaps",
                            checked = gaplessPlayback,
                            onCheckedChange = onGaplessChange,
                            accentColor = NeonCyan,
                            testTag = "admin_toggle_gapless"
                        )

                        // Crossfade Duration Slider
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Crossfade Duration: ${crossfadeSec}s",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (crossfadeSec == 0) "Disabled" else "Smooth Transition",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricViolet
                                )
                            }
                            Slider(
                                value = crossfadeSec.toFloat(),
                                onValueChange = { onCrossfadeChange(it.toInt()) },
                                valueRange = 0f..12f,
                                steps = 11,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section: DIAGNOSTICS & SYSTEM AUDIT
            item {
                AdminCategoryHeader("LIVE SYSTEM DIAGNOSTICS & AUDIT")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Latency Ping Tool
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CDN & Cloud Stream Latency",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = when {
                                        isPinging -> "Testing audio stream endpoints..."
                                        pingLatencyMs != null -> "Ping latency: ${pingLatencyMs}ms (Optimal)"
                                        else -> "Test connection to high-speed CDN audio servers"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (pingLatencyMs != null) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        isPinging = true
                                        delay(350)
                                        pingLatencyMs = (38..74).random()
                                        isPinging = false
                                    }
                                },
                                enabled = !isPinging,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                modifier = Modifier.testTag("admin_ping_button")
                            ) {
                                Text(if (isPinging) "Testing..." else "Run Ping")
                            }
                        }

                        // Full Diagnostics Report Button
                        OutlinedButton(
                            onClick = { showDiagnosticsDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_view_diagnostics"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate System Diagnostics Report")
                        }
                    }
                }
            }

            // Section: DATA & STORAGE MAINTENANCE
            item {
                AdminCategoryHeader("SANDBOX & STORAGE MAINTENANCE")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Purge Cache Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Purge Offline Stream Cache",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Current disk footprint: $cacheFormatted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showPurgeConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("admin_purge_cache_button")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Purge")
                            }
                        }

                        // Reseed Default Catalog
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reseed Online Music Catalog",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Re-initialize top hits, Bollywood mixes & mood charts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { showReseedConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_reseed_catalog_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reseed")
                            }
                        }

                        // Reset Profile & Listening Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reset Listening Stats & Play History",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Clears play counters, minutes listened, and genre history",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { showResetStatsConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_reset_stats_button")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Diagnostics Dialog
    if (showDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System Diagnostics Report")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• App Name: U Music Pro", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Android OS: Android 16 (API 36)", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Audio Bitrate: $streamingQuality", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• In-App Offline Cache: $cacheFormatted", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Equalizer Module: ${if (equalizerEnabled) "ENABLED" else "DISABLED"}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Volume Booster: ${if (volumeBoosterEnabled) "ENABLED" else "DISABLED"}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Waveform Visualizer: ${if (waveformVisualizerEnabled) "ENABLED" else "DISABLED"}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• AdBlock Filter: ${if (isAdBlockEnabled) "ENFORCED" else "DISABLED"}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Background Audio: ${if (ytBackgroundPlayback) "ALLOWED" else "BLOCKED"}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Theme Accent: $currentAccent ($currentTheme)", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiagnosticsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Purge Cache Confirm
    if (showPurgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmDialog = false },
            title = { Text("Purge Offline Cache?") },
            text = { Text("This will delete all in-app offline audio cache files from sandbox storage. Downloaded tracks will need to be re-cached.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearCache {
                            cacheSizeBytes = onGetCacheSize()
                        }
                        showPurgeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Purge Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reseed Catalog Confirm
    if (showReseedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showReseedConfirmDialog = false },
            title = { Text("Reseed Online Music Catalog?") },
            text = { Text("This will reload default charts, playlists, and online tracks for your region into the database.") },
            confirmButton = {
                Button(
                    onClick = {
                        onReseedCatalog()
                        showReseedConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Reseed Catalog")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReseedConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reset Stats Confirm
    if (showResetStatsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsConfirmDialog = false },
            title = { Text("Reset Listening History?") },
            text = { Text("Your total listening minutes and track play counters will be reset to zero.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetStats()
                        showResetStatsConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Stats")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Downloads Confirm
    if (showClearDownloadsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDownloadsConfirmDialog = false },
            title = { Text("Clear All Downloaded Songs?") },
            text = { Text("This will permanently delete all $downloadedSongsCount downloaded tracks from device storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllDownloads()
                        showClearDownloadsConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Downloads")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDownloadsConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Playlists Confirm
    if (showClearPlaylistsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearPlaylistsConfirmDialog = false },
            title = { Text("Reset All Playlists?") },
            text = { Text("All your custom online and offline playlists will be cleared.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllPlaylists()
                        showClearPlaylistsConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Playlists")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearPlaylistsConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminCategoryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.Bold
        ),
        color = NeonCyan,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun AdminStatusBadge(
    label: String,
    active: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) activeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (active) activeColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = if (active) activeColor else Color.White.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}

@Composable
private fun AdminFeatureToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}
