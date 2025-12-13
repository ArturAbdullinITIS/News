package com.example.news.data.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.news.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager

) {

    init {
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.new_articles),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun showNewArticlesNotification(topics: List<String>) {
        val notificationBuilder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context).setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
        val notification = notificationBuilder
            .setSmallIcon(R.drawable.ic_breaking_news)
            .setContentTitle(context.getString(R.string.new_articles_notification_title))
            .setContentText(
                context.getString(
                    R.string.update_subscriptions,
                    topics.size,
                    topics.joinToString(", ")
                )
            )
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    companion object {
        private const val CHANNEL_ID = "new articles"
        private const val NOTIFICATION_ID = 1
    }
}