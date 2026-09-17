package com.animdex.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM anim_dex_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<AnimalEntry>>

    @Query("SELECT * FROM anim_dex_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): AnimalEntry?

    @Query("SELECT COUNT(*) FROM anim_dex_entries")
    fun getEntryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AnimalEntry): Long

    @Delete
    suspend fun deleteEntry(entry: AnimalEntry)
}
