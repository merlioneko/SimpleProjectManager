package com.aqualion.vani.data

import androidx.room.withTransaction
import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.ProjectRepository
import com.aqualion.vani.domain.toDomain
import com.aqualion.vani.domain.toEntity
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProjectRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase
): ProjectRepository {
    private val projectDao = appDatabase.projectDao()

    override fun getProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addProjects(projects: List<Project>): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projects.count {
                projectDao.insert(it.toEntity()) != 0L
            }.toLong()
        }
    }

    override suspend fun updateProject(project: Project): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.update(project.toEntity())
        }.toLong()
    }

    override suspend fun deleteProject(id: Int): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.deleteProject(id)
        }.toLong()
    }

    override fun getNotesByProject(projectId: Int): Flow<List<Note>> {
        return projectDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addNotes(notes: List<Note>): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            notes.count {
                projectDao.insert(it.toEntity()) != 0L
            }.toLong()
        }
    }

    override suspend fun updateNotes(note: List<Note>): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            note.count {
                projectDao.update(it.toEntity()) != 0
            }.toLong()
        }
    }

    override suspend fun deleteNote(id: Int): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.deleteNote(id)
        }.toLong()
    }

    override fun getProjectDetail(projectId: Int): Flow<ProjectDetail?> {
        return projectDao.getProjectDetail(projectId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun addProjectDetail(projectDetail: ProjectDetail): Long = withContext(Dispatchers.IO) {
        val project = projectDetail.project.toEntity()
        val notes = projectDetail.notes.map { it.toEntity() }
        val newNotes = notes.filter { it.id == DataBaseRule.AUTO_INCREMENT }
        val updatedNotes = notes.filter { it.id != DataBaseRule.AUTO_INCREMENT }

        appDatabase.withTransaction {
            if (projectDao.insert(project) == 0L) {
                throw IllegalArgumentException("Failed to insert project")
            }
            val newNoteSuccessCount = newNotes.count {
                projectDao.insert(it) != 0L
            }.toLong()

            if (newNoteSuccessCount != newNotes.size.toLong()) {
                throw IllegalArgumentException("Failed to insert notes")
            }

            val updateNoteSuccessCount = updatedNotes.count {
                projectDao.update(it) != 0
            }.toLong()
            if (updateNoteSuccessCount != updatedNotes.size.toLong()) {
                throw IllegalArgumentException("Failed to update notes")
            }
            return@withTransaction newNoteSuccessCount + updateNoteSuccessCount
        }
    }

    /**
     * ProjectDetailを更新する。
     * return 成功：更新した行数、失敗：例外
     */
    override suspend fun updateProjectDetail(projectDetail: ProjectDetail): Long = withContext(Dispatchers.IO) {
        val project = projectDetail.project.toEntity()
        val notes = projectDetail.notes.map { it.toEntity() }
        val newNotes = notes.filter { it.id == DataBaseRule.AUTO_INCREMENT }
        val updatedNotes = notes.filter { it.id != DataBaseRule.AUTO_INCREMENT }

        appDatabase.withTransaction {
            if (projectDao.update(project) == 0) {
                throw IllegalArgumentException("Failed to insert project")
            }
            val newNoteSuccessCount = newNotes.count {
                projectDao.insert(it) != 0L
            }.toLong()

            if (newNoteSuccessCount != newNotes.size.toLong()) {
                throw IllegalArgumentException("Failed to insert notes")
            }

            val updateNoteSuccessCount = updatedNotes.count {
                projectDao.update(it) != 0
            }.toLong()
            if (updateNoteSuccessCount != updatedNotes.size.toLong()) {
                throw IllegalArgumentException("Failed to update notes")
            }
            return@withTransaction newNoteSuccessCount + updateNoteSuccessCount
        }

    }

    override fun getNote(id: Int): Flow<Note> {
        return projectDao.getNote(id).map { it.toDomain() }
    }

    override suspend fun addProjectRelation(parentId: Int, childId: Int): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.insertRelation(ProjectRelationEntity(parentId, childId))
        }
    }

    override suspend fun deleteProjectRelation(parentId: Int, childId: Int): Long = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.deleteRelation(parentId, childId).toLong()
        }
    }
}
