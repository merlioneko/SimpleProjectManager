package com.aqualion.vani.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [ProjectEntity::class, NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}

class AppDatabaseHelper(private val applicationContext: Context) {
    private var database: AppDatabase? = null
    fun getDatabase(): AppDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
        }
        return database!!
    }
    fun getDatabaseInMemory(): AppDatabase {
        if (database == null) {
            database = Room.inMemoryDatabaseBuilder(
                applicationContext,
                AppDatabase::class.java
            ).allowMainThreadQueries().build()
        }
        return database!!
    }
}