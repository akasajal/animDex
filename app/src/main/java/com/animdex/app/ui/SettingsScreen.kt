package com.animdex.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animdex.app.ui.theme.AccentColors
import com.animdex.app.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentAccent: Color,
    totalDiscoveries: Int,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onAccentColorChanged: (Color) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Top Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Customize appearance & preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // APPEARANCE SECTION
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Section Title: APPEARANCE
                Text(
                    text = "APPEARANCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = currentAccent
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ROW 1: Theme (System | Dark | Light segmented button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Theme",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 3-Segmented Pill Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .height(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeSegment(
                            label = "System",
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            accent = currentAccent,
                            isFirst = true,
                            isLast = false,
                            onClick = { onThemeModeChanged(ThemeMode.SYSTEM) }
                        )

                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ThemeSegment(
                            label = "Dark",
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            accent = currentAccent,
                            isFirst = false,
                            isLast = false,
                            onClick = { onThemeModeChanged(ThemeMode.DARK) }
                        )

                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ThemeSegment(
                            label = "Light",
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            accent = currentAccent,
                            isFirst = false,
                            isLast = true,
                            onClick = { onThemeModeChanged(ThemeMode.LIGHT) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ROW 2: Accent Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accent Color",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 6 Circular Color Swatches
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AccentColors.all.forEach { (name, color) ->
                            val isSelected = currentAccent == color
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                shape = CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .padding(if (isSelected) 3.dp else 0.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { onAccentColorChanged(color) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MACHINE LEARNING SECTION
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ON-DEVICE MACHINE LEARNING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = currentAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ModelItem(
                    title = "iNaturalist Birds Model",
                    subtitle = "965 species with binomial Latin names",
                    size = "3.57 MB • Active"
                )
                Spacer(modifier = Modifier.height(12.dp))
                ModelItem(
                    title = "iNaturalist Insects Model",
                    subtitle = "1,022 invertebrate & butterfly species",
                    size = "3.65 MB • Active"
                )
                Spacer(modifier = Modifier.height(12.dp))
                ModelItem(
                    title = "General Fauna & Wildlife Model",
                    subtitle = "Mammals, reptiles, fish & scene evaluator",
                    size = "4.28 MB • Active"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DATA & STORAGE SECTION
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DATABASE & PRIVACY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = currentAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Registered Discoveries",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stored on local Room SQLite database",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$totalDiscoveries catches",
                        color = currentAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "No images or data leave your phone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = currentAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun ThemeSegment(
    label: String,
    isSelected: Boolean,
    accent: Color,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val shape = when {
        isFirst -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
        isLast -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModelItem(
    title: String,
    subtitle: String,
    size: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = size,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
