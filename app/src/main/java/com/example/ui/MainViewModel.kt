package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.UniClassApp
import com.example.data.DayOfWeekPersian
import com.example.data.model.CourseClass
import com.example.data.model.UserRole
import com.example.data.repository.CourseRepository
import com.example.reminder.ClassAlarmScheduler
import com.example.reminder.NotificationHelper
import com.example.util.NextClassState
import com.example.util.ScheduleTextParser
import com.example.util.ScheduleTimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CourseRepository =
        CourseRepository((application as UniClassApp).database.courseClassDao())

    // Active User Role: Student vs Professor
    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    val allCourses: StateFlow<List<CourseClass>> = repository.allCourses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val coursesForActiveRole: StateFlow<List<CourseClass>> = combine(allCourses, currentRole) { courses, role ->
        courses.filter { it.roleType.equals(role.id, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedDay = MutableStateFlow(DayOfWeekPersian.today())
    val selectedDay: StateFlow<DayOfWeekPersian> = _selectedDay.asStateFlow()

    val coursesForSelectedDay: StateFlow<List<CourseClass>> = combine(coursesForActiveRole, selectedDay) { courses, day ->
        courses.filter { it.dayOfWeek == day.dayIndex }
            .sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _nextClassState = MutableStateFlow(NextClassState())
    val nextClassState: StateFlow<NextClassState> = _nextClassState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        // Ticker to update next class countdown in real-time
        viewModelScope.launch {
            while (isActive) {
                _nextClassState.value = ScheduleTimeHelper.findNextClass(coursesForActiveRole.value)
                delay(30_000)
            }
        }

        // Update whenever courses or active role changes
        viewModelScope.launch {
            coursesForActiveRole.collect { courses ->
                _nextClassState.value = ScheduleTimeHelper.findNextClass(courses)
            }
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        _snackbarMessage.value = "به ${role.faTitle} تغییر یافت"
    }

    fun selectDay(day: DayOfWeekPersian) {
        _selectedDay.value = day
    }

    fun addCourse(course: CourseClass) {
        viewModelScope.launch {
            val courseWithRole = if (course.roleType.isBlank()) {
                course.copy(roleType = _currentRole.value.id)
            } else {
                course
            }
            val id = repository.insertCourse(courseWithRole)
            val insertedCourse = courseWithRole.copy(id = id)
            ClassAlarmScheduler.scheduleReminder(getApplication(), insertedCourse)
            _snackbarMessage.value = "کلاس «${course.courseName}» با موفقیت اضافه شد"
        }
    }

    fun updateCourse(course: CourseClass) {
        viewModelScope.launch {
            repository.updateCourse(course)
            if (course.isReminderEnabled && course.isActiveThisWeek) {
                ClassAlarmScheduler.scheduleReminder(getApplication(), course)
            } else {
                ClassAlarmScheduler.cancelReminder(getApplication(), course.id)
            }
            _snackbarMessage.value = "اطلاعات کلاس بروزرسانی شد"
        }
    }

    fun deleteCourse(course: CourseClass) {
        viewModelScope.launch {
            repository.deleteCourse(course.id)
            ClassAlarmScheduler.cancelReminder(getApplication(), course.id)
            _snackbarMessage.value = "کلاس «${course.courseName}» حذف شد"
        }
    }

    fun toggleReminder(course: CourseClass, enabled: Boolean) {
        viewModelScope.launch {
            val updated = course.copy(isReminderEnabled = enabled)
            repository.updateCourse(updated)
            if (enabled) {
                ClassAlarmScheduler.scheduleReminder(getApplication(), updated)
                _snackbarMessage.value = "یادآور فعال شد (${course.remindMinutesBefore} دقیقه قبل)"
            } else {
                ClassAlarmScheduler.cancelReminder(getApplication(), course.id)
                _snackbarMessage.value = "یادآور این کلاس غیرفعال شد"
            }
        }
    }

    fun importParsedCourses(courses: List<CourseClass>) {
        viewModelScope.launch {
            if (courses.isEmpty()) return@launch
            val coursesWithRole = courses.map {
                if (it.roleType.isBlank()) it.copy(roleType = _currentRole.value.id) else it
            }
            val ids = repository.insertCourses(coursesWithRole)
            for (i in coursesWithRole.indices) {
                val c = coursesWithRole[i].copy(id = ids.getOrElse(i) { 0L })
                if (c.isReminderEnabled) {
                    ClassAlarmScheduler.scheduleReminder(getApplication(), c)
                }
            }
            _snackbarMessage.value = "${courses.size} کلاس به پنل ${_currentRole.value.faTitle} اضافه شد"
        }
    }

    fun loadSamplePreset(preset: ScheduleTextParser.SamplePreset) {
        _currentRole.value = preset.role
        val parsed = ScheduleTextParser.parseScheduleText(preset.sampleText, preset.role)
        importParsedCourses(parsed)
    }

    fun clearCoursesForCurrentRole() {
        viewModelScope.launch {
            val currentRoleCourses = coursesForActiveRole.value
            for (c in currentRoleCourses) {
                ClassAlarmScheduler.cancelReminder(getApplication(), c.id)
            }
            repository.deleteCoursesByRole(_currentRole.value.id)
            _snackbarMessage.value = "تمامی کلاس‌های پنل ${_currentRole.value.faTitle} پاک شدند"
        }
    }

    fun testNotification() {
        NotificationHelper.showTestNotification(getApplication())
        _snackbarMessage.value = "اعلان آزمایشی ارسال شد! بالای صفحه را بررسی کنید."
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}
