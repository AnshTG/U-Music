package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SleepTimerOption
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

/**
 * Dialog component that allows users to set a sleep timer to automatically stop audio playback after a specified duration.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerDialog(
    currentOption: SleepTimerOption,
    remainingSeconds: Long,
    onOptionSelected: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit
) {
    var customMinutes by remember {
        mutableFloatStateOf(
            if (currentOption != SleepTimerOption.OFF && currentOption != SleepTimerOption.END_OF_TRACK) {
                currentOption.minutes.toFloat().coerceIn(5f, 120f)
            } else {
                30f
            }
        )
    }
    var isCustomMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("sleep_timer_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer Icon",
                        tint = ElectricViolet,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Stop playback automatically",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Countdown Banner if timer is running
                if (remainingSeconds > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ElectricViolet.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Timer Active",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    val minutesLeft = remainingSeconds / 60
                                    val secondsLeft = remainingSeconds % 60
                                    Text(
                                        text = "%02d:%02d remaining".format(minutesLeft, secondsLeft),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = ElectricViolet,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    onOptionSelected(SleepTimerOption.OFF)
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("turn_off_timer_button")
                            ) {
                                Text("Turn Off", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Text(
                    text = "Select Duration",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Quick Preset Options
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        SleepTimerOption.OFF,
                        SleepTimerOption.MIN_5,
                        SleepTimerOption.MIN_15,
                        SleepTimerOption.MIN_30,
                        SleepTimerOption.MIN_45,
                        SleepTimerOption.MIN_60,
                        SleepTimerOption.END_OF_TRACK
                    )

                    presets.forEach { option ->
                        val isSelected = !isCustomMode && currentOption == option
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomMode = false
                                onOptionSelected(option)
                                onDismiss()
                            },
                            label = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (option == SleepTimerOption.END_OF_TRACK) {
                                    Icon(
                                        imageVector = Icons.Default.MusicOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (option == SleepTimerOption.OFF) {
                                    Icon(
                                        imageVector = Icons.Default.TimerOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricViolet,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("timer_option_${option.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Duration Toggle & Slider
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isCustomMode = true },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCustomMode) {
                            ElectricViolet.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AvTimer,
                                    contentDescription = null,
                                    tint = if (isCustomMode) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Custom Duration",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCustomMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCustomMode) ElectricViolet else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Text(
                                text = "${customMinutes.toInt()} min",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCustomMode) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Slider(
                            value = customMinutes,
                            onValueChange = {
                                customMinutes = it
                                isCustomMode = true
                            },
                            valueRange = 1f..120f,
                            steps = 23, // 5 min intervals roughly
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricViolet,
                                activeTrackColor = ElectricViolet,
                                inactiveTrackColor = ElectricViolet.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_timer_slider")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("timer_cancel_button")
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (isCustomMode) {
                    FilledTonalButton(
                        onClick = {
                            val mins = customMinutes.toLong()
                            val option = when (mins) {
                                5L -> SleepTimerOption.MIN_5
                                10L -> SleepTimerOption.MIN_10
                                15L -> SleepTimerOption.MIN_15
                                30L -> SleepTimerOption.MIN_30
                                45L -> SleepTimerOption.MIN_45
                                60L -> SleepTimerOption.MIN_60
                                else -> SleepTimerOption.MIN_30 // closest or custom
                            }
                            onOptionSelected(option)
                            onDismiss()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectricViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("timer_apply_custom_button")
                    ) {
                        Text("Set ${customMinutes.toInt()}m")
                    }
                }
            }
        }
    )
}

