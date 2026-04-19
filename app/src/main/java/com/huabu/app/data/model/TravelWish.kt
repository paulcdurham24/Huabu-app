package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_wishes")
data class TravelWish(
    @PrimaryKey val id: String,
    val userId: String,
    val destination: String,
    val country: String,
    val countryCode: String = "",
    val priority: Int = 1, // 1 = high, 2 = medium, 3 = someday
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
