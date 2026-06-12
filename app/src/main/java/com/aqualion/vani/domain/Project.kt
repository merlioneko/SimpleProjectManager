package com.aqualion.vani.domain

import com.aqualion.vani.data.NoteEntity
import com.aqualion.vani.data.ProjectEntity
import com.aqualion.vani.data.ProjectDetailEntity
import java.time.LocalTime

data class Project(
    val id: Int,
    val name: String,
    val createdAt: String,
    val updatedAt: String
) {
    init {
        require(name.isNotBlank()) { "Project name cannot be blank" }
    }
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        id = this.id,
        name = this.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun ProjectEntity.toDomain(): Project {
    return Project(
        id = this.id,
        name = this.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class Note(
    val id: Int,
    val name: String,
    val value: String,
    val projectId: Int,
    val createdAt: String,
    val updatedAt: String
) {
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = this.id,
        name = this.name,
        value = this.value,
        projectId = this.projectId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun NoteEntity.toDomain(): Note {
    return Note(
        id = this.id,
        name = this.name,
        value = this.value,
        projectId = this.projectId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class ProjectDetail(
    val project: Project,
    val notes: List<Note>
) {
}

fun ProjectDetail.toEntity(): ProjectDetailEntity {
    return ProjectDetailEntity(
        projectEntity = this.project.toEntity(),
        noteEntities = this.notes.map { it.toEntity() }
    )
}

fun ProjectDetailEntity.toDomain(): ProjectDetail {
    return ProjectDetail(
        project = this.projectEntity.toDomain(),
        notes = this.noteEntities.map { it.toDomain() }
    )
}