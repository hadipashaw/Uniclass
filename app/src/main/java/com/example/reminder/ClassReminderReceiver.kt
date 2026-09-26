package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.UniClassApp
import com.example.data.model.CourseClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_BOOT_COMPLETED -> {
                // Device rebooted; reschedule all active alarms
                val app = context.applicationContext as? UniClassApp ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val courses = app.database.courseClassDao().getAllCoursesSync()
                        ClassAlarmScheduler.rescheduleAll(context, courses)
                    } catch (_: Exception) {
                        // DB error or empty
                    }
                }
            }

            ACTION_CLASS_REMINDER -> {
                val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L)
                val courseName = intent.getStringExtra(EXTRA_COURSE_NAME) ?: "کلاس دانشگاه"
                val location = intent.getStringExtra(EXTRA_LOCATION) ?: ""
                val instructor = intent.getStringExtra(EXTRA_INSTRUCTOR) ?: ""
                val startHour = intent.getIntExtra(EXTRA_START_HOUR, 8)
                val startMin = intent.getIntExtra(EXTRA_START_MIN, 0)
                val remindMinutes = intent.getIntExtra(EXTRA_REMIND_MINUTES, 15)
                val dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, 1)

                // Show the notification
                NotificationHelper.showClassReminder(
                    context = context,
                    courseId = courseId,
                    courseName = courseName,
                    location = location,
                    instructor = instructor,
                    startHour = startHour,
                    startMinute = startMin,
                    remindMinutes = remindMinutes
                )

                // Automatically schedule the next occurrence for next week!
                if (courseId > 0) {
                    val dummyCourse = CourseClass(
                        id = courseId,
                        courseName = courseName,
                        instructor = instructor,
                        dayOfWeek = dayOfWeek,
                        startHour = startHour,
                        startMinute = startMin,
                        endHour = startHour + 2, // approximation for next alarm calculation
                        endMinute = startMin,
                        location = location,
                        remindMinutesBefore = remindMinutes,
                        isReminderEnabled = true
                    )
                    ClassAlarmScheduler.scheduleReminder(context, dummyCourse)
                }
            }
        }
    }

    companion object {
        const val ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED
        const val ACTION_CLASS_REMINDER = "com.aistudio.uniclass.ACTION_CLASS_REMINDER"

        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_COURSE_NAME = "extra_course_name"
        const val EXTRA_LOCATION = "extra_location"
        const val EXTRA_INSTRUCTOR = "extra_instructor"
        const val EXTRA_START_HOUR = "extra_start_hour"
        const val EXTRA_START_MIN = "extra_start_min"
        const val EXTRA_REMIND_MINUTES = "extra_remind_minutes"
        const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
    }
}
