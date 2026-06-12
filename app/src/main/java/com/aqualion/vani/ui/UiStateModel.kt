package com.aqualion.vani.ui

import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.Project

interface UiStateModel {

}

data class ProjectUiModel(
    val id: Int,
    val name: String,
    val createdAt: String,
    val updatedAt: String
): UiStateModel

fun Project.asUiState(): ProjectUiModel = ProjectUiModel(
    id = this.id,
    name = this.name,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

@JvmName("asProjectUiState")
fun List<Project>.asUiState(): List<ProjectUiModel> = this.map { it.asUiState() }

data class NoteUiModel(
    val id: Int,
    val name: String,
    val value: String,
    val projectId: Int,
    val createdAt: String,
    val updatedAt: String
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