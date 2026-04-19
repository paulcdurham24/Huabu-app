package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_events")
data class ProfileEvent(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String = "",
    val location: String = "",
    val eventDate: Long,
    val isOnline: Boolean = false,
    val eventUrl: String = "",
    val rsvpCount: Int = 0,
    val hasRsvped: Boolean = false,
    val coverImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
