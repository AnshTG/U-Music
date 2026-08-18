package com.example.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "Single",
    val durationMs: Long = 0L,
    val artworkUrl: String = "",
    val audioUrl: String = "",
    val lyrics: String = "",
    val isOnline: Boolean = true,
    val isDownloaded: Boolean = false,
    val localFilePath: String = "",
    val genre: String = "Pop",
    val year: Int = 2024,
    val isLiked: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val downloadQuality: String = "High (320kbps)",
    val youtubeId: String = "",
    val youtubeMusicUrl: String = "",
    val youtubeViews: String = "10M views",
    val youtubeChannel: String = "",
    val isYoutubeConnected: Boolean = true,
    val youtubeAudioBitrate: String = "320 kbps Opus"
) {
    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun getResolvedYoutubeMusicUrl(): String {
        return when {
            youtubeMusicUrl.isNotEmpty() -> youtubeMusicUrl
            youtubeId.isNotEmpty() -> "https://music.youtube.com/watch?v=$youtubeId"
            else -> "https://music.youtube.com/search?q=${Uri.encode("$title $artist")}"
        }
    }
}

data class LyricsLine(
    val timeMs: Long,
    val text: String
)

object LyricsParser {
    fun parseLrc(lrcContent: String): List<LyricsLine> {
        val lines = mutableListOf<LyricsLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.?(\d{2,3})?\](.*)""")
        
        lrcContent.lines().forEach { line ->
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msStr = match.groupValues[3]
                val ms = if (msStr.isNotEmpty()) {
                    if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                } else 0L
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty()) {
                    lines.add(LyricsLine(totalMs, text))
                }
            } else if (line.isNotBlank() && !line.startsWith("[")) {
                // Fallback static line estimation
                lines.add(LyricsLine(lines.size * 4000L, line.trim()))
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}
