package com.example.data.repository

import com.example.data.dao.CourseClassDao
import com.example.data.model.CourseClass
import kotlinx.coroutines.flow.Flow

class CourseRepository(private val dao: CourseClassDao) {

    val allCourses: Flow<List<CourseClass>> = dao.getAllCourses()

    fun getCoursesByRole(role: String): Flow<List<CourseClass>> {
        return dao.getCoursesByRole(role)
    }

    fun getCoursesForDay(dayOfWeek: Int): Flow<List<CourseClass>> {
        return dao.getCoursesForDay(dayOfWeek)
    }

    suspend fun getCourseById(id: Long): CourseClass? {
        return dao.getCourseById(id)
    }

    suspend fun getAllCoursesSync(): List<CourseClass> {
        return dao.getAllCoursesSync()
    }

    suspend fun insertCourse(course: CourseClass): Long {
        return dao.insertCourse(course)
    }

    suspend fun insertCourses(courses: List<CourseClass>): List<Long> {
        return dao.insertCourses(courses)
    }

    suspend fun updateCourse(course: CourseClass) {
        dao.updateCourse(course)
    }

    suspend fun deleteCourse(id: Long) {
        dao.deleteCourse(id)
    }

    suspend fun deleteCoursesByRole(role: String) {
        dao.deleteCoursesByRole(role)
    }

    suspend fun deleteAllCourses() {
        dao.deleteAllCourses()
    }
}
