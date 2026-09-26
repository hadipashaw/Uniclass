package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.DayOfWeekPersian
import java.util.Locale

@Entity(tableName = "courses")
data class CourseClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseName: String,
    val instructor: String = "",
    val dayOfWeek: Int, // 1 to 7 (DayOfWeekPersian.dayIndex)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val location: String = "",
    val colorHex: String = "#2563EB",
    val remindMinutesBefore: Int = 15,
    val isReminderEnabled: Boolean = true,
    val notes: String = "",
    val isActiveThisWeek: Boolean = true,
    // Role-specific fields
    val roleType: String = UserRole.STUDENT.id, // "STUDENT" or "PROFESSOR"
    val building: String = "", // ساختمان یا دانشکده
    val roomNumber: String = "", // شماره کلاس / آزمایشگاه
    val floor: String = "", // طبقه
    val classRepContact: String = "", // نماینده کلاس
    val topicToday: String = "" // مبحث تدریس یا سرفصل
) {
    val dayPersian: DayOfWeekPersian
        get() = DayOfWeekPersian.fromDayIndex(dayOfWeek)

    val formattedStartTime: String
        get() = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)

    val formattedEndTime: String
        get() = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)

    val formattedTimeRange: String
        get() = "$formattedStartTime - $formattedEndTime"

    val displayLocation: String
        get() = buildString {
            if (building.isNotBlank()) append(building)
            if (floor.isNotBlank()) {
                if (isNotEmpty()) append("، ")
                append("طبقه ").append(floor)
            }
            if (roomNumber.isNotBlank()) {
                if (isNotEmpty()) append("، ")
                append("کلاس ").append(roomNumber)
            }
            if (isEmpty()) {
                append(location.ifBlank { "محل تعیین‌نشده" })
            }
        }
}
