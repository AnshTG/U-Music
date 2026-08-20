package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.EqualizerBand
import com.example.data.model.EqualizerState
import com.example.data.model.RepeatMode
import com.example.data.model.SleepTimerOption
import com.example.data.model.Song
import com.example.data.repository.AudioStreamExtractor
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = 0,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isShuffle: Boolean = false,
    val sleepTimerOption: SleepTimerOption = SleepTimerOption.OFF,
    val sleepTimerRemainingSeconds: Long = 0L,
    val equalizerState: EqualizerState = EqualizerState(),
    val abRepeatA: Long? = null,
    val abRepeatB: Long? = null,
    val isLoading: Boolean = false,
    val isMusicVideoMode: Boolean = false,
    val isCloudStreamActive: Boolean = true
)

class MusicPlayerController(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared: Boolean = false
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    var onPlaybackStateChanged: ((Song?, Boolean) -> Unit)? = null

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var prepareJob: Job? = null
    private var progressJob: Job? = null
    private var sleepTimer: CountDownTimer? = null
    private var _retryAttempted: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        startProgressTracker()
    }

    private fun initPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    isPrepared = true
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        durationMs = mp.duration.toLong().coerceAtLeast(0L)
                    )
                    initAudioEffects()
                    applyPlaybackParams()
                    applyEqualizerSettings(_uiState.value.equalizerState)
                    try {
                        mp.start()
                        _uiState.value = _uiState.value.copy(isPlaying = true)
                        onPlaybackStateChanged?.invoke(_uiState.value.currentSong, true)
                    } catch (e: Exception) {
                        Log.e("MusicPlayerController", "Error starting player in onPrepared: ${e.message}")
                    }
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("MusicPlayerController", "MediaPlayer error: what=$what, extra=$extra")
                    isPrepared = false
                    try {
                        mp.reset()
                    } catch (e: Exception) {
                        // ignore
                    }
                    val current = _uiState.value.currentSong
                    if (current != null && !_retryAttempted) {
                        _retryAttempted = true
                        Log.i("MusicPlayerController", "Attempting playback fallback for: ${current.title}")
                        scope.launch {
                            try {
                                val fallbackSong = AudioStreamExtractor.getIndianTrendingHits().first()
                                val fallbackHeaders = mapOf(
                                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
                                    "Accept" to "*/*"
                                )
                                mp.setDataSource(context, Uri.parse(fallbackSong.audioUrl), fallbackHeaders)
                                mp.prepareAsync()
                            } catch (e: Exception) {
                                Log.e("MusicPlayerController", "Fallback attempt failed: ${e.message}")
                                _uiState.value = _uiState.value.copy(isLoading = false, isPlaying = false)
                                onPlaybackStateChanged?.invoke(current, false)
                            }
                        }
                    } else {
                        _retryAttempted = false
                        _uiState.value = _uiState.value.copy(isLoading = false, isPlaying = false)
                        onPlaybackStateChanged?.invoke(current, false)
                    }
                    true
                }
            }
        }
    }

    private fun initAudioEffects() {
        val player = mediaPlayer ?: return
        if (!isPrepared) return
        try {
            val audioSessionId = player.audioSessionId
            if (audioSessionId != 0) {
                try { equalizer?.release() } catch (_: Exception) {}
                try { bassBoost?.release() } catch (_: Exception) {}
                try { virtualizer?.release() } catch (_: Exception) {}

                equalizer = Equalizer(0, audioSessionId).apply { enabled = _uiState.value.equalizerState.isEnabled }
                bassBoost = BassBoost(0, audioSessionId).apply { enabled = _uiState.value.equalizerState.isEnabled }
                virtualizer = Virtualizer(0, audioSessionId).apply { enabled = _uiState.value.equalizerState.isEnabled }
            }
        } catch (e: Exception) {
            Log.w("MusicPlayerController", "AudioEffects not supported on this session: ${e.message}")
        }
    }

    fun playSong(song: Song, newQueue: List<Song>? = null) {
        _retryAttempted = false
        val queue = newQueue ?: if (_uiState.value.queue.isEmpty()) listOf(song) else _uiState.value.queue
        val index = queue.indexOfFirst { it.id == song.id }.let { if (it == -1) 0 else it }

        _uiState.value = _uiState.value.copy(
            currentSong = song,
            queue = queue,
            queueIndex = index,
            isLoading = true,
            isPlaying = false,
            currentPositionMs = 0L,
            durationMs = song.durationMs
        )

        onPlaybackStateChanged?.invoke(song, true)
        initPlayer()

        prepareJob?.cancel()
        prepareJob = scope.launch {
            try {
                val resolvedAudioUrl = if (song.isOnline && !song.isDownloaded) {
                    AudioStreamExtractor.resolveStreamAudioUrl(song)
                } else {
                    song.audioUrl
                }

                val player = mediaPlayer ?: return@launch
                isPrepared = false
                player.reset()
                if (song.isDownloaded && song.localFilePath.isNotEmpty() && File(song.localFilePath).exists()) {
                    player.setDataSource(song.localFilePath)
                } else if (resolvedAudioUrl.startsWith("http")) {
                    val headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
                        "Accept" to "*/*"
                    )
                    player.setDataSource(context, Uri.parse(resolvedAudioUrl), headers)
                } else if (song.audioUrl.startsWith("content://")) {
                    player.setDataSource(context, Uri.parse(song.audioUrl))
                } else if (song.localFilePath.isNotEmpty()) {
                    player.setDataSource(song.localFilePath)
                } else {
                    val fallbackSong = AudioStreamExtractor.getIndianTrendingHits().first()
                    player.setDataSource(context, Uri.parse(fallbackSong.audioUrl), mapOf("User-Agent" to "Mozilla/5.0"))
                }
                player.prepareAsync()
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore coroutine cancellation when a user rapidly selects another song
            } catch (e: Exception) {
                Log.e("MusicPlayerController", "Error preparing song: ${e.message}")
                isPrepared = false
                _uiState.value = _uiState.value.copy(isLoading = false, isPlaying = false)
                onPlaybackStateChanged?.invoke(song, false)
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null && isPrepared) {
            try {
                if (player.isPlaying) {
                    player.pause()
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                    onPlaybackStateChanged?.invoke(_uiState.value.currentSong, false)
                } else {
                    player.start()
                    _uiState.value = _uiState.value.copy(isPlaying = true)
                    onPlaybackStateChanged?.invoke(_uiState.value.currentSong, true)
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerController", "Toggle play error: ${e.message}")
                _uiState.value.currentSong?.let { playSong(it) }
            }
        } else if (_uiState.value.currentSong != null && !_uiState.value.isLoading) {
            playSong(_uiState.value.currentSong!!)
        }
    }

    fun toggleCloudStreamSource() {
        _uiState.value = _uiState.value.copy(
            isCloudStreamActive = !_uiState.value.isCloudStreamActive
        )
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
        if (player != null && isPrepared && !_uiState.value.isLoading) {
            try {
                player.seekTo(positionMs.toInt())
            } catch (e: Exception) {
                Log.w("MusicPlayerController", "Seek error: ${e.message}")
            }
        }
    }

    fun skipNext() {
        val queue = _uiState.value.queue
        if (queue.isEmpty()) return

        var nextIndex = _uiState.value.queueIndex + 1
        if (_uiState.value.isShuffle && queue.size > 1) {
            nextIndex = (queue.indices).filter { it != _uiState.value.queueIndex }.random()
        } else if (nextIndex >= queue.size) {
            nextIndex = 0
        }

        val nextSong = queue[nextIndex]
        _uiState.value = _uiState.value.copy(queueIndex = nextIndex)
        playSong(nextSong, queue)
    }

    fun skipPrevious() {
        val queue = _uiState.value.queue
        if (queue.isEmpty()) return

        if (_uiState.value.currentPositionMs > 3000) {
            seekTo(0)
            return
        }

        var prevIndex = _uiState.value.queueIndex - 1
        if (prevIndex < 0) {
            prevIndex = queue.size - 1
        }
        val prevSong = queue[prevIndex]
        _uiState.value = _uiState.value.copy(queueIndex = prevIndex)
        playSong(prevSong, queue)
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun setMusicVideoMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMusicVideoMode = enabled)
    }

    private fun handleTrackCompletion() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _uiState.value = _uiState.value.copy(isPlaying = true)
            }
            RepeatMode.ALL -> {
                if (_uiState.value.sleepTimerOption == SleepTimerOption.END_OF_TRACK) {
                    cancelSleepTimer()
                    mediaPlayer?.pause()
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                } else {
                    skipNext()
                }
            }
            RepeatMode.OFF -> {
                if (_uiState.value.queueIndex < _uiState.value.queue.size - 1) {
                    skipNext()
                } else {
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                }
            }
        }
    }

    // Audio Effects and EQ
    fun applyEqualizerSettings(eqState: EqualizerState) {
        _uiState.value = _uiState.value.copy(equalizerState = eqState)
        try {
            equalizer?.let { eq ->
                eq.enabled = eqState.isEnabled
                if (eqState.isEnabled) {
                    val numBands = eq.numberOfBands.toInt()
                    eqState.bands.forEachIndexed { index, band ->
                        if (index < numBands) {
                            val minLevel = eq.bandLevelRange[0]
                            val maxLevel = eq.bandLevelRange[1]
                            // Map -12dB..+12dB to minLevel..maxLevel (usually -1500mB to +1500mB)
                            val level = ((band.gainDb / 12.0f) * maxLevel).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                            eq.setBandLevel(index.toShort(), level.toShort())
                        }
                    }
                }
            }
            bassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    bb.enabled = eqState.isEnabled
                    bb.setStrength((eqState.bassBoost * 1000).toInt().toShort())
                }
            }
            virtualizer?.let { virt ->
                if (virt.strengthSupported) {
                    virt.enabled = eqState.isEnabled
                    virt.setStrength((eqState.virtualizer * 1000).toInt().toShort())
                }
            }

            // Software Volume boost multiplier
            val volumeMultiplier = eqState.volumeBoostMultiplier.coerceIn(1.0f, 5.0f)
            val normalizedVol = (1.0f * volumeMultiplier).coerceAtMost(5.0f)
            mediaPlayer?.setVolume(normalizedVol, normalizedVol)

            applyPlaybackParams()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyPlaybackParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                params.speed = _uiState.value.equalizerState.playbackSpeed
                params.pitch = _uiState.value.equalizerState.pitch
                mediaPlayer?.playbackParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSleepTimer(option: SleepTimerOption) {
        sleepTimer?.cancel()
        if (option == SleepTimerOption.OFF || option == SleepTimerOption.END_OF_TRACK) {
            _uiState.value = _uiState.value.copy(
                sleepTimerOption = option,
                sleepTimerRemainingSeconds = 0L
            )
            return
        }

        val totalMs = option.minutes * 60 * 1000
        _uiState.value = _uiState.value.copy(
            sleepTimerOption = option,
            sleepTimerRemainingSeconds = option.minutes * 60
        )

        sleepTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(
                    sleepTimerRemainingSeconds = millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                mediaPlayer?.pause()
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    sleepTimerOption = SleepTimerOption.OFF,
                    sleepTimerRemainingSeconds = 0L
                )
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        _uiState.value = _uiState.value.copy(
            sleepTimerOption = SleepTimerOption.OFF,
            sleepTimerRemainingSeconds = 0L
        )
    }

    fun setABRepeat(aMs: Long?, bMs: Long?) {
        _uiState.value = _uiState.value.copy(abRepeatA = aMs, abRepeatB = bMs)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentQueue = _uiState.value.queue.toMutableList()
        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices) {
            val item = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, item)
            val newCurrentIndex = currentQueue.indexOfFirst { it.id == _uiState.value.currentSong?.id }
            _uiState.value = _uiState.value.copy(queue = currentQueue, queueIndex = if (newCurrentIndex >= 0) newCurrentIndex else 0)
        }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _uiState.value.queue.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            val newCurrentIndex = currentQueue.indexOfFirst { it.id == _uiState.value.currentSong?.id }
            _uiState.value = _uiState.value.copy(queue = currentQueue, queueIndex = if (newCurrentIndex >= 0) newCurrentIndex else 0)
        }
    }

    fun addToQueue(song: Song) {
        val currentQueue = _uiState.value.queue.toMutableList()
        currentQueue.add(song)
        _uiState.value = _uiState.value.copy(queue = currentQueue)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                try {
                    val player = mediaPlayer
                    if (player != null && player.isPlaying) {
                        val pos = player.currentPosition.toLong()
                        val dur = player.duration.toLong().let { if (it > 0) it else _uiState.value.durationMs }

                        // Check A-B repeat
                        val a = _uiState.value.abRepeatA
                        val b = _uiState.value.abRepeatB
                        if (a != null && b != null && b > a && pos >= b) {
                            seekTo(a)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                currentPositionMs = pos,
                                durationMs = dur
                            )
                        }
                    } else if (_uiState.value.isPlaying && player == null) {
                        // Simulated progression fallback
                        val newPos = (_uiState.value.currentPositionMs + 500)
                        if (newPos >= _uiState.value.durationMs && _uiState.value.durationMs > 0) {
                            handleTrackCompletion()
                        } else {
                            _uiState.value = _uiState.value.copy(currentPositionMs = newPos)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore state errors during fast seeking
                }
                delay(500)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        sleepTimer?.cancel()
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }
}
