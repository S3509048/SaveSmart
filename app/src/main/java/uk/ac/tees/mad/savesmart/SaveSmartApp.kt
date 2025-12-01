package uk.ac.tees.mad.savesmart

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import uk.ac.tees.mad.savesmart.utils.NotificationHelper
import uk.ac.tees.mad.savesmart.utils.NotificationScheduler
import javax.inject.Inject


@HiltAndroidApp
class SaveSmartApp: Application(), Configuration.Provider{


    @Inject
    lateinit var workerFactory: HiltWorkerFactory


    override fun onCreate() {
        super.onCreate()

        // 🔔 Setup notification channel
        NotificationHelper.createNotificationChannel(this)

        // 🔔 Schedule weekly reminder (will respect user preference)
        NotificationScheduler.scheduleWeeklyReminder(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}