package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import java.time.LocalTime

class SaveProjectDetailUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
){
    suspend operator fun invoke(projectDetail: ProjectDetail) = runCatching {
        projectRepository.updateProjectDetail(projectDetail.copy(
            project = projectDetail.project.copy(updatedAt = LocalTime.now().toString()),
            notes = projectDetail.notes.map { it.copy(updatedAt = LocalTime.now().toString()) }
        )) == 1L
    }
}
