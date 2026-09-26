package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CourseClass
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseClassDao {

    @Query("SELECT * FROM courses ORDER BY dayOfWeek ASC, startHour ASC, startMinute ASC")
    fun getAllCourses(): Flow<List<CourseClass>>

    @Query("SELECT * FROM courses WHERE roleType = :role ORDER BY dayOfWeek ASC, startHour ASC, startMinute ASC")
    fun getCoursesByRole(role: String): Flow<List<CourseClass>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek ORDER BY startHour ASC, startMinute ASC")
    fun getCoursesForDay(dayOfWeek: Int): Flow<List<CourseClass>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: Long): CourseClass?

    @Query("SELECT * FROM courses ORDER BY dayOfWeek ASC, startHour ASC, startMinute ASC")
    suspend fun getAllCoursesSync(): List<CourseClass>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseClass>): List<Long>

    @Update
    suspend fun updateCourse(course: CourseClass)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: Long)

    @Query("DELETE FROM courses WHERE roleType = :role")
    suspend fun deleteCoursesByRole(role: String)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()
}
