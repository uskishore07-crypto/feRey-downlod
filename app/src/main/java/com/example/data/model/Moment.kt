package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moments")
data class Moment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // Format: "yyyy-MM-dd"
    val timeString: String, // Format: "hh:mm a"
    val type: String, // "PHOTO", "THOUGHT", "MUSIC", "COMBINED"
    val thoughtText: String? = null,
    val imageUri: String? = null,
    val songTitle: String? = null,
    val artistName: String? = null,
    val userId: String = "default"
)
