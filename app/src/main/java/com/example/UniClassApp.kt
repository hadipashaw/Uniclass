package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.reminder.NotificationHelper

class UniClassApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "uniclass_database"
        ).fallbackToDestructiveMigration().build()

        NotificationHelper.createNotificationChannel(this)
    }

    companion object {
        lateinit var instance: UniClassApp
            private set
    }
}
