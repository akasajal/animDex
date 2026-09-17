package com.animdex.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anim_dex_entries")
data class AnimalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val speciesName: String,
    val scientificName: String = "",
    val modelSource: String = "",
    val rawLabel: String,
    val groupName: String,
    val groupEmoji: String,
    val confidence: Float,
    val photoUri: String,
    val funFact: String,
    val timestamp: Long = System.currentTimeMillis()
)
