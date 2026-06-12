package com.aqualion.vani.domain

import kotlinx.coroutines.flow.Flow

sealed class IoResult<T> {
    data class Success<T>(val value: T) : IoResult<T>()
    data class Failure<T>(val error: Throwable) : IoResult<T>()
}

interface ProjectRepository {
    fun getProjects(): Flow<List<Project>>
    suspend fun saveProjects(projects: List<Project>): Flow<IoResult<String>>

    /**
     * プロジェクトを削除する（関係するノートだけは残る）
     */
    suspend fun deleteProject(id: Int): Flow<IoResult<String>>

    fun getNotes(projectId: Int): Flow<List<Note>>

    /**
     * Add or update a new notes of a project.
     */
    suspend fun saveNotes(notes: List<Note>): Flow<IoResult<String>>
    suspend fun deleteNote(id: Int): Flow<IoResult<String>>

    fun getProjectDetail(projectId: Int): Flow<ProjectDetail?>
    suspend fun saveProjectDetail(projectDetail: ProjectDetail): Flow<IoResult<String>>
    fun getNote(id: Int): Flow<Note>

}