package com.example.data

import java.util.Calendar

enum class DayOfWeekPersian(
    val dayIndex: Int, // 1 to 7 (Saturday to Friday)
    val faName: String,
    val shortName: String,
    val calendarDay: Int
) {
    SATURDAY(1, "شنبه", "ش", Calendar.SATURDAY),
    SUNDAY(2, "یکشنبه", "ی", Calendar.SUNDAY),
    MONDAY(3, "دوشنبه", "د", Calendar.MONDAY),
    TUESDAY(4, "سه‌شنبه", "س", Calendar.TUESDAY),
    WEDNESDAY(5, "چهارشنبه", "چ", Calendar.WEDNESDAY),
    THURSDAY(6, "پنج‌شنبه", "پ", Calendar.THURSDAY),
    FRIDAY(7, "جمعه", "ج", Calendar.FRIDAY);

    companion object {
        fun fromDayIndex(index: Int): DayOfWeekPersian {
            return entries.firstOrNull { it.dayIndex == index } ?: SATURDAY
        }

        fun fromCalendar(calendar: Calendar): DayOfWeekPersian {
            val calDay = calendar.get(Calendar.DAY_OF_WEEK)
            return entries.firstOrNull { it.calendarDay == calDay } ?: SATURDAY
        }

        fun today(): DayOfWeekPersian {
            return fromCalendar(Calendar.getInstance())
        }

        fun parseFromPersianName(name: String): DayOfWeekPersian? {
            val normalized = name.replace("ي", "ی").replace("ك", "ک").trim()
            return entries.firstOrNull {
                normalized.contains(it.faName) ||
                        (it == SATURDAY && normalized == "شنبه") ||
                        (it == SUNDAY && (normalized == "یکشنبه" || normalized == "یک شنبه")) ||
                        (it == MONDAY && (normalized == "دوشنبه" || normalized == "دو شنبه")) ||
                        (it == TUESDAY && (normalized == "سه‌شنبه" || normalized == "سه شنبه")) ||
                        (it == WEDNESDAY && (normalized == "چهارشنبه" || normalized == "چهار شنبه")) ||
                        (it == THURSDAY && (normalized == "پنج‌شنبه" || normalized == "پنج شنبه")) ||
                        (it == FRIDAY && normalized == "جمعه")
            }
        }
    }
}
