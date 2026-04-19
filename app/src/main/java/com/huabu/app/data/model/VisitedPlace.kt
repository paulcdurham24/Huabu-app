package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visited_places")
data class VisitedPlace(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String, // City name
    val country: String,
    val countryCode: String = "", // emoji flag
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val visitedAt: Long = System.currentTimeMillis(),
    val photoUrl: String = "",
    val memory: String = ""
)
