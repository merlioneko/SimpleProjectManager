package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject

class DeleteProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
){
    suspend operator fun invoke(id: Int) = runCatching{
        projectRepository.deleteProject(id)
    }
}