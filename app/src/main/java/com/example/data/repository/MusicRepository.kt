package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()

    val allSavedSongs: Flow<List<Song>> = songDao.getAllSongs()
    val likedSongs: Flow<List<Song>> = songDao.getLikedSongs()
    val downloadedSongs: Flow<List<Song>> = songDao.getDownloadedSongs()
    val localDeviceSongs: Flow<List<Song>> = songDao.getLocalSongs()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    // Dynamic Online Trending Catalog StateFlow
    private val _onlineCatalogFlow = MutableStateFlow<List<Song>>(
        (AudioStreamExtractor.getIndianTrendingHits() + AudioStreamExtractor.getGlobalTrendingHits()).distinctBy { it.id }
    )
    val onlineCatalogFlow: StateFlow<List<Song>> = _onlineCatalogFlow.asStateFlow()

    suspend fun initializeCatalog(country: String = "India", age: Int = 22) = withContext(Dispatchers.IO) {
        // Fetch diverse initial songs from regional & global hit lists
        val regionalSongs = if (country.equals("India", ignoreCase = true)) {
            AudioStreamExtractor.getIndianTrendingHits() + AudioStreamExtractor.getGlobalTrendingHits()
        } else {
            AudioStreamExtractor.getGlobalTrendingHits() + AudioStreamExtractor.getIndianTrendingHits()
        }

        val uniqueSongs = regionalSongs.distinctBy { it.id }
        _onlineCatalogFlow.value = uniqueSongs

        // Insert into Room
        uniqueSongs.forEach { song ->
            songDao.insertSong(song)
        }

        // No pre-made playlists seeded - new users start with empty playlists
    }

    suspend fun refreshCatalogForRegion(country: String, age: Int) = withContext(Dispatchers.IO) {
        val songs = AudioStreamExtractor.fetchTrendingForCountry(country, age)
        if (songs.isNotEmpty()) {
            val combined = (songs + _onlineCatalogFlow.value).distinctBy { it.id }
            _onlineCatalogFlow.value = combined
            songs.forEach { songDao.insertSong(it) }
        }
    }

    fun getOnlineCatalog(): List<Song> {
        return _onlineCatalogFlow.value
    }

    fun searchSongs(query: String, filter: String = "All"): List<Song> {
        val q = query.trim().lowercase()
        val baseList = getOnlineCatalog()
        if (q.isEmpty()) {
            return when (filter) {
                "Songs" -> baseList
                "Cloud Streams" -> baseList.filter { it.isYoutubeConnected }
                "Bollywood" -> baseList.filter { it.genre == "Bollywood" }
                "Punjabi" -> baseList.filter { it.genre.contains("Punjabi", ignoreCase = true) }
                "Electronic" -> baseList.filter { it.genre == "Electronic" }
                "Pop" -> baseList.filter { it.genre == "Pop" }
                "Rock" -> baseList.filter { it.genre == "Rock" }
                "Hip-Hop" -> baseList.filter { it.genre == "Hip-Hop" }
                "Lo-Fi" -> baseList.filter { it.genre == "Lo-Fi" }
                else -> baseList
            }
        }
        return baseList.filter { song ->
            val matchesText = song.title.lowercase().contains(q) ||
                    song.artist.lowercase().contains(q) ||
                    song.album.lowercase().contains(q) ||
                    song.genre.lowercase().contains(q) ||
                    song.youtubeChannel.lowercase().contains(q)
            
            val matchesFilter = when (filter) {
                "All" -> true
                "Songs" -> true
                "Cloud Streams" -> song.isYoutubeConnected
                "Artists" -> song.artist.lowercase().contains(q)
                "Albums" -> song.album.lowercase().contains(q)
                else -> song.genre.equals(filter, ignoreCase = true)
            }
            matchesText && matchesFilter
        }
    }

    suspend fun searchOnlineDirect(query: String): List<Song> {
        val localMatches = searchSongs(query)
        val onlineStreams = AudioStreamExtractor.searchOnlineStreams(query)
        return (onlineStreams + localMatches).distinctBy { it.title.lowercase() + it.artist.lowercase() }
    }

    fun matchSongWithYoutube(song: Song): Song {
        if (song.isYoutubeConnected && song.youtubeId.isNotEmpty()) return song
        
        val matchedInCatalog = getOnlineCatalog().firstOrNull {
            it.title.equals(song.title, ignoreCase = true) || it.artist.equals(song.artist, ignoreCase = true)
        }

        val ytId = matchedInCatalog?.youtubeId ?: "dQw4w9WgXcQ"
        val channel = if (song.artist.isNotEmpty()) "${song.artist} Topic" else "Official Artist Audio"
        val ytViews = matchedInCatalog?.youtubeViews ?: "24M streams"

        return song.copy(
            youtubeId = ytId,
            youtubeMusicUrl = "https://music.youtube.com/watch?v=$ytId",
            youtubeViews = ytViews,
            youtubeChannel = channel,
            isYoutubeConnected = true,
            youtubeAudioBitrate = "320 kbps Opus"
        )
    }

    suspend fun toggleLike(song: Song) = withContext(Dispatchers.IO) {
        val newLiked = !song.isLiked
        songDao.setLiked(song.id, newLiked)
    }

    suspend fun downloadSong(song: Song, quality: String = "High (320kbps)") = withContext(Dispatchers.IO) {
        try {
            val offlineCacheDir = File(context.cacheDir, "offline_stream_cache")
            if (!offlineCacheDir.exists()) offlineCacheDir.mkdirs()
            val targetFile = File(offlineCacheDir, "${song.id}.cache")

            if (song.audioUrl.startsWith("http")) {
                try {
                    if (AdBlockEndpointFilter.isAdEndpoint(song.audioUrl)) {
                        return@withContext
                    }
                    val url = URL(song.audioUrl)
                    val connection = url.openConnection()
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    val input = connection.getInputStream()
                    val output = FileOutputStream(targetFile)
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                    output.close()
                    input.close()

                    songDao.setDownloaded(song.id, true, targetFile.absolutePath)
                } catch (e: Exception) {
                    songDao.setDownloaded(song.id, true, song.audioUrl)
                }
            } else {
                songDao.setDownloaded(song.id, true, song.localFilePath.ifEmpty { song.audioUrl })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeDownload(songId: String) = withContext(Dispatchers.IO) {
        val song = songDao.getSongById(songId)
        if (song != null && song.localFilePath.isNotEmpty()) {
            val file = File(song.localFilePath)
            if (file.exists() && file.absolutePath.contains("offline_stream_cache")) {
                file.delete()
            }
        }
        songDao.setDownloaded(songId, false, "")
    }

    suspend fun deleteDownload(song: Song) = withContext(Dispatchers.IO) {
        removeDownload(song.id)
    }

    suspend fun clearAllDownloads() = withContext(Dispatchers.IO) {
        val offlineCacheDir = File(context.cacheDir, "offline_stream_cache")
        if (offlineCacheDir.exists()) {
            offlineCacheDir.listFiles()?.forEach { it.delete() }
        }
        songDao.clearAllDownloaded()
    }

    suspend fun createPlaylist(name: String, description: String = "", isOffline: Boolean = false): String = withContext(Dispatchers.IO) {
        val id = "pl_${System.currentTimeMillis()}"
        val playlist = Playlist(
            id = id,
            title = name,
            description = description,
            coverUrl = "",
            isCustom = true,
            isOffline = isOffline,
            songCount = 0
        )
        playlistDao.insertPlaylist(playlist)
        id
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId, 0))
        playlistDao.updatePlaylistSongCount(playlistId)
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null && playlist.isOffline) {
            val song = songDao.getSongById(songId)
            if (song != null && !song.isDownloaded) {
                downloadSong(song)
            }
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        playlistDao.updatePlaylistSongCount(playlistId)
    }

    suspend fun moveSongBetweenPlaylists(fromPlaylistId: String, toPlaylistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(fromPlaylistId, songId)
        playlistDao.updatePlaylistSongCount(fromPlaylistId)

        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(toPlaylistId, songId, 0))
        playlistDao.updatePlaylistSongCount(toPlaylistId)

        val targetPlaylist = playlistDao.getPlaylistById(toPlaylistId)
        if (targetPlaylist != null && targetPlaylist.isOffline) {
            val song = songDao.getSongById(songId)
            if (song != null && !song.isDownloaded) {
                downloadSong(song)
            }
        }
    }

    fun getSongsForPlaylist(playlistId: String): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId)
    }

    suspend fun clearAllPlaylists() = withContext(Dispatchers.IO) {
        playlistDao.clearAllPlaylistSongs()
        playlistDao.clearAllPlaylists()
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun updateSongMetadata(
        songId: String,
        newTitle: String,
        newArtist: String,
        newAlbum: String,
        newGenre: String,
        newYear: Int
    ) = withContext(Dispatchers.IO) {
        songDao.updateMetadata(songId, newTitle, newArtist, newAlbum, newGenre, newYear)
    }

    suspend fun scanLocalMedia(filterShort: Boolean = true) = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
            )

            val minDuration = if (filterShort) 30000L else 5000L
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > $minDuration"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val path = cursor.getString(dataCol) ?: ""

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId).toString()
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    val localSong = Song(
                        id = "local_$mediaId",
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = duration,
                        artworkUrl = artworkUri,
                        audioUrl = contentUri,
                        localFilePath = path,
                        lyrics = "",
                        isOnline = false,
                        genre = "Local Audio",
                        year = 2024,
                        isYoutubeConnected = false
                    )
                    songDao.insertSong(localSong)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOfflineCacheSizeBytes(): Long {
        val cacheDir = File(context.cacheDir, "offline_stream_cache")
        if (!cacheDir.exists()) return 0L
        return cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    suspend fun clearAllOfflineCache(): Int = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "offline_stream_cache")
        var count = 0
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.delete()) count++
            }
        }
        songDao.clearAllDownloaded()
        count
    }

    suspend fun reseedDefaultCatalog(country: String = "India", age: Int = 22) = withContext(Dispatchers.IO) {
        initializeCatalog(country, age)
    }
}
