package uk.ac.tees.mad.savesmart.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import uk.ac.tees.mad.savesmart.R
import uk.ac.tees.mad.savesmart.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "savesmart_channel"
    private const val CHANNEL_NAME = "SaveSmart Reminders"
    private const val WEEKLY_NOTIFICATION_ID = 1001
    private const val MILESTONE_NOTIFICATION_ID = 1002

    // Create notification channel (call this in Application class)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for savings reminders and milestones"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Send weekly reminder notification
    fun sendWeeklyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle("Time to Save! 💰")
            .setContentText("Add this week's deposit to reach your financial goals")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(WEEKLY_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // User denied notification permission
        }
    }

    // Send milestone notification (25%, 50%, 75%, 100%)
    fun sendMilestoneNotification(
        context: Context,
        goalTitle: String,
        percentage: Int,
        currentAmount: Double,
        targetAmount: Double,
        currency: String
    ) {
        val symbol = when (currency) {
            "GBP" -> "£"
            "USD" -> "$"
            "EUR" -> "€"
            "INR" -> "₹"
            else -> currency
        }

        val emoji = when (percentage) {
            25 -> "🎯"
            50 -> "🔥"
            75 -> "⭐"
            100 -> "🎉"
            else -> "💪"
        }

        val title = when (percentage) {
            100 -> "Goal Achieved! $emoji"
            else -> "Milestone Reached! $emoji"
        }

        val message = when (percentage) {
            100 -> "Congratulations! You've completed '$goalTitle' - $symbol${String.format("%.2f", targetAmount)}"
            else -> "You're $percentage% there! '$goalTitle' - $symbol${String.format("%.2f", currentAmount)} of $symbol${String.format("%.2f", targetAmount)}"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                MILESTONE_NOTIFICATION_ID + percentage,
                notification
            )
        } catch (e: SecurityException) {
            // User denied notification permission
        }
    }
}