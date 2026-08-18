package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Accents
val ElectricViolet = Color(0xFF8B5CF6)
val ElectricVioletLight = Color(0xFFA78BFA)
val NeonCyan = Color(0xFF06B6D4)
val NeonCyanLight = Color(0xFF22D3EE)
val NeonPink = Color(0xFFF43F5E)
val NeonPinkLight = Color(0xFFFB7185)
val AmberGold = Color(0xFFF59E0B)
val AmberGoldLight = Color(0xFFFBBF24)
val EmeraldGreen = Color(0xFF10B981)
val EmeraldGreenLight = Color(0xFF34D399)
val SunsetOrange = Color(0xFFFF5722)
val SunsetOrangeLight = Color(0xFFFF8A65)
val OceanBlue = Color(0xFF3B82F6)
val OceanBlueLight = Color(0xFF60A5FA)

enum class AppAccent(val displayName: String, val primary: Color, val light: Color) {
    VIOLET("Violet", ElectricViolet, ElectricVioletLight),
    CYAN("Cyan", NeonCyan, NeonCyanLight),
    EMERALD("Emerald", EmeraldGreen, EmeraldGreenLight),
    PINK("Rose Pink", NeonPink, NeonPinkLight),
    AMBER("Amber Gold", AmberGold, AmberGoldLight),
    OCEAN("Ocean Blue", OceanBlue, OceanBlueLight),
    SUNSET("Sunset Orange", SunsetOrange, SunsetOrangeLight);

    companion object {
        fun fromName(name: String): AppAccent =
            values().find { it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) } ?: VIOLET
    }
}

// Dark / Obsidian Palette
val DarkBackground = Color(0xFF0D0E15)
val DarkSurface = Color(0xFF151722)
val DarkSurfaceVariant = Color(0xFF1F2232)
val DarkCard = Color(0xFF25293C)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

// AMOLED Palette
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0E)
val AmoledSurfaceVariant = Color(0xFF141418)

// Light Palette
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)
