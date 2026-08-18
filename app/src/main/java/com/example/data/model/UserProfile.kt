package com.example.data.model

data class UserProfile(
    val name: String = "Alex",
    val age: Int = 22,
    val country: String = "Global",
    val favoriteGenres: List<String> = listOf("Pop", "Electronic", "Hip-Hop", "Lo-Fi"),
    val favoriteArtists: List<String> = listOf("The Weeknd", "Dua Lipa", "Imagine Dragons"),
    val isOnboardingCompleted: Boolean = false,
    val totalMinutesListened: Int = 342,
    val songsPlayedCount: Int = 89,
    val topGenre: String = "Electronic",
    val topArtist: String = "The Weeknd"
)
