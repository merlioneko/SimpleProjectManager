package com.aqualion.vani.usecase

import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import java.time.LocalTime

class SaveProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project) = runCatching {
        projectRepository.addProjects(listOf(project.copy(updatedAt= LocalTime.now().toString())))
    }
}