package uk.ac.tees.mad.savesmart.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import uk.ac.tees.mad.savesmart.data.local.UserPreferencesManager
import uk.ac.tees.mad.savesmart.utils.NotificationHelper

@HiltWorker
class WeeklyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val preferencesManager: UserPreferencesManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Check if notifications are enabled
            val notificationsEnabled = preferencesManager.notificationsEnabledFlow.first()

            if (notificationsEnabled) {
                // Send weekly reminder
                NotificationHelper.sendWeeklyReminder(context)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "weekly_reminder_work"
    }
}