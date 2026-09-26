package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_ID = "uni_class_reminders_channel"
    private const val CHANNEL_NAME = "یادآور کلاس‌های دانشگاه"
    private const val CHANNEL_DESC = "اعلان و هشدار قبل از شروع کلاس‌های دانشگاهی"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showClassReminder(
        context: Context,
        courseId: Long,
        courseName: String,
        location: String,
        instructor: String,
        startHour: Int,
        startMinute: Int,
        remindMinutes: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            courseId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)
        val locationText = if (location.isNotBlank()) " | محل: $location" else ""
        val instructorText = if (instructor.isNotBlank()) " | استاد: $instructor" else ""

        val contentText = if (remindMinutes > 0) {
            "کلاس تا $remindMinutes دقیقه دیگر آغاز می‌شود (ساعت $formattedTime)$locationText"
        } else {
            "کلاس هم‌اکنون آغاز شد (ساعت $formattedTime)$locationText"
        }

        val bigText = buildString {
            append("نام درس: $courseName\n")
            append("ساعت شروع: $formattedTime\n")
            if (location.isNotBlank()) append("محل تشکیل: $location\n")
            if (instructor.isNotBlank()) append("مدرس: $instructor\n")
            append("آماده حضور در کلاس باشید!")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🔔 یادآور کلاس: $courseName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(courseId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission might be revoked by user
        }
    }

    fun showTestNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            99999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ آزمایش اعلان یادآور کلاس")
            .setContentText("سیستم اعلان فعال است! کلاس‌های شما به موقع یادآوری خواهند شد.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "تبریک! سیستم یادآوری هوشمند کلاس با موفقیت تست شد. " +
                            "قبل از شروع هر کلاس بر اساس دقیقه تنظیمی به شما اطلاع داده خواهد شد."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(99999, notification)
        } catch (_: SecurityException) {
            // Permission might be revoked
        }
    }
}
