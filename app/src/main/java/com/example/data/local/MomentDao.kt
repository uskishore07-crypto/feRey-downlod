package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Moment
import kotlinx.coroutines.flow.Flow

@Dao
interface MomentDao {
    @Query("SELECT * FROM moments ORDER BY timestamp DESC")
    fun getAllMoments(): Flow<List<Moment>>

    @Query("SELECT * FROM moments WHERE dateString = :dateString ORDER BY timestamp ASC")
    fun getMomentsForDate(dateString: String): Flow<List<Moment>>

    @Query("SELECT * FROM moments WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getMomentsForDateDesc(dateString: String): Flow<List<Moment>>

    @Query("SELECT DISTINCT dateString FROM moments ORDER BY dateString DESC")
    fun getAllDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM moments WHERE dateString = :dateString")
    fun getMomentCountForDate(dateString: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: Moment): Long

    @Delete
    suspend fun deleteMoment(moment: Moment)

    @Query("DELETE FROM moments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM moments")
    suspend fun clearAll()
}
