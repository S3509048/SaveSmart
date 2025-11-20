package uk.ac.tees.mad.savesmart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    // Insert or update goal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    // Get all goals for user (Flow for reactive updates)
    @Query("SELECT * FROM goals_cache WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsFlow(userId: String): Flow<List<GoalEntity>>

    // Get all goals (one-time)
    @Query("SELECT * FROM goals_cache WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getGoals(userId: String): List<GoalEntity>

    // Get single goal
    @Query("SELECT * FROM goals_cache WHERE goalId = :goalId LIMIT 1")
    suspend fun getGoal(goalId: String): GoalEntity?

    // Update current amount
    @Query("UPDATE goals_cache SET currentAmount = :newAmount, updatedAt = :timestamp, isSynced = :synced WHERE goalId = :goalId")
    suspend fun updateGoalAmount(goalId: String, newAmount: Double, timestamp: Long, synced: Boolean = true)

    // Update currency for all goals
    @Query("UPDATE goals_cache SET currency = :currency WHERE userId = :userId")
    suspend fun updateCurrency(userId: String, currency: String)

    // Delete goal
    @Query("DELETE FROM goals_cache WHERE goalId = :goalId")
    suspend fun deleteGoal(goalId: String)

    // Delete all goals (for logout)
    @Query("DELETE FROM goals_cache WHERE userId = :userId")
    suspend fun deleteAllGoals(userId: String)

    // Get unsynced goals
    @Query("SELECT * FROM goals_cache WHERE isSynced = 0")
    suspend fun getUnsyncedGoals(): List<GoalEntity>
}