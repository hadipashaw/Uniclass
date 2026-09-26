package com.example.util

import com.example.data.DayOfWeekPersian
import com.example.data.model.CourseClass
import java.util.Calendar

enum class ClassStatus {
    UPCOMING,
    IN_PROGRESS,
    FINISHED
}

data class NextClassState(
    val course: CourseClass? = null,
    val isOngoing: Boolean = false,
    val remainingDays: Long = 0,
    val remainingHours: Long = 0,
    val remainingMinutes: Long = 0,
    val formattedCountdown: String = "",
    val nextOccurrenceMillis: Long = 0
)

object ScheduleTimeHelper {

    fun determineClassStatus(course: CourseClass, forSelectedDay: DayOfWeekPersian): ClassStatus {
        val today = DayOfWeekPersian.today()
        if (forSelectedDay != today) {
            return ClassStatus.UPCOMING
        }

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startMinutes = course.startHour * 60 + course.startMinute
        val endMinutes = course.endHour * 60 + course.endMinute

        return when {
            currentMinutes < startMinutes -> ClassStatus.UPCOMING
            currentMinutes in startMinutes..endMinutes -> ClassStatus.IN_PROGRESS
            else -> ClassStatus.FINISHED
        }
    }

    fun findNextClass(courses: List<CourseClass>): NextClassState {
        val activeCourses = courses.filter { it.isActiveThisWeek }
        if (activeCourses.isEmpty()) return NextClassState()

        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis
        val currentMinutesToday = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val todayDay = DayOfWeekPersian.today()

        // 1. Check if there is an in-progress class right now today
        val ongoingCourse = activeCourses.firstOrNull { c ->
            c.dayOfWeek == todayDay.dayIndex &&
                    currentMinutesToday >= (c.startHour * 60 + c.startMinute) &&
                    currentMinutesToday < (c.endHour * 60 + c.endMinute)
        }

        if (ongoingCourse != null) {
            val endMinutes = ongoingCourse.endHour * 60 + ongoingCourse.endMinute
            val remainingMins = endMinutes - currentMinutesToday
            val hours = remainingMins / 60
            val mins = remainingMins % 60
            val countdownStr = if (hours > 0) "$hours ساعت و $mins دقیقه تا پایان" else "$mins دقیقه تا پایان کلاس"
            return NextClassState(
                course = ongoingCourse,
                isOngoing = true,
                remainingHours = hours.toLong(),
                remainingMinutes = mins.toLong(),
                formattedCountdown = countdownStr
            )
        }

        // 2. Find the earliest upcoming class in the future
        var bestCourse: CourseClass? = null
        var minDiffMillis = Long.MAX_VALUE

        for (course in activeCourses) {
            val classTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, course.startHour)
                set(Calendar.MINUTE, course.startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetDay = course.dayPersian.calendarDay
            val curDay = now.get(Calendar.DAY_OF_WEEK)

            var daysToAdd = targetDay - curDay
            if (daysToAdd < 0) {
                daysToAdd += 7
            }

            classTime.add(Calendar.DAY_OF_YEAR, daysToAdd)

            // If it's today and start time has passed, next occurrence is next week
            if (classTime.timeInMillis <= nowMillis) {
                classTime.add(Calendar.DAY_OF_YEAR, 7)
            }

            val diff = classTime.timeInMillis - nowMillis
            if (diff in 1..<minDiffMillis) {
                minDiffMillis = diff
                bestCourse = course
            }
        }

        if (bestCourse == null) return NextClassState()

        val totalMinutes = minDiffMillis / (1000 * 60)
        val days = totalMinutes / (24 * 60)
        val remainingHours = (totalMinutes % (24 * 60)) / 60
        val remainingMins = totalMinutes % 60

        val countdownStr = buildString {
            if (days > 0) {
                append("$days روز و ")
            }
            if (remainingHours > 0 || days > 0) {
                append("$remainingHours ساعت و ")
            }
            append("$remainingMins دقیقه دیگر")
        }

        return NextClassState(
            course = bestCourse,
            isOngoing = false,
            remainingDays = days,
            remainingHours = remainingHours,
            remainingMinutes = remainingMins,
            formattedCountdown = countdownStr,
            nextOccurrenceMillis = nowMillis + minDiffMillis
        )
    }
}
