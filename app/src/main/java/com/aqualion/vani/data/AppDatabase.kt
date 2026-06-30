package com.aqualion.vani.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.migration.Migration

@Database(entities = [ProjectEntity::class, NoteEntity::class, ProjectRelationEntity::class], version = 2, exportSchema = false)
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
            ).addMigrations(migration1_2).build()
        }
        return database!!
    }

    val migration1_2 = object : Migration(1, 2) {
        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `project_relation` (
                    `parent_id` INTEGER NOT NULL, 
                    `child_id` INTEGER NOT NULL, 
                    PRIMARY KEY(`parent_id`, `child_id`), 
                    FOREIGN KEY(`parent_id`) REFERENCES `project`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`child_id`) REFERENCES `project`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_project_relation_parent_id` ON `project_relation` (`parent_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_project_relation_child_id` ON `project_relation` (`child_id`)")
        }
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