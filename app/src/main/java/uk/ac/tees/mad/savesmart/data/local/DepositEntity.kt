package uk.ac.tees.mad.savesmart.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import uk.ac.tees.mad.savesmart.data.model.Deposit

@Entity(tableName = "deposits_cache")
data class DepositEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val userId: String,
    val amount: Double,
    val note: String,
    val createdAt: Long,
    val isSynced: Boolean = false  // ✅ Track sync status
)

// ✅ Conversion functions
fun Deposit.toEntity(): DepositEntity {
    return DepositEntity(
        id = this.id,
        goalId = this.goalId,
        userId = this.userId,
        amount = this.amount,
        note = this.note,
        createdAt = this.createdAt.seconds * 1000,
        isSynced = this.isSynced
    )
}

fun DepositEntity.toDeposit(): Deposit {
    return Deposit(
        id = this.id,
        goalId = this.goalId,
        userId = this.userId,
        amount = this.amount,
        note = this.note,
        createdAt = Timestamp(this.createdAt / 1000, ((this.createdAt % 1000) * 1000000).toInt()),
        isSynced = this.isSynced
    )
}