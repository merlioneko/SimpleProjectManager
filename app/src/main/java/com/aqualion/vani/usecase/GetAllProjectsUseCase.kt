package com.aqualion.vani.usecase

import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class GetAllProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
){
    suspend operator fun invoke(): Result<List<Project>> = runCatching {
        projectRepository.getProjects().first()
    }
}
