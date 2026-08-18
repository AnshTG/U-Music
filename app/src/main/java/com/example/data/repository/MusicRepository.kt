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

    // Dynamic Online Trending Catalog
    private var dynamicOnlineCatalog = mutableListOf<Song>()

    suspend fun initializeCatalog(country: String = "India", age: Int = 22) = withContext(Dispatchers.IO) {
        // Fetch real regional suggestions based on Country
        val regionalSongs = if (country.equals("India", ignoreCase = true)) {
            AudioStreamExtractor.getIndianTrendingHits()
        } else {
            AudioStreamExtractor.getGlobalTrendingHits()
        }

        dynamicOnlineCatalog.clear()
        dynamicOnlineCatalog.addAll(regionalSongs)

        // Insert into Room
        dynamicOnlineCatalog.forEach { song ->
            songDao.insertSong(song)
        }

        // Insert default playlists
        val defaultPlaylists = listOf(
            Playlist(
                id = "pl_trending",
                title = if (country.equals("India", ignoreCase = true)) "India Top 50 Chart" else "Today's Top Hits",
                description = "Top trending tracks streaming directly in background without interruption.",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                isCustom = false,
                songCount = dynamicOnlineCatalog.size
            ),
            Playlist(
                id = "pl_chill",
                title = "Late Night Chill & Lo-Fi",
                description = "Mellow beats and soothing melodies for unwinding.",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                isCustom = false,
                songCount = 4
            ),
            Playlist(
                id = "pl_workout",
                title = "High Energy Cardio",
                description = "Pump up the volume with electrifying beats.",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                isCustom = false,
                songCount = 5
            )
        )

        defaultPlaylists.forEach { playlist ->
            playlistDao.insertPlaylist(playlist)
        }

        dynamicOnlineCatalog.forEachIndexed { index, song ->
            playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_trending", song.id, index))
        }
    }

    suspend fun refreshCatalogForRegion(country: String, age: Int) = withContext(Dispatchers.IO) {
        val songs = AudioStreamExtractor.fetchTrendingForCountry(country, age)
        if (songs.isNotEmpty()) {
            dynamicOnlineCatalog.clear()
            dynamicOnlineCatalog.addAll(songs)
            songs.forEach { songDao.insertSong(it) }
        }
    }

    fun getOnlineCatalog(): List<Song> {
        return if (dynamicOnlineCatalog.isNotEmpty()) dynamicOnlineCatalog else AudioStreamExtractor.getIndianTrendingHits()
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

    suspend fun createPlaylist(name: String, description: String = ""): String = withContext(Dispatchers.IO) {
        val id = "custom_pl_${System.currentTimeMillis()}"
        val playlist = Playlist(
            id = id,
            title = name,
            description = description,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            isCustom = true,
            songCount = 0
        )
        playlistDao.insertPlaylist(playlist)
        id
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId, 0))
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
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

    suspend fun scanLocalMedia() = withContext(Dispatchers.IO) {
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

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"
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
}
