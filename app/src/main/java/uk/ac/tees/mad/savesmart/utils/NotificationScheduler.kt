package uk.ac.tees.mad.savesmart.utils

import android.content.Context
import androidx.work.*
import uk.ac.tees.mad.savesmart.workers.WeeklyReminderWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar

object NotificationScheduler {

    /**
     * Schedule weekly reminder notification
     * Sends notification every Sunday at 10:00 AM
     */
    fun scheduleWeeklyReminder(context: Context) {
        // Calculate initial delay to Sunday 10:00 AM
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If target time is before current time, schedule for next week
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 7)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyReminderWorker>(
            7, TimeUnit.DAYS // Repeat every 7 days
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("weekly_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeeklyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
            weeklyRequest
        )
    }

    /**
     * Cancel weekly reminder
     */
    fun cancelWeeklyReminder(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WeeklyReminderWorker.WORK_NAME)
    }

    /**
     * Enable/Disable notifications
     */
    fun updateNotificationSchedule(context: Context, enabled: Boolean) {
        if (enabled) {
            scheduleWeeklyReminder(context)
        } else {
            cancelWeeklyReminder(context)
        }
    }
}