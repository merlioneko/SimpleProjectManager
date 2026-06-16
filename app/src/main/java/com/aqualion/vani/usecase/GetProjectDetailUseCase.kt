package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetProjectDetailUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * プロジェクト詳細を購読します。
     * DB（Room）の変更を検知して継続的に最新の値を流すため、
     * ノート保存などでDBが変わると自動的に新しい値が流れます。
     * @param projectId プロジェクトID
     * @return プロジェクト詳細を流す Flow
     */
    operator fun invoke(projectId: Int): Flow<ProjectDetail?> =
        projectRepository.getProjectDetail(projectId)
            .flowOn(Dispatchers.IO)
}
