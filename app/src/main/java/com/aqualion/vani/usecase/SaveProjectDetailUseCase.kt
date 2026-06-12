package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject

class SaveProjectDetailUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
){
    suspend operator fun invoke(projectDetail: ProjectDetail) = runCatching {
        projectRepository.saveProjectDetail(projectDetail)
    }
}
