package uk.ac.tees.mad.savesmart.data.model


import com.google.firebase.Timestamp

data class Deposit(
    val id: String = "",
    val goalId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false // For offline support
) {
    // Convert to map for Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "goalId" to goalId,
            "userId" to userId,
            "amount" to amount,
            "note" to note,
            "createdAt" to createdAt,
            "isSynced" to isSynced
        )
    }

    companion object {
        // Create from Firebase map
        fun fromMap(map: Map<String, Any>): Deposit {
            return Deposit(
                id = map["id"] as? String ?: "",
                goalId = map["goalId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                note = map["note"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                isSynced = map["isSynced"] as? Boolean ?: false
            )
        }
    }
}