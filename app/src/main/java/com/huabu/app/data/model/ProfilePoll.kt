package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_polls")
data class ProfilePoll(
    @PrimaryKey val id: String,
    val userId: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String = "",
    val optionD: String = "",
    val votesA: Int = 0,
    val votesB: Int = 0,
    val votesC: Int = 0,
    val votesD: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val endsAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000, // 7 days default
    val isActive: Boolean = true
) {
    fun totalVotes(): Int = votesA + votesB + votesC + votesD
    fun percent(option: Char): Float {
        val total = totalVotes()
        if (total == 0) return 0f
        return when (option) {
            'A' -> votesA * 100f / total
            'B' -> votesB * 100f / total
            'C' -> votesC * 100f / total
            'D' -> votesD * 100f / total
            else -> 0f
        }
    }
}

@Entity(tableName = "poll_votes", primaryKeys = ["pollId", "voterId"])
data class PollVote(
    val pollId: String,
    val voterId: String,
    val option: Char, // 'A', 'B', 'C', or 'D'
    val votedAt: Long = System.currentTimeMillis()
)
