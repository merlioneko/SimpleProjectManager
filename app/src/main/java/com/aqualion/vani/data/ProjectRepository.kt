package com.aqualion.vani.data

import android.util.Log
import androidx.room.withTransaction
import com.aqualion.vani.domain.IoResult
import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.ProjectRepository
import com.aqualion.vani.domain.toDomain
import com.aqualion.vani.domain.toEntity
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    override suspend fun saveProjects(projects: List<Project>): Flow<IoResult<String>> = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projects.forEach {
                projectDao.insert(it.toEntity())
            }
        }
        Log.d("ProjectRepositoryImpl", "saved projects: $projects")
        return@withContext flowOf(IoResult.Success("Success"))
    }

    override suspend fun deleteProject(id: Int): Flow<IoResult<String>> = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.deleteProject(id)
        }
        return@withContext flowOf(IoResult.Success("Success"))
    }

    override fun getNotes(projectId: Int): Flow<List<Note>> {
        return projectDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNotes(notes: List<Note>): Flow<IoResult<String>> = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            notes.forEach {
                projectDao.insert(it.toEntity())
            }
        }
        return@withContext flowOf(IoResult.Success("Success"))
    }

    override suspend fun deleteNote(id: Int): Flow<IoResult<String>> = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            projectDao.deleteNote(id)
        }
        return@withContext flowOf(IoResult.Success("Success"))
    }

    override fun getProjectDetail(projectId: Int): Flow<ProjectDetail?> {
        return projectDao.getProjectDetail(projectId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveProjectDetail(projectDetail: ProjectDetail): Flow<IoResult<String>> = withContext(Dispatchers.IO) {
        val project = projectDetail.project.toEntity()
        val notes = projectDetail.notes.map { it.toEntity() }

        appDatabase.withTransaction {
            projectDao.insert(project)
            notes.forEach {
                projectDao.insert(it)
            }
        }
        return@withContext flowOf(IoResult.Success("Success"))
    }

    override fun getNote(id: Int): Flow<Note> {
        return projectDao.getNote(id).map { it.toDomain() }
    }
}
