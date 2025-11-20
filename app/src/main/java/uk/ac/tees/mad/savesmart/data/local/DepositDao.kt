package uk.ac.tees.mad.savesmart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {

    // Insert deposit
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity)

    // Get all deposits for a goal
    @Query("SELECT * FROM deposits_cache WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun getDepositsForGoal(goalId: String): Flow<List<DepositEntity>>

    // Get unsynced deposits (for Firebase sync)
    @Query("SELECT * FROM deposits_cache WHERE isSynced = 0")
    suspend fun getUnsyncedDeposits(): List<DepositEntity>

    // Mark deposit as synced
    @Query("UPDATE deposits_cache SET isSynced = 1 WHERE id = :depositId")
    suspend fun markAsSynced(depositId: String)

    // Delete all deposits for user (logout)
    @Query("DELETE FROM deposits_cache WHERE userId = :userId")
    suspend fun deleteAllDeposits(userId: String)
}