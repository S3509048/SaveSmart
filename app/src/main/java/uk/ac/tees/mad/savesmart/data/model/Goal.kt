package uk.ac.tees.mad.savesmart.data.model

import com.google.firebase.Timestamp

data class Goal(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val currency: String = "GBP", // Default to British Pound
    val deadline: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // Calculate progress percentage
    fun getProgressPercentage(): Float {
        return if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    // Calculate remaining amount
    fun getRemainingAmount(): Double {
        return (targetAmount - currentAmount).coerceAtLeast(0.0)
    }

    // Check if goal is completed
    fun isCompleted(): Boolean {
        return currentAmount >= targetAmount
    }

    // Convert to map for Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "targetAmount" to targetAmount,
            "currentAmount" to currentAmount,
            "currency" to currency,
            "deadline" to (deadline ?: ""),
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        // Create from Firebase map
        fun fromMap(map: Map<String, Any>): Goal {
            return Goal(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                targetAmount = (map["targetAmount"] as? Number)?.toDouble() ?: 0.0,
                currentAmount = (map["currentAmount"] as? Number)?.toDouble() ?: 0.0,
                currency = map["currency"] as? String ?: "GBP",
                deadline = map["deadline"] as? Timestamp,
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = map["updatedAt"] as? Timestamp ?: Timestamp.now()
            )
        }
    }
}