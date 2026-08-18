package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RepeatMode
import com.example.data.model.SleepTimerOption
import com.example.data.model.Song
import com.example.player.PlaybackUiState
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle

@Composable
fun FullScreenPlayer(
    playbackState: PlaybackUiState,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleLike: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenVolumeBooster: () -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onSelectQueueSong: (Song) -> Unit,
    onOpenInYoutubeMusic: (Song) -> Unit = {},
    onToggleYoutubeStream: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val song = playbackState.currentSong ?: return
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Player, 1: Lyrics, 2: Up Next
    var showMoreMenu by remember { mutableStateOf(false) }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableStateOf(0f) }

    val currentPos = if (isDraggingSlider) {
        (sliderDragPosition * playbackState.durationMs).toLong()
    } else {
        playbackState.currentPositionMs
    }

    val sliderValue = if (playbackState.durationMs > 0) {
        (currentPos.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_screen_player")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = song.genre,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Stream Source: ${song.youtubeAudioBitrate}") },
                            onClick = { showMoreMenu = false },
                            leadingIcon = { Icon(Icons.Default.GraphicEq, null, tint = NeonCyan) }
                        )
                        DropdownMenuItem(
                            text = { Text("Equalizer & Effects") },
                            onClick = {
                                showMoreMenu = false
                                onOpenEqualizer()
                            },
                            leadingIcon = { Icon(Icons.Default.Equalizer, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep Timer") },
                            onClick = {
                                showMoreMenu = false
                                onOpenSleepTimer()
                            },
                            leadingIcon = { Icon(Icons.Default.Timer, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Volume Booster (${(playbackState.equalizerState.volumeBoostMultiplier * 100).toInt()}%)") },
                            onClick = {
                                showMoreMenu = false
                                onOpenVolumeBooster()
                            },
                            leadingIcon = { Icon(Icons.Default.VolumeUp, null) }
                        )
                    }
                }
            }

            // Tabs for Player / Lyrics / Queue
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = ElectricViolet,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElectricViolet
                    )
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Song", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Lyrics", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Up Next (${playbackState.queue.size})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Tab Content
            AnimatedContent(
                targetState = selectedTab,
                label = "player_tab_content",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    0 -> PlayerMainContent(
                        song = song,
                        playbackState = playbackState,
                        sliderValue = sliderValue,
                        currentPos = currentPos,
                        onSliderChange = {
                            isDraggingSlider = true
                            sliderDragPosition = it
                        },
                        onSliderChangeFinished = {
                            isDraggingSlider = false
                            onSeekTo((sliderDragPosition * playbackState.durationMs).toLong())
                        },
                        onToggleLike = { onToggleLike(song) },
                        onDownload = { onDownload(song) },
                        onToggleShuffle = onToggleShuffle,
                        onSkipPrevious = onSkipPrevious,
                        onPlayPause = onPlayPause,
                        onSkipNext = onSkipNext,
                        onCycleRepeat = onCycleRepeat,
                        onOpenEqualizer = onOpenEqualizer,
                        onOpenSleepTimer = onOpenSleepTimer,
                        onOpenVolumeBooster = onOpenVolumeBooster,
                        onOpenInYoutubeMusic = { onOpenInYoutubeMusic(song) },
                        onToggleYoutubeStream = onToggleYoutubeStream
                    )
                    1 -> LyricsView(
                        song = song,
                        currentPositionMs = playbackState.currentPositionMs,
                        onSeekTo = onSeekTo
                    )
                    2 -> QueueView(
                        playbackState = playbackState,
                        onSongClick = onSelectQueueSong,
                        onRemoveSong = onRemoveFromQueue
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerMainContent(
    song: Song,
    playbackState: PlaybackUiState,
    sliderValue: Float,
    currentPos: Long,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onToggleLike: () -> Unit,
    onDownload: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSkipPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenVolumeBooster: () -> Unit,
    onOpenInYoutubeMusic: () -> Unit = {},
    onToggleYoutubeStream: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Large Album Art with Glow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ElectricViolet,
                    ambientColor = NeonCyan
                )
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (song.artworkUrl.isNotEmpty()) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(ElectricViolet, NeonCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        // Title, Artist, and Action Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onToggleLike) {
                    Icon(
                        imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (song.isLiked) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = if (song.isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                        contentDescription = if (song.isDownloaded) "Cached for Offline" else "Make Available Offline (In-App Cache)",
                        tint = if (song.isDownloaded) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Progress Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = sliderValue,
                onValueChange = onSliderChange,
                onValueChangeFinished = onSliderChangeFinished,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricVioletLight,
                    activeTrackColor = ElectricViolet,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(currentPos),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatMs(playbackState.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Main Controls: Shuffle, Prev, Play/Pause, Next, Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playbackState.isShuffle) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Big Play/Pause Floating Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricViolet, ElectricVioletLight)
                        )
                    )
                    .clickable { onPlayPause() }
                    .testTag("full_player_play_pause"),
                contentAlignment = Alignment.Center
            ) {
                if (playbackState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(34.dp)
                )
            }

            IconButton(onClick = onCycleRepeat) {
                Icon(
                    imageVector = when (playbackState.repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (playbackState.repeatMode != RepeatMode.OFF) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick Audio Tools Strip: EQ, Sleep Timer, Volume Boost
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Equalizer
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenEqualizer() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Equalizer",
                    tint = if (playbackState.equalizerState.isEnabled) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = playbackState.equalizerState.currentPreset,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Sleep Timer
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenSleepTimer() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Sleep Timer",
                    tint = if (playbackState.sleepTimerOption != SleepTimerOption.OFF) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (playbackState.sleepTimerRemainingSeconds > 0) {
                        "${playbackState.sleepTimerRemainingSeconds / 60}m"
                    } else if (playbackState.sleepTimerOption == SleepTimerOption.END_OF_TRACK) "Track" else "Timer",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Volume Boost
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenVolumeBooster() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val boostPercent = (playbackState.equalizerState.volumeBoostMultiplier * 100).toInt()
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume Boost",
                    tint = if (boostPercent > 100) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$boostPercent%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
