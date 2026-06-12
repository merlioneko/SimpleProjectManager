package com.aqualion.vani.usecase

import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.Project
import java.time.LocalDateTime
import jakarta.inject.Inject

class CreateProjectUseCaseTest @Inject constructor(
    private val createNewProjectUseCase: CreateNewProjectUseCase
){
    private val _id = 999
    private val testProject = Project(
        id=_id,
        name="test",
        createdAt = LocalDateTime.now().toString(),
        updatedAt = LocalDateTime.now().toString()
    )
    private val testNote = Note(
        id=_id,
        name="test",
        value="test",
        projectId=_id,
        createdAt = LocalDateTime.now().toString(),
        updatedAt = LocalDateTime.now().toString()
    )

    suspend fun execute() {
        createNewProjectUseCase.invoke(testProject.name)
    }
}