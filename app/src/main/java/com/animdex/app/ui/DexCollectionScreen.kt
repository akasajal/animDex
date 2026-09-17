package com.animdex.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.animdex.app.data.db.AnimalEntry
import com.animdex.app.ui.theme.CrimsonAccent
import com.animdex.app.ui.theme.DarkSurfaceVariant
import com.animdex.app.ui.theme.EmeraldDark
import com.animdex.app.ui.theme.EmeraldPrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getGroupIcon(groupName: String): ImageVector {
    return when (groupName.lowercase()) {
        "all" -> Icons.Default.Apps
        "mammal" -> Icons.Default.Pets
        "bird" -> Icons.Default.Flight
        "reptile" -> Icons.Default.Terrain
        "amphibian" -> Icons.Default.WaterDrop
        "fish" -> Icons.Default.Waves
        "invertebrate" -> Icons.Default.BugReport
        else -> Icons.Default.Category
    }
}

@Composable
fun DexCollectionScreen(
    entries: List<AnimalEntry>,
    totalCount: Int,
    onDeleteEntry: (AnimalEntry) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedEntryForDetail by remember { mutableStateOf<AnimalEntry?>(null) }

    val filterOptions = listOf("All", "Mammal", "Bird", "Reptile", "Amphibian", "Fish", "Invertebrate")

    val filteredEntries = if (selectedFilter == "All") {
        entries
    } else {
        entries.filter { it.groupName.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AnimDex",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    text = "Multi-Model ML Wildlife Catalog",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Catch Counter Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EmeraldDark.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$totalCount Discovered",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    leadingIcon = {
                        Icon(
                            imageVector = getGroupIcon(filter),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) Color.Black else EmeraldPrimary
                        )
                    },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        // Collection Grid or Empty State
        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (entries.isEmpty()) "Your AnimDex is empty" else "No $selectedFilter entries",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (entries.isEmpty())
                            "Switch to the Scanner tab to identify your first animal!"
                        else
                            "Scan more species with specialized models to fill this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    AnimalCard(
                        entry = entry,
                        onClick = { selectedEntryForDetail = entry }
                    )
                }
            }
        }
    }

    // Detail & Delete Dialog
    selectedEntryForDetail?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntryForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getGroupIcon(entry.groupName),
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = entry.speciesName)
                        }
                        if (entry.scientificName.isNotEmpty()) {
                            Text(
                                text = entry.scientificName,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                            )
                        }
                    }
                    IconButton(onClick = {
                        onDeleteEntry(entry)
                        selectedEntryForDetail = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = CrimsonAccent
                        )
                    }
                }
            },
            text = {
                Column {
                    if (entry.photoUri.isNotEmpty()) {
                        AsyncImage(
                            model = File(entry.photoUri),
                            contentDescription = entry.speciesName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Match: ${(entry.confidence * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (entry.modelSource.isNotEmpty()) {
                            Text(
                                text = entry.modelSource,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entry.funFact,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
                    Text(
                        text = "Registered: $dateFormatted",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntryForDetail = null }) {
                    Text("Close", color = EmeraldPrimary)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

@Composable
private fun AnimalCard(
    entry: AnimalEntry,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                if (entry.photoUri.isNotEmpty()) {
                    AsyncImage(
                        model = File(entry.photoUri),
                        contentDescription = entry.speciesName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        Icon(
                            imageVector = getGroupIcon(entry.groupName),
                            contentDescription = entry.groupName,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = entry.speciesName,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.scientificName.isNotEmpty()) {
                    Text(
                        text = entry.scientificName,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.groupName,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${(entry.confidence * 100).toInt()}%",
                        color = EmeraldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
