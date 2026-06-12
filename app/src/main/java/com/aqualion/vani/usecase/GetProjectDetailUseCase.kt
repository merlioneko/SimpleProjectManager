package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GetProjectDetailUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * プロジェクト詳細を取得します
     * @param projectId プロジェクトID
     * @return プロジェクト詳細
     */
    suspend operator fun invoke(projectId: Int): Result<ProjectDetail?> = runCatching {
        withContext(Dispatchers.IO) {
            projectRepository.getProjectDetail(projectId).first {
                it != null
            }
        }
    }
}
