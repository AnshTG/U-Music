package com.example.data.model

data class EqualizerBand(
    val frequencyHz: Int,
    val name: String,
    val gainDb: Float // Range: -12.0f to +12.0f
)

data class EqualizerPreset(
    val name: String,
    val gains: List<Float> // 5-band: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val currentPreset: String = "Custom",
    val bands: List<EqualizerBand> = defaultBands(),
    val bassBoost: Float = 0.4f, // 0.0f to 1.0f
    val virtualizer: Float = 0.3f, // 0.0f to 1.0f
    val volumeBoostMultiplier: Float = 1.0f, // 1.0f (100%) to 5.0f (500%)
    val playbackSpeed: Float = 1.0f, // 0.5x to 2.0x
    val pitch: Float = 1.0f // 0.8x to 1.2x
) {
    companion object {
        fun defaultBands(): List<EqualizerBand> {
            return listOf(
                EqualizerBand(60, "60Hz", 2.0f),
                EqualizerBand(230, "230Hz", 1.0f),
                EqualizerBand(910, "910Hz", 0.0f),
                EqualizerBand(3600, "3.6kHz", 3.0f),
                EqualizerBand(14000, "14kHz", 4.0f)
            )
        }

        val PRESETS = listOf(
            EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f)),
            EqualizerPreset("Pop", listOf(1.5f, 2.5f, 0f, 2f, 3.5f)),
            EqualizerPreset("Rock", listOf(4f, 2.5f, -1f, 3f, 4.5f)),
            EqualizerPreset("Jazz", listOf(3f, 1f, 1f, 2f, 3f)),
            EqualizerPreset("Classical", listOf(4f, 2.5f, 0f, 2.5f, 3.5f)),
            EqualizerPreset("Electronic", listOf(5f, 3.5f, 0f, 2f, 4f)),
            EqualizerPreset("Hip-Hop", listOf(5f, 3.5f, 0f, 1.5f, 3f)),
            EqualizerPreset("Vocal Booster", listOf(-2f, 1f, 4f, 3f, 1f)),
            EqualizerPreset("Bass Supreme", listOf(6f, 4f, 1f, 0f, -1f))
        )
    }
}

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class SleepTimerOption(val label: String, val minutes: Long) {
    OFF("Off", 0L),
    MIN_5("5 Minutes", 5L),
    MIN_10("10 Minutes", 10L),
    MIN_15("15 Minutes", 15L),
    MIN_30("30 Minutes", 30L),
    MIN_45("45 Minutes", 45L),
    MIN_60("60 Minutes", 60L),
    END_OF_TRACK("End of track", -1L)
}
