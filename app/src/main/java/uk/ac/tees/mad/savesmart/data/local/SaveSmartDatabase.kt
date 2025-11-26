package uk.ac.tees.mad.savesmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import uk.ac.tees.mad.savesmart.data.model.Goal

@Database(
    entities = [GoalEntity::class, DepositEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SaveSmartDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun depositDao(): DepositDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Timestamp {
        return Timestamp(value / 1000, ((value % 1000) * 1000000).toInt())
    }

    @TypeConverter
    fun timestampToLong(timestamp: Timestamp): Long {
        return timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
    }
}

fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id  = this.id,
        userId = this.userId,
        title = this.title,
        targetAmount = this.targetAmount,
        currentAmount = this.currentAmount,
        currency = this.currency,
        createdAt = this.createdAt.seconds * 1000,
        updatedAt = this.updatedAt.seconds * 1000,
        isSynced = true
    )
}

fun GoalEntity.toGoal(): Goal {
    return Goal(
        id = this.id,
        userId = this.userId,
        title = this.title,
        targetAmount = this.targetAmount,
        currentAmount = this.currentAmount,
        currency = this.currency,
        createdAt = Timestamp(this.createdAt / 1000, ((this.createdAt % 1000) * 1000000).toInt()),
        updatedAt = Timestamp(this.updatedAt / 1000, ((this.updatedAt % 1000) * 1000000).toInt())
    )
}