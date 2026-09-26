package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.CourseClass
import java.util.Calendar

object ClassAlarmScheduler {

    fun scheduleReminder(context: Context, course: CourseClass) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // If reminder is disabled, cancel any existing alarm
        if (!course.isReminderEnabled || !course.isActiveThisWeek) {
            cancelReminder(context, course.id)
            return
        }

        val triggerMillis = calculateNextTriggerMillis(course)

        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
            putExtra(ClassReminderReceiver.EXTRA_COURSE_ID, course.id)
            putExtra(ClassReminderReceiver.EXTRA_COURSE_NAME, course.courseName)
            putExtra(ClassReminderReceiver.EXTRA_LOCATION, course.location)
            putExtra(ClassReminderReceiver.EXTRA_INSTRUCTOR, course.instructor)
            putExtra(ClassReminderReceiver.EXTRA_START_HOUR, course.startHour)
            putExtra(ClassReminderReceiver.EXTRA_START_MIN, course.startMinute)
            putExtra(ClassReminderReceiver.EXTRA_REMIND_MINUTES, course.remindMinutesBefore)
            putExtra(ClassReminderReceiver.EXTRA_DAY_OF_WEEK, course.dayOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } catch (_: Exception) {
                // Handle device-specific restriction
            }
        }
    }

    fun cancelReminder(context: Context, courseId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            courseId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAll(context: Context, courses: List<CourseClass>) {
        for (course in courses) {
            if (course.isReminderEnabled && course.isActiveThisWeek) {
                scheduleReminder(context, course)
            } else {
                cancelReminder(context, course.id)
            }
        }
    }

    fun calculateNextTriggerMillis(course: CourseClass): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, course.startHour)
            set(Calendar.MINUTE, course.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -course.remindMinutesBefore)
        }

        val targetCalendarDay = course.dayPersian.calendarDay
        val currentCalendarDay = now.get(Calendar.DAY_OF_WEEK)

        var daysToAdd = targetCalendarDay - currentCalendarDay
        if (daysToAdd < 0) {
            daysToAdd += 7
        }

        target.add(Calendar.DAY_OF_YEAR, daysToAdd)

        // If today and time has already passed, push to next week
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 7)
        }

        return target.timeInMillis
    }
}
