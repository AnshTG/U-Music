package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY dateAdded DESC")
    fun getLikedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY dateAdded DESC")
    fun getDownloadedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isOnline = 0 ORDER BY title ASC")
    fun getLocalSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Query("UPDATE songs SET isLiked = :isLiked WHERE id = :songId")
    suspend fun setLiked(songId: String, isLiked: Boolean)

    @Query("UPDATE songs SET isDownloaded = :isDownloaded, localFilePath = :filePath WHERE id = :songId")
    suspend fun setDownloaded(songId: String, isDownloaded: Boolean, filePath: String)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String)

    @Query("UPDATE songs SET title = :title, artist = :artist, album = :album, genre = :genre, year = :year WHERE id = :id")
    suspend fun updateMetadata(id: String, title: String, artist: String, album: String, genre: String, year: Int)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: String)
}
