package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.player.PlaybackUiState
import com.example.ui.components.AnimatedWaveform
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    playlistSongs: List<Song>,
    playbackState: PlaybackUiState,
    onBack: () -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffle: (List<Song>) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onSongLike: (Song) -> Unit,
    onSongMore: (Song) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onAddTracksClick: () -> Unit = {},
    onRemoveSong: (Song) -> Unit = {},
    onMoveSong: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }

    if (showDeletePlaylistConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistConfirm = false },
            title = {
                Text("Delete Playlist?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${playlist.title}\"? Tracks in your library will not be deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeletePlaylistConfirm = false
                        onDeletePlaylist(playlist.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlaylistConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // Top Nav
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onAddTracksClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add Tracks", tint = ElectricViolet)
                        }

                        if (playlist.isCustom) {
                            IconButton(onClick = { showDeletePlaylistConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = NeonPink)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hero Artwork & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playlist.coverUrl.isNotEmpty()) {
                            AsyncImage(
                                model = playlist.coverUrl,
                                contentDescription = playlist.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            if (playlist.isOffline) listOf(NeonCyan, MaterialTheme.colorScheme.primary)
                                            else listOf(ElectricViolet, NeonCyan)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (playlist.isOffline) Icons.Default.DownloadDone else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playlist.description.ifEmpty { if (playlist.isOffline) "Offline collection" else "Curated by you" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Type Badge (Online vs Offline)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (playlist.isOffline) NeonCyan.copy(alpha = 0.2f) else ElectricViolet.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (playlist.isOffline) "OFFLINE PLAYLIST • AUTO-DOWNLOADS" else "ONLINE PLAYLIST • CLOUD STREAM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (playlist.isOffline) NeonCyan else ElectricViolet,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${playlistSongs.size} tracks",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play, Shuffle & Add Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (playlistSongs.isNotEmpty()) {
                        Button(
                            onClick = { onPlayAll(playlistSongs) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onShuffle(playlistSongs) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle", color = MaterialTheme.colorScheme.onBackground)
                        }
                    } else {
                        Button(
                            onClick = onAddTracksClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Tracks to Playlist", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Empty Playlist State
        if (playlistSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "This playlist is empty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (playlist.isOffline) {
                                "Add songs to this playlist and they will automatically download for offline playback."
                            } else {
                                "Add songs to stream them anytime from your custom playlist."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddTracksClick,
                            colors = ButtonDefaults.buttonColors(containerColor = if (playlist.isOffline) NeonCyan else ElectricViolet)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Browse & Add Songs")
                        }
                    }
                }
            }
        } else {
            // Song List with Actions (Remove & Move)
            items(playlistSongs) { song ->
                val isCurrent = song.id == playbackState.currentSong?.id
                val isPlaying = isCurrent && playbackState.isPlaying
                var showOptionsDropdown by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song, playlistSongs) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedWaveform(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPlaying) ElectricViolet else MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (song.isDownloaded) {
                                Icon(
                                    imageVector = Icons.Default.DownloadDone,
                                    contentDescription = "Saved Offline",
                                    tint = NeonCyan,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                            Text(
                                text = "${song.artist} • ${song.formattedDuration()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Like Action
                    IconButton(
                        onClick = { onSongLike(song) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Song",
                            tint = if (song.isLiked) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Playlist Dropdown Menu
                    Box {
                        IconButton(
                            onClick = { showOptionsDropdown = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsDropdown,
                            onDismissRequest = { showOptionsDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove from this Playlist") },
                                leadingIcon = {
                                    Icon(Icons.Default.PlaylistRemove, contentDescription = null, tint = NeonPink)
                                },
                                onClick = {
                                    showOptionsDropdown = false
                                    onRemoveSong(song)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Move to another Playlist") },
                                leadingIcon = {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = NeonCyan)
                                },
                                onClick = {
                                    showOptionsDropdown = false
                                    onMoveSong(song)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("More Song Details & Options") },
                                leadingIcon = {
                                    Icon(Icons.Default.MoreVert, contentDescription = null)
                                },
                                onClick = {
                                    showOptionsDropdown = false
                                    onSongMore(song)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
