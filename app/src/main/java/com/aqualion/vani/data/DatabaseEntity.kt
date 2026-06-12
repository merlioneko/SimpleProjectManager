package com.aqualion.vani.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.aqualion.vani.domain.IoResult
import kotlinx.coroutines.flow.Flow

class DataBaseRule {
    companion object {
        const val AUTO_INCREMENT = 0
    }
}

@Entity(tableName="project")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = DataBaseRule.AUTO_INCREMENT,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name= "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: String,
    @ColumnInfo(name= "updated_at", defaultValue = "CURRENT_TIMESTAMP") val updatedAt: String
)

@Entity(tableName="note"
    ,foreignKeys = [
    ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["project_id"],
        onDelete = ForeignKey.CASCADE
    )
], indices = [androidx.room.Index("project_id")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = DataBaseRule.AUTO_INCREMENT,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "project_id") val projectId: Int,
    @ColumnInfo(name= "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: String,
    @ColumnInfo(name= "updated_at", defaultValue = "CURRENT_TIMESTAMP") val updatedAt: String
)

data class ProjectDetailEntity(
    @Embedded val projectEntity: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "project_id"
    ) val noteEntities: List<NoteEntity>
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM project")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM note")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(projectEntity: ProjectEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(noteEntity: NoteEntity): Long


    @Update
    suspend fun update(projectEntity: ProjectEntity): Int
    @Update
    suspend fun update(noteEntity: NoteEntity): Int


    @Transaction
    @Query("SELECT * FROM project WHERE id = :id")
    fun getProjectDetail(id: Int): Flow<ProjectDetailEntity?>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNote(id: Int): Flow<NoteEntity>

    @Query("DELETE FROM note WHERE id = :id")
    fun deleteNote(id: Int): Int

    @Query("DELETE FROM project WHERE id = :id")
    fun deleteProject(id: Int): Int
}