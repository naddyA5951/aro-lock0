package com.example

import android.app.Application
import com.example.data.TrekDatabase
import com.example.data.TrekRepository
import com.example.data.UserPreferences

class ArolockApp : Application() {

    lateinit var database: TrekDatabase
        private set

    lateinit var repository: TrekRepository
        private set

    lateinit var userPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = TrekDatabase.getDatabase(this)
        userPreferences = UserPreferences(this)
        repository = TrekRepository(database.trekDao(), userPreferences)
    }

    companion object {
        lateinit var instance: ArolockApp
            private set
    }
}
