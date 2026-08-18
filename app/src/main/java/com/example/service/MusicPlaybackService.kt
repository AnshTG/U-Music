package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.UMusicApplication
import com.example.data.model.Song
import com.example.player.MusicPlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicPlaybackService : Service(), AudioManager.OnAudioFocusChangeListener {
    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastArtworkUrl: String? = null
    private var cachedArtworkBitmap: Bitmap? = null

    var playerController: MusicPlayerController? = null
        set(value) {
            field = value
            value?.onPlaybackStateChanged = { song, isPlaying ->
                updateNotification(song, isPlaying)
                if (isPlaying) {
                    acquireWakeLock()
                    requestAudioFocus()
                } else {
                    releaseWakeLock()
                }
            }
        }

    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UMusic:BackgroundAudioWakeLock")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> playerController?.togglePlayPause()
            ACTION_PAUSE -> playerController?.togglePlayPause()
            ACTION_NEXT -> playerController?.skipNext()
            ACTION_PREV -> playerController?.skipPrevious()
            ACTION_STOP -> {
                playerController?.let {
                    if (it.uiState.value.isPlaying) {
                        it.togglePlayPause()
                    }
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    fun updateNotification(song: Song?, isPlaying: Boolean) {
        val title = song?.title ?: "U Music Stream"
        val artist = song?.artist ?: "Playing in Background"
        val isYt = song?.isYoutubeConnected == true

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseAction = PendingIntent.getService(
            this,
            1,
            Intent(this, MusicPlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextAction = PendingIntent.getService(
            this,
            2,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevAction = PendingIntent.getService(
            this,
            3,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopAction = PendingIntent.getService(
            this,
            4,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val subText = if (isYt) "▶ YouTube HD" else "U Music Lossless"

        // Build notification with cached or placeholder bitmap
        val currentBitmap = cachedArtworkBitmap ?: createFallbackBitmap(title)
        
        val notification = NotificationCompat.Builder(this, UMusicApplication.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(subText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(currentBitmap)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setContentIntent(openAppIntent)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevAction)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                playPauseAction
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextAction)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Asynchronously load the high-res album art bitmap if changed
        val artworkUrl = song?.artworkUrl
        if (!artworkUrl.isNullOrBlank() && artworkUrl != lastArtworkUrl) {
            lastArtworkUrl = artworkUrl
            serviceScope.launch {
                val loadedBitmap = loadArtworkBitmap(artworkUrl)
                if (loadedBitmap != null) {
                    cachedArtworkBitmap = loadedBitmap
                    // Refresh notification with high-res album cover
                    val updatedNotification = NotificationCompat.Builder(this@MusicPlaybackService, UMusicApplication.CHANNEL_ID)
                        .setContentTitle(title)
                        .setContentText(artist)
                        .setSubText(subText)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setLargeIcon(loadedBitmap)
                        .setStyle(
                            androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                        )
                        .setContentIntent(openAppIntent)
                        .setOngoing(isPlaying)
                        .setShowWhen(false)
                        .addAction(android.R.drawable.ic_media_previous, "Previous", prevAction)
                        .addAction(
                            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                            if (isPlaying) "Pause" else "Play",
                            playPauseAction
                        )
                        .addAction(android.R.drawable.ic_media_next, "Next", nextAction)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopAction)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build()

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private suspend fun loadArtworkBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(this@MusicPlaybackService)
                .data(url)
                .size(512, 512)
                .allowHardware(false)
                .build()
            val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
            if (result is android.graphics.drawable.BitmapDrawable) {
                result.bitmap
            } else if (result != null) {
                val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                result.setBounds(0, 0, canvas.width, canvas.height)
                result.draw(canvas)
                bmp
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun createFallbackBitmap(title: String): Bitmap {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#8B5CF6"))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            textAlign = Paint.Align.CENTER
        }
        val initial = title.firstOrNull()?.toString()?.uppercase() ?: "U"
        val bounds = Rect()
        paint.getTextBounds(initial, 0, initial.length, bounds)
        canvas.drawText(initial, 128f, (128 + bounds.height() / 2).toFloat(), paint)
        return bitmap
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(60 * 60 * 1000L) // 1 hour max safety limit
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()

            audioManager?.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                playerController?.let {
                    if (it.uiState.value.isPlaying) it.togglePlayPause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                playerController?.let {
                    if (it.uiState.value.isPlaying) it.togglePlayPause()
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Audio focus restored
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY = "com.example.umusic.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.umusic.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.umusic.ACTION_NEXT"
        const val ACTION_PREV = "com.example.umusic.ACTION_PREV"
        const val ACTION_STOP = "com.example.umusic.ACTION_STOP"
    }
}

