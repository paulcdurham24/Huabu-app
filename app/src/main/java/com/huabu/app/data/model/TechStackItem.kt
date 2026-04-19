package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tech_stack_items")
data class TechStackItem(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val category: String, // e.g., "Frontend", "Backend", "Mobile", "DevOps", "Database", "Design"
    val proficiency: Int = 50, // 0-100
    val yearsExperience: Float = 0f,
    val iconUrl: String = "",
    val sortOrder: Int = 0
)
