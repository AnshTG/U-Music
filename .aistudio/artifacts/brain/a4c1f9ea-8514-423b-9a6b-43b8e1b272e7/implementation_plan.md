# Implementation Plan: Music App Quality, Playlists & Offline Enhancements

## 1. Search Loading & Timeout Fix
- **Root Cause**: Search job in `MusicViewModel` and `AudioStreamExtractor` could hang on network calls or when parsing without strict timeouts, leaving `isLoading = true` indefinitely. Furthermore, `SearchScreen` does not show empty/timeout fallback states when queries fail.
- **Solution**:
  - Add explicit `withTimeoutOrNull(4000L)` on search network calls in `AudioStreamExtractor` and `MusicViewModel`.
  - Always reset `isLoading = false` in a `finally` block or when query is cleared.
  - In `SearchScreen`, show a clean empty state or retry prompt when search yields no matches after timeout rather than spinning forever.

## 2. Deletable Downloaded Songs & Renaming from "Cached"
- **Rename**: Convert all references from "Cached" to "Downloaded Songs" in UI, headers, menus, and labels.
- **Individual Deletion**:
  - In `DownloadedSongsScreen` and `SongItemRow` / action menus, add a direct Delete icon button and "Delete from Downloads" action.
  - Delete audio file from storage and update Room database (`isDownloaded = false`, `downloadedUri = null`).
- **Bulk Clear Option**:
  - Add a "Clear All Downloads" action button with confirmation dialog in `DownloadedSongsScreen` and `SettingsScreen`.

## 3. Playlists: Remove Pre-made, Empty New Playlists, Online vs Offline Types
- **Remove Pre-made Playlists**:
  - Remove seeding of `pl_trending`, `pl_chill`, `pl_workout` in `MusicRepository.initializeCatalog()`.
  - New users will start with 0 playlists. Any newly created playlist begins completely empty (`songCount = 0`, no songs pre-added).
- **Playlist Types (Online vs Offline)**:
  - Add `isOffline: Boolean` and `type: PlaylistType` to `Playlist` model.
  - In `CreatePlaylistDialog`, provide a toggle/selector:
    - **Online Playlist**: For cloud streaming tracks and general collections.
    - **Offline Playlist**: Dedicated to offline playback.
  - **Auto-Download Logic**: When a song is added to an Offline Playlist, if it is not already downloaded, automatically trigger download in the background with toast notification.
  - In Library / You screen and Playlist Detail, show distinct Online / Offline badges.

## 4. Advanced Playlist Song Management (Add, Remove, Move)
- **Add Song to Playlist**:
  - Enhanced dialog listing all user playlists (distinguishing Online and Offline).
- **Remove Song from Playlist**:
  - In `PlaylistDetailScreen`, add a "Remove from Playlist" action in the 3-dot menu or trailing action icon.
- **Move Song to Another Playlist**:
  - In `PlaylistDetailScreen` 3-dot menu, add "Move to Another Playlist" dialog: removes the song from current playlist and inserts it into destination playlist.

## 5. Distinct Authentic Song Covers & Loading Screens
- **Fix Duplicate Covers**:
  - Replace repeated Unsplash fallback image URL with distinct, authentic high-res album artworks in `AudioStreamExtractor` and catalog initializers.
  - Use real, verified artwork URLs for all trending songs across Bollywood, Punjabi, International Pop, and Regional tracks.
  - Add Coil crossfade, loading shimmer placeholder, and error fallback icon in `SongItemRow`, `FullScreenPlayer`, `MiniPlayer`, and `HomeScreen`.
- **Loading Indicators**:
  - Add loading screens/shimmers during initial catalog initialization and playlist loading so screens never feel stuck.

## 6. Home Screen Initial State & Freshness
- Seed diverse, top-tier songs with unique artist/artwork info on first launch.
- Prevent identical repeat items on Home screen.

## 7. Admin Panel Controls
- Add controls in `AdminPanelScreen`:
  - Search Timeout Duration & Instant Fail Simulation.
  - Delete All Downloads action & download count manager.
  - Default Playlist Seeding toggle (Enable/Disable pre-made playlists).
  - Offline Playlist Auto-Download Toggle.
  - Home Screen Catalog Source & Artwork Mode.

## Proposed Changes
1. **`com.example.data.model.Playlist.kt`**: Add `isOffline: Boolean = false`.
2. **`com.example.data.local.AppDatabase.kt` & DAOs**: Update queries if needed to support removing and moving songs between playlists.
3. **`com.example.data.repository.AudioStreamExtractor.kt`**: Fix timeouts, distinct cover arts, robust parsing.
4. **`com.example.data.repository.MusicRepository.kt`**: Remove pre-made playlists, add moveSongBetweenPlaylists, removeSongFromPlaylist, bulk delete downloads.
5. **`com.example.player.MusicPlayerController.kt` & `MusicViewModel.kt`**: Implement search timeout, playlist move/remove logic, auto-download on offline playlist addition.
6. **`com.example.ui.screens.SearchScreen.kt`**: Fix infinite loading animation with clear timeout handling.
7. **`com.example.ui.screens.DownloadedSongsScreen.kt`**: Renamed, add individual delete and clear all options.
8. **`com.example.ui.screens.PlaylistDetailScreen.kt`**: Add remove song, move song, and offline status.
9. **`com.example.ui.screens.AdminPanelScreen.kt`**: Add controls for all updated features.
10. **`com.example.MainActivity.kt`**: Fix compile errors (state variables `showAdminPanelScreen`, `showDownloadedSongsScreen`), connect all dialogs.

## Verification Plan
1. Compile app with `compile_applet` to ensure zero compilation or type errors.
2. Verify search loading indicator terminates properly within timeout.
3. Verify downloaded songs can be deleted individually and in bulk.
4. Verify creating a playlist produces an empty playlist of chosen type (Online / Offline).
5. Verify adding a song to an offline playlist initiates download.
6. Verify moving a song from one playlist to another works seamlessly.
7. Verify distinct cover arts render properly on Home and player screens.
