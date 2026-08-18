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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.player.PlaybackUiState
import com.example.ui.components.PlaylistCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SongItemRow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

data class GenreTile(val name: String, val gradient: List<Color>)

@Composable
fun ExploreScreen(
    playbackState: PlaybackUiState,
    allSongs: List<Song>,
    playlists: List<Playlist>,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onGenreClick: (String) -> Unit,
    onSongLike: (Song) -> Unit,
    onSongMore: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val genres = listOf(
        GenreTile("Electronic & Synth", listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))),
        GenreTile("Pop & Hits", listOf(Color(0xFFEC4899), Color(0xFFF43F5E))),
        GenreTile("Hip-Hop & Rap", listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
        GenreTile("Lo-Fi & Chill", listOf(Color(0xFF06B6D4), Color(0xFF0EA5E9))),
        GenreTile("Rock & Metal", listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
        GenreTile("R&B & Soul", listOf(Color(0xFF10B981), Color(0xFF059669))),
        GenreTile("Ambient & Sleep", listOf(Color(0xFF64748B), Color(0xFF334155))),
        GenreTile("Acoustic & Folk", listOf(Color(0xFF84CC16), Color(0xFF65A30D)))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Explore & Discover",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Browse new releases, trending charts and genres",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Top Charts Carousel
        item {
            SectionHeader(title = "Top Charts & Trending", subtitle = "Global streaming popularity")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                }
            }
        }

        // Moods & Genres Grid
        item {
            SectionHeader(title = "Moods & Genres", subtitle = "Explore music tailored to your vibe")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                genres.chunked(2).forEach { rowGenres ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowGenres.forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(genre.gradient))
                                    .clickable { onGenreClick(genre.name) }
                                    .padding(14.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fresh Releases
        item {
            SectionHeader(title = "New Releases", subtitle = "Newly added high-fidelity tracks")
        }

        items(allSongs.take(8)) { song ->
            val isCurrent = song.id == playbackState.currentSong?.id
            SongItemRow(
                song = song,
                isPlaying = isCurrent && playbackState.isPlaying,
                onSongClick = { onSongClick(song, allSongs) },
                onLikeClick = { onSongLike(song) },
                onMoreClick = { onSongMore(song) }
            )
        }
    }
}
