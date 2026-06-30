package com.aqualion.vani.domain

import kotlinx.coroutines.flow.Flow

sealed class IoResult<T> {
    data class Success<T>(val value: T) : IoResult<T>()
    data class Failure<T>(val error: Throwable) : IoResult<T>()
}

interface ProjectRepository {
    fun getProjects(): Flow<List<Project>>
    suspend fun addProjects(projects: List<Project>): Long
    suspend fun updateProject(project: Project): Long

    /**
     * プロジェクトを削除する（関係するノートだけは残る）
     */
    suspend fun deleteProject(id: Int): Long

    fun getNotesByProject(projectId: Int): Flow<List<Note>>

    /**
     * Add a new notes of a project.
     */
    suspend fun addNotes(notes: List<Note>): Long
    suspend fun updateNotes(note: List<Note>): Long

    suspend fun deleteNote(id: Int): Long

    fun getProjectDetail(projectId: Int): Flow<ProjectDetail?>
    suspend fun addProjectDetail(projectDetail: ProjectDetail): Long
    suspend fun updateProjectDetail(projectDetail: ProjectDetail): Long
    fun getNote(id: Int): Flow<Note>

    suspend fun addProjectRelation(parentId: Int, childId: Int): Long
    suspend fun deleteProjectRelation(parentId: Int, childId: Int): Long
}
