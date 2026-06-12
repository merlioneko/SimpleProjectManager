package com.aqualion.vani.ui

import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.Project
import java.time.LocalTime

interface UiStateModel {

}

data class ProjectUiModel(
    val id: Int = 0,
    val name: String,
    val createdAt: String = LocalTime.now().toString(),
    val updatedAt: String = LocalTime.now().toString()
): UiStateModel

fun Project.asUiState(): ProjectUiModel = ProjectUiModel(
    id = this.id,
    name = this.name,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun ProjectUiModel.toDomain(): Project = Project(
    id = this.id,
    name = this.name,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

@JvmName("asProjectUiState")
fun List<Project>.asUiState(): List<ProjectUiModel> = this.map { it.asUiState() }

data class NoteUiModel(
    val id: Int = 0,
    val name: String,
    val value: String,
    val projectId: Int,
    val createdAt: String = LocalTime.now().toString(),
    val updatedAt: String = LocalTime.now().toString()
): UiStateModel

fun Note.asUiState(): NoteUiModel = NoteUiModel(
    id = this.id,
    name = this.name,
    value = this.value,
    projectId = this.projectId,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

@JvmName("asNoteUiState")
fun List<Note>.asUiState(): List<NoteUiModel> = this.map { it.asUiState() }

data class ProjectDetailUiModel(
    val project: ProjectUiModel,
    val notes: List<NoteUiModel>
): UiStateModel

fun ProjectDetail.asUiState(): ProjectDetailUiModel = ProjectDetailUiModel(
    project = this.project.asUiState(),
    notes = this.notes.map { it.asUiState() }
)