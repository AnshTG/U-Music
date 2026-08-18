package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val coverUrl: String = "",
    val isCustom: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val orderIndex: Int = 0
)
