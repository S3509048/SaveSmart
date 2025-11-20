package uk.ac.tees.mad.savesmart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals_cache")
data class GoalEntity(
    @PrimaryKey
    val goalId: String,
    val userId: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true
)