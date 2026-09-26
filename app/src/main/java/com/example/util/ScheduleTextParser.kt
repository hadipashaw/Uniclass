package com.example.util

import com.example.data.DayOfWeekPersian
import com.example.data.model.CourseClass
import com.example.data.model.UserRole
import java.util.regex.Pattern

object ScheduleTextParser {

    private val COURSE_COLORS = listOf(
        "#2563EB", // Blue
        "#059669", // Green
        "#D97706", // Amber
        "#7C3AED", // Violet
        "#E11D48", // Rose
        "#0D9488", // Teal
        "#EA580C", // Orange
        "#4F46E5"  // Indigo
    )

    fun normalizePersianNumbers(text: String): String {
        return text
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3')
            .replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7')
            .replace('۸', '8').replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3')
            .replace('٤', '4').replace('٥', '5').replace('٦', '6').replace('٧', '7')
            .replace('٨', '8').replace('٩', '9')
            .replace('ي', 'ی').replace('ك', 'ک')
    }

    fun parseScheduleText(rawInput: String, role: UserRole = UserRole.STUDENT): List<CourseClass> {
        val results = mutableListOf<CourseClass>()
        val normalized = normalizePersianNumbers(rawInput)
        val lines = normalized.split("\n", ";").map { it.trim() }.filter { it.isNotBlank() }

        var colorIndex = 0

        for (line in lines) {
            val daysFound = extractDays(line)
            if (daysFound.isEmpty()) continue

            val timeRange = extractTimeRange(line)
            val instructor = extractInstructor(line)
            val (location, building, floor, room) = extractLocationDetails(line)
            val classRep = extractClassRep(line)
            val topic = extractTopic(line)
            val courseName = extractCourseName(line, daysFound, instructor, location)

            val color = COURSE_COLORS[colorIndex % COURSE_COLORS.size]
            colorIndex++

            for (day in daysFound) {
                results.add(
                    CourseClass(
                        id = 0,
                        courseName = courseName.ifBlank { if (role == UserRole.PROFESSOR) "کلاس تدریس" else "درس دانشگاهی" },
                        instructor = instructor,
                        dayOfWeek = day.dayIndex,
                        startHour = timeRange.first.first,
                        startMinute = timeRange.first.second,
                        endHour = timeRange.second.first,
                        endMinute = timeRange.second.second,
                        location = location,
                        colorHex = color,
                        remindMinutesBefore = if (role == UserRole.PROFESSOR) 20 else 15,
                        isReminderEnabled = true,
                        roleType = role.id,
                        building = building,
                        floor = floor,
                        roomNumber = room,
                        classRepContact = classRep,
                        topicToday = topic
                    )
                )
            }
        }

        return results
    }

    private fun extractDays(line: String): List<DayOfWeekPersian> {
        val days = mutableListOf<DayOfWeekPersian>()
        val dayKeywords = listOf(
            Pair("یکشنبه", DayOfWeekPersian.SUNDAY),
            Pair("یک شنبه", DayOfWeekPersian.SUNDAY),
            Pair("دوشنبه", DayOfWeekPersian.MONDAY),
            Pair("دو شنبه", DayOfWeekPersian.MONDAY),
            Pair("سه شنبه", DayOfWeekPersian.TUESDAY),
            Pair("سه‌شنبه", DayOfWeekPersian.TUESDAY),
            Pair("چهارشنبه", DayOfWeekPersian.WEDNESDAY),
            Pair("چهار شنبه", DayOfWeekPersian.WEDNESDAY),
            Pair("پنج شنبه", DayOfWeekPersian.THURSDAY),
            Pair("پنج‌شنبه", DayOfWeekPersian.THURSDAY),
            Pair("جمعه", DayOfWeekPersian.FRIDAY),
            Pair("شنبه", DayOfWeekPersian.SATURDAY)
        )

        var processed = line
        for ((keyword, dayEnum) in dayKeywords) {
            if (processed.contains(keyword) && !days.contains(dayEnum)) {
                days.add(dayEnum)
                processed = processed.replace(keyword, " ")
            }
        }
        return days
    }

    private fun extractTimeRange(line: String): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val fullTimeRegex = Pattern.compile("(\\d{1,2})[:：](\\d{2})\\s*(?:تا|الی|-|to)\\s*(\\d{1,2})[:：](\\d{2})")
        val match1 = fullTimeRegex.matcher(line)
        if (match1.find()) {
            val sh = match1.group(1)?.toIntOrNull() ?: 8
            val sm = match1.group(2)?.toIntOrNull() ?: 0
            val eh = match1.group(3)?.toIntOrNull() ?: (sh + 2)
            val em = match1.group(4)?.toIntOrNull() ?: 0
            return Pair(Pair(sh.coerceIn(0, 23), sm.coerceIn(0, 59)), Pair(eh.coerceIn(0, 23), em.coerceIn(0, 59)))
        }

        val shortTimeRegex = Pattern.compile("(\\d{1,2})\\s*(?:تا|الی|-)\\s*(\\d{1,2})")
        val match2 = shortTimeRegex.matcher(line)
        if (match2.find()) {
            val sh = match2.group(1)?.toIntOrNull() ?: 8
            val eh = match2.group(2)?.toIntOrNull() ?: (sh + 2)
            return Pair(Pair(sh.coerceIn(0, 23), 0), Pair(eh.coerceIn(0, 23), 0))
        }

        return Pair(Pair(8, 0), Pair(10, 0))
    }

    private fun extractInstructor(line: String): String {
        val instructorRegex = Pattern.compile("(?:دکتر|استاد|مهندس)\\s+([\\u0600-\\u06FF]+(?:\\s+[\\u0600-\\u06FF]+)?)")
        val match = instructorRegex.matcher(line)
        if (match.find()) {
            return match.group(0)?.trim() ?: ""
        }
        return ""
    }

    private fun extractLocationDetails(line: String): LocationData {
        var building = ""
        var floor = ""
        var room = ""

        // Building / Faculty
        val bMatch = Pattern.compile("(?:دانشکده|ساختمان)\\s*([\\u0600-\\u06FF0-9\\-]+(?:\\s+[\\u0600-\\u06FF0-9\\-]+)?)").matcher(line)
        if (bMatch.find()) {
            building = bMatch.group(0)?.trim() ?: ""
        }

        // Floor
        val fMatch = Pattern.compile("طبقه\\s*([\\u0600-\\u06FF0-9\\-]+)").matcher(line)
        if (fMatch.find()) {
            floor = fMatch.group(1)?.trim() ?: ""
        }

        // Room
        val rMatch = Pattern.compile("(?:کلاس|سایت|آزمایشگاه|تالار|اتاق)\\s*([\\u0600-\\u06FF0-9\\-]+)").matcher(line)
        if (rMatch.find()) {
            room = rMatch.group(1)?.trim() ?: ""
        }

        val full = listOf(building, if (floor.isNotBlank()) "طبقه $floor" else "", if (room.isNotBlank()) "کلاس $room" else "")
            .filter { it.isNotBlank() }
            .joinToString("، ")

        return LocationData(
            location = full.ifBlank {
                val locRegex = Pattern.compile("(?:کلاس|سایت|دانشکده|آزمایشگاه|تالار|اتاق|آمفی تئاتر)\\s*([\\u0600-\\u06FF0-9\\-]+(?:\\s+[\\u0600-\\u06FF0-9\\-]+)?)")
                val m = locRegex.matcher(line)
                if (m.find()) m.group(0)?.trim() ?: "" else ""
            },
            building = building,
            floor = floor,
            room = room
        )
    }

    data class LocationData(
        val location: String,
        val building: String,
        val floor: String,
        val room: String
    )

    private fun extractClassRep(line: String): String {
        val repRegex = Pattern.compile("(?:نماینده|رابط)\\s*[:：]?\\s*([\\u0600-\\u06FF0-9\\-\\s]+)")
        val match = repRegex.matcher(line)
        if (match.find()) {
            return match.group(1)?.trim() ?: ""
        }
        return ""
    }

    private fun extractTopic(line: String): String {
        val topicRegex = Pattern.compile("(?:سرفصل|مبحث|موضوع)\\s*[:：]?\\s*([\\u0600-\\u06FF0-9\\-\\s]+)")
        val match = topicRegex.matcher(line)
        if (match.find()) {
            return match.group(1)?.trim() ?: ""
        }
        return ""
    }

    private fun extractCourseName(
        line: String,
        days: List<DayOfWeekPersian>,
        instructor: String,
        location: String
    ): String {
        var clean = line
        if (instructor.isNotBlank()) clean = clean.replace(instructor, "")
        if (location.isNotBlank()) clean = clean.replace(location, "")

        val dayWords = listOf(
            "یکشنبه", "یک شنبه", "دوشنبه", "دو شنبه", "سه‌شنبه", "سه شنبه",
            "چهارشنبه", "چهار شنبه", "پنج‌شنبه", "پنج شنبه", "جمعه", "شنبه", "روزهای", "روز"
        )
        for (w in dayWords) clean = clean.replace(w, "")

        clean = clean
            .replace(Regex("\\d{1,2}[:：]\\d{2}\\s*(?:تا|الی|-|to)\\s*\\d{1,2}[:：]\\d{2}"), "")
            .replace(Regex("\\d{1,2}\\s*(?:تا|الی|-)\\s*\\d{1,2}"), "")
            .replace("ساعت", "")
            .replace("الی", "")
            .replace("تا", "")
            .replace("-", "")
            .replace("|", "")
            .replace(":", "")
            .replace("،", "")
            .replace(",", "")
            .replace("درس", "")
            .replace("نام", "")
            .replace("استاد", "")
            .replace("محل", "")
            .replace("تشکیل", "")
            .replace("طبقه", "")
            .replace("کلاس", "")
            .replace("دانشکده", "")
            .replace("ساختمان", "")
            .replace("و", " ")
            .trim()

        clean = clean.replace(Regex("\\s+"), " ").trim()
        return if (clean.length in 2..50) clean else "درس تخصصی"
    }

    val STUDENT_PRESETS = listOf(
        SamplePreset(
            title = "ترم مهندسی کامپیوتر",
            description = "ریاضی ۱، برنامه‌نویسی، فیزیک، مدارهای منطقی",
            sampleText = """
                ریاضی عمومی ۱ - شنبه و دوشنبه ۰۸:۰۰ تا ۱۰:۰۰ - ساختمان خوارزمی، طبقه ۳، کلاس ۳۰۱ - دکتر اکبری
                برنامه‌نویسی پیشرفته - یکشنبه و سه‌شنبه ۱۰:۰۰ تا ۱۲:۰۰ - دانشکده کامپیوتر، طبقه ۱، سایت ۲ - استاد میرزایی
                فیزیک دانشگاهی ۱ - شنبه و دوشنبه ۱۳:۳۰ تا ۱۵:۳۰ - دانشکده علوم، طبقه ۲، تالار ۳ - دکتر کریمی
                مدارهای منطقی - چهارشنبه ۰۸:۰۰ تا ۱۱:۰۰ - دانشکده برق، طبقه ۲، کلاس ۲۰۴ - مهندس حسینی
            """.trimIndent(),
            role = UserRole.STUDENT
        ),
        SamplePreset(
            title = "علوم پایه و پزشکی",
            description = "شیمی عمومی، بیوشیمی، فیزیولوژی، آناتومی",
            sampleText = """
                بیوشیمی پایه - شنبه ۰۸:۰۰ تا ۱۰:۰۰ - دانشکده پزشکی، طبقه همکف، تالار رازی - دکتر شمس
                فیزیولوژی ۱ - یکشنبه و سه‌شنبه ۱۰:۰۰ تا ۱۲:۰۰ - ساختمان ابن‌سینا، طبقه ۲، کلاس ۱۲ - دکتر طباطبایی
                آناتومی اندام‌ها - چهارشنبه ۰۹:۰۰ تا ۱۲:۰۰ - دانشکده پزشکی، طبقه منفی ۱، سالن تشریح - دکتر رادمنش
            """.trimIndent(),
            role = UserRole.STUDENT
        )
    )

    val PROFESSOR_PRESETS = listOf(
        SamplePreset(
            title = "برنامه تدریس اساتید مهندسی",
            description = "ریاضی مهندسی، معادلات دیفرانسیل، محاسبات عددی",
            sampleText = """
                ریاضیات مهندسی پیشرفته - شنبه و دوشنبه ۰۸:۰۰ تا ۱۰:۰۰ - دانشکده فنی، طبقه ۳، کلاس ۳۰۴ - نماینده: رضایی ۰۹۱۲۳۴۵۶۷۸۹
                معادلات دیفرانسیل - یکشنبه و سه‌شنبه ۱۰:۰۰ تا ۱۲:۰۰ - ساختمان ابوریحان، طبقه ۲، کلاس ۲۰۱ - نماینده: کمالی
                محاسبات عددی - دوشنبه ۱۳:۳۰ تا ۱۵:۳۰ - دانشکده علوم، طبقه ۱، سایت کامپیوتر - نماینده: حسینی
                جلسه گروه آموزشی و رفع اشکال - چهارشنبه ۱۰:۰۰ تا ۱۲:۰۰ - ساختمان اساتید، طبقه ۴، اتاق ۴۰۵
            """.trimIndent(),
            role = UserRole.PROFESSOR
        ),
        SamplePreset(
            title = "برنامه تدریس اساتید پزشکی",
            description = "فیزیولوژی، بافت‌شناسی و کارآموزی بالینی",
            sampleText = """
                فیزیولوژی اعصاب - شنبه ۰۸:۳۰ تا ۱۰:۳۰ - بیمارستان امام، طبقه ۴، آمفی تئاتر - نماینده: رستمی
                بافت‌شناسی عمومی - یکشنبه و سه‌شنبه ۱۰:۰۰ تا ۱۲:۰۰ - دانشکده پزشکی، طبقه ۲، آزمایشگاه ۱ - نماینده: مهدوی
                راندهای آموزشی و ژورنال کلاب - چهارشنبه ۰۸:۰۰ تا ۱۰:۰۰ - ساختمان ابن‌سینا، طبقه ۱، تالار کنفرانس
            """.trimIndent(),
            role = UserRole.PROFESSOR
        )
    )

    data class SamplePreset(
        val title: String,
        val description: String,
        val sampleText: String,
        val role: UserRole
    )
}
