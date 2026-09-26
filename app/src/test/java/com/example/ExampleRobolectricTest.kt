package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DayOfWeekPersian
import com.example.data.model.UserRole
import com.example.util.ScheduleTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UniClass", appName)
    }

    @Test
    fun `test schedule text parser`() {
        val sample = "ریاضی عمومی ۱ - شنبه و دوشنبه ۰۸:۰۰ تا ۱۰:۰۰ - کلاس ۳۰۲ - دکتر رضایی"
        val parsed = ScheduleTextParser.parseScheduleText(sample, UserRole.STUDENT)

        assertEquals(2, parsed.size) // Saturday and Monday
        val satClass = parsed.first { it.dayOfWeek == DayOfWeekPersian.SATURDAY.dayIndex }
        assertEquals(8, satClass.startHour)
        assertEquals(0, satClass.startMinute)
        assertEquals(10, satClass.endHour)
        assertTrue(satClass.courseName.contains("ریاضی"))
    }

    @Test
    fun `test professor schedule text parser with building and room`() {
        val sample = "ریاضیات مهندسی پیشرفته - شنبه ۰۸:۰۰ تا ۱۰:۰۰ - دانشکده فنی، طبقه ۳، کلاس ۳۰۴ - نماینده: رضایی ۰۹۱۲۳۴۵۶۷۸۹"
        val parsed = ScheduleTextParser.parseScheduleText(sample, UserRole.PROFESSOR)

        assertEquals(1, parsed.size)
        val course = parsed[0]
        assertEquals(UserRole.PROFESSOR.id, course.roleType)
        assertTrue(course.displayLocation.contains("دانشکده فنی"))
        assertTrue(course.displayLocation.contains("304"))
        assertTrue(course.classRepContact.contains("رضایی"))
    }
}
