package uk.ac.tees.mad.savesmart.utils

import android.content.Context
import uk.ac.tees.mad.savesmart.data.model.Goal

object MilestoneTracker {

    private val milestones = listOf(25, 50, 75, 100)


//     * Check if a milestone was reached and send notification
//     * Returns the milestone percentage if reached, null otherwise
    fun checkAndNotifyMilestone(
        context: Context,
        goal: Goal,
        oldAmount: Double,
        newAmount: Double
    ): Int? {
        val oldPercentage = (oldAmount / goal.targetAmount * 100).toInt()
        val newPercentage = (newAmount / goal.targetAmount * 100).toInt()

        // Find if any milestone was crossed
        val reachedMilestone = milestones.firstOrNull { milestone ->
            oldPercentage < milestone && newPercentage >= milestone
        }

        // Send notification if milestone reached
        reachedMilestone?.let { milestone ->
            NotificationHelper.sendMilestoneNotification(
                context = context,
                goalTitle = goal.title,
//                percentage = milestone,
                currentAmount = newAmount,
                targetAmount = goal.targetAmount,
                currency = goal.currency
            )
        }

        return reachedMilestone
    }

//     * Get current milestone percentage
    fun getCurrentMilestone(current: Double, target: Double): Int {
        val percentage = (current / target * 100).toInt()
        return when {
            percentage >= 100 -> 100
            percentage >= 75 -> 75
            percentage >= 50 -> 50
            percentage >= 25 -> 25
            else -> 0
        }
    }
}