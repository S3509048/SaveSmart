package uk.ac.tees.mad.savesmart.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import uk.ac.tees.mad.savesmart.workers.WeeklyReminderWorker
import java.util.concurrent.TimeUnit

object NT {
    fun testNotificationNow(context: Context) {
        NotificationHelper.sendWeeklyReminder(context)
    }
}