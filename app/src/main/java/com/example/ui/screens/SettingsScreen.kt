package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppAccent
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    currentAccent: String = "Violet",
    isAdBlockEnabled: Boolean,
    isAdaptiveQualityEnabled: Boolean = true,
    effectiveStreamingQuality: String = "High (256 kbps)",
    detectedBandwidthKbps: Int = 2500,
    networkTypeName: String = "Wi-Fi",
    streamingQuality: String,
    downloadQuality: String,
    crossfadeSec: Int,
    gaplessPlayback: Boolean,
    dataSaver: Boolean,
    normalizeVolume: Boolean,
    ytBackgroundPlayback: Boolean = true,
    ytPreferStream: Boolean = true,
    ytAutoMatch: Boolean = true,
    onBack: () -> Unit,
    onOpenAdminPanel: () -> Unit = {},
    onThemeChange: (String) -> Unit,
    onAccentChange: (String) -> Unit = {},
    onAdBlockChange: (Boolean) -> Unit,
    onAdaptiveQualityEnabledChange: (Boolean) -> Unit = {},
    onStreamingQualityChange: (String) -> Unit,
    onDownloadQualityChange: (String) -> Unit,
    onCrossfadeChange: (Int) -> Unit,
    onGaplessChange: (Boolean) -> Unit,
    onDataSaverChange: (Boolean) -> Unit,
    onNormalizeVolumeChange: (Boolean) -> Unit,
    onYtBackgroundPlaybackChange: (Boolean) -> Unit = {},
    onYtPreferStreamChange: (Boolean) -> Unit = {},
    onYtAutoMatchChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showQualityDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings & Preferences",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cloud Streaming Engine Section
            item {
                SettingsCategoryHeader("CLOUD STREAMING ENGINE")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Background Playback Engine
                        SettingsToggleRow(
                            icon = Icons.Default.PlayCircle,
                            title = "Background Audio Playback",
                            subtitle = "Keep music playing seamlessly when screen is locked or switching apps",
                            checked = ytBackgroundPlayback,
                            onCheckedChange = onYtBackgroundPlaybackChange,
                            accentColor = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // High Definition Stream
                        SettingsToggleRow(
                            icon = Icons.Default.PlayCircle,
                            title = "High-Definition Audio Engine",
                            subtitle = "Stream high quality 320kbps lossless audio streams in background",
                            checked = ytPreferStream,
                            onCheckedChange = onYtPreferStreamChange,
                            accentColor = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Auto-Match Catalog
                        SettingsToggleRow(
                            icon = Icons.Default.Refresh,
                            title = "Auto-Match Cloud Metadata",
                            subtitle = "Automatically link and enrich local tracks with cloud streams",
                            checked = ytAutoMatch,
                            onCheckedChange = onYtAutoMatchChange,
                            accentColor = NeonCyan
                        )
                    }
                }
            }

            // Online Streaming Section
            item {
                SettingsCategoryHeader("ONLINE STREAMING & QUALITY")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Ad-Blocking Switch
                        SettingsToggleRow(
                            icon = Icons.Default.Block,
                            title = "Legal Endpoint Ad-Filter",
                            subtitle = "Block ad servers, telemetry tracking endpoints, and promotional tracking beacons compliant with lawful privacy filtering",
                            checked = isAdBlockEnabled,
                            onCheckedChange = onAdBlockChange,
                            accentColor = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Adaptive Stream Quality Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.NetworkCheck,
                            title = "Adaptive Stream Quality (Auto-adjust, lowest 48k)",
                            subtitle = "Automatically scales audio bitrate to match live connection speed. Lowest floor is 48 kbps.",
                            checked = isAdaptiveQualityEnabled,
                            onCheckedChange = onAdaptiveQualityEnabledChange,
                            accentColor = ElectricViolet
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Active Network Condition & Effective Bitrate Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Current: $effectiveStreamingQuality",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAdaptiveQualityEnabled) NeonCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$networkTypeName • ${detectedBandwidthKbps} kbps downstream",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAdaptiveQualityEnabled) NeonCyan.copy(alpha = 0.2f) else ElectricViolet.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isAdaptiveQualityEnabled) "AUTO" else "MANUAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAdaptiveQualityEnabled) NeonCyan else ElectricViolet
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Audio Streaming Quality / Manual Override
                        SettingsClickableRow(
                            icon = Icons.Default.HighQuality,
                            title = "Manual Bitrate Override",
                            value = if (isAdaptiveQualityEnabled) "Auto ($effectiveStreamingQuality)" else "$streamingQuality (Locked)",
                            onClick = { showQualityDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Offline In-App Cache Quality
                        SettingsClickableRow(
                            icon = Icons.Default.Storage,
                            title = "In-App Offline Cache Quality",
                            value = "$downloadQuality (Isolated In-App Cache)",
                            onClick = { showDownloadQualityDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Storage Notice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local Storage Export: Disabled. Offline listening is preserved via secure in-app cache sandbox.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Data Saver Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.NetworkCheck,
                            title = "Data Saver Mode",
                            subtitle = "Stream lower bitrate (96 kbps) on mobile data",
                            checked = dataSaver,
                            onCheckedChange = onDataSaverChange
                        )
                    }
                }
            }

            // Playback & Transitions Section
            item {
                SettingsCategoryHeader("PLAYBACK & TRANSITIONS")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Crossfade Duration Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Crossfade Duration",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${crossfadeSec}s",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricViolet
                                )
                            )
                        }

                        Slider(
                            value = crossfadeSec.toFloat(),
                            onValueChange = { onCrossfadeChange(it.toInt()) },
                            valueRange = 0f..12f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricViolet,
                                activeTrackColor = ElectricViolet
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Gapless Playback Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.GraphicEq,
                            title = "Gapless Playback",
                            subtitle = "Eliminate silence between continuous album tracks",
                            checked = gaplessPlayback,
                            onCheckedChange = onGaplessChange
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Volume Normalization Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Normalize Volume",
                            subtitle = "Set uniform loudness across all tracks",
                            checked = normalizeVolume,
                            onCheckedChange = onNormalizeVolumeChange
                        )
                    }
                }
            }

            // Appearance & App Section
            item {
                SettingsCategoryHeader("APPEARANCE & THEME")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsClickableRow(
                            icon = Icons.Default.DarkMode,
                            title = "Theme Mode",
                            value = currentTheme,
                            onClick = { showThemeDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingsClickableRow(
                            icon = Icons.Default.ColorLens,
                            title = "Theme Accent Color",
                            value = currentAccent,
                            onClick = { showAccentDialog = true }
                        )
                    }
                }
            }

            // Admin & System Console Section
            item {
                SettingsCategoryHeader("ADMINISTRATION & SYSTEM CONSOLE")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenAdminPanel() }
                        .testTag("settings_open_admin_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Admin & Feature Controls",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CONSOLE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = NeonCyan
                                        )
                                    }
                                }
                                Text(
                                    text = "Manage feature flags, purge cache & diagnostics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Developer & Attribution Section
            item {
                SettingsCategoryHeader("DEVELOPER & CREDITS")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Developer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Made by Ansh Yadav",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lead Designer & Developer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // About & Privacy Section
            item {
                SettingsCategoryHeader("ABOUT U MUSIC")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "U Music Pro v1.0.0",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Crafted with passion by Ansh Yadav",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Isolated In-App Cache: Offline tracks are stored safely inside app sandbox.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Quality Dialog
    if (showQualityDialog) {
        val qualityOptions = listOf(
            "Auto (Adaptive 48k-320k)" to "Automatically adjusts to network condition (48k min)",
            "Ultra (320 kbps)" to "Studio Master Quality (Best on Wi-Fi)",
            "High (256 kbps)" to "High Definition Audio",
            "Medium (128 kbps)" to "Balanced Quality (3G/Cellular)",
            "Low (48 kbps)" to "Lowest Floor / Weak 2G & Data Saver"
        )
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Select Streaming Quality") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    qualityOptions.forEach { (option, desc) ->
                        val isSelected = if (option.startsWith("Auto")) isAdaptiveQualityEnabled else (!isAdaptiveQualityEnabled && streamingQuality == option)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (option.startsWith("Auto")) {
                                        onAdaptiveQualityEnabledChange(true)
                                    } else {
                                        onAdaptiveQualityEnabledChange(false)
                                        onStreamingQualityChange(option)
                                    }
                                    showQualityDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ElectricViolet)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    listOf("Dark", "AMOLED", "Light", "Auto").forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChange(t)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t, style = MaterialTheme.typography.bodyLarge)
                            if (currentTheme == t) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ElectricViolet)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Download Quality Dialog
    if (showDownloadQualityDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadQualityDialog = false },
            title = { Text("Select Download Quality") },
            text = {
                Column {
                    listOf("High (320 kbps)", "Normal (160 kbps)", "Lossless FLAC").forEach { dq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDownloadQualityChange(dq)
                                    showDownloadQualityDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dq, style = MaterialTheme.typography.bodyLarge)
                            if (downloadQuality == dq) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadQualityDialog = false }) { Text("Close") }
            }
        )
    }

    // Accent Color Selection Dialog
    if (showAccentDialog) {
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            title = { Text("Select Theme Accent Color") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppAccent.values().forEach { accent ->
                        val isSelected = accent.displayName.equals(currentAccent, ignoreCase = true) || accent.name.equals(currentAccent, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAccentChange(accent.displayName)
                                    showAccentDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(accent.primary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = accent.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = accent.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingsCategoryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        ),
        color = ElectricViolet,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color = ElectricViolet
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = ElectricViolet
            )
        }
    }
}
