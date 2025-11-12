package com.example.habittracker.ui.hydration

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.habittracker.R

class HydrationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "hydration_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ Create notification channel if Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // ✅ Show notification safely
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_hydration)
            .setContentTitle("Time to Drink Water 💧")
            .setContentText("Stay hydrated! Drink a glass of water now.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.e("HydrationReceiver", "Missing POST_NOTIFICATIONS permission", e)
        }

        // 🔁 Reschedule itself
        val interval = intent?.getLongExtra("interval", 60 * 1000L) ?: 60 * 1000L
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(context, HydrationReceiver::class.java).apply {
            putExtra("interval", interval)
        }
        val newPending = PendingIntent.getBroadcast(
            context, 0, newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Check exact alarm permission before scheduling
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + interval,
                newPending
            )
        } else {
            Log.w("HydrationReceiver", "Exact alarm not allowed, skipping reschedule")
        }
    }
}
