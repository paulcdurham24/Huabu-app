package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.ProfilePoll
import com.huabu.app.data.model.PollVote
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfilePollDao {
    @Query("SELECT * FROM profile_polls WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActivePolls(userId: String): Flow<List<ProfilePoll>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPoll(poll: ProfilePoll)

    @Query("DELETE FROM profile_polls WHERE id = :pollId")
    suspend fun deletePoll(pollId: String)

    @Query("UPDATE profile_polls SET votesA = votesA + 1 WHERE id = :pollId")
    suspend fun voteA(pollId: String)

    @Query("UPDATE profile_polls SET votesB = votesB + 1 WHERE id = :pollId")
    suspend fun voteB(pollId: String)

    @Query("UPDATE profile_polls SET votesC = votesC + 1 WHERE id = :pollId")
    suspend fun voteC(pollId: String)

    @Query("UPDATE profile_polls SET votesD = votesD + 1 WHERE id = :pollId")
    suspend fun voteD(pollId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordVote(vote: PollVote)

    @Query("SELECT option FROM poll_votes WHERE pollId = :pollId AND voterId = :voterId LIMIT 1")
    suspend fun getUserVote(pollId: String, voterId: String): Char?
}
