package com.aqualion.vani.usecase

import android.util.Log
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject

class AddProjectRelationUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * 親プロジェクトと子プロジェクトの関係を登録する。
     * @param parentId 親プロジェクトのID
     * @param childId 子プロジェクトのID
     * @return 成功時、Unit、失敗時、例外
     */
    suspend operator fun invoke(parentId: Int, childId: Int) = runCatching {
        // TODO: ここで循環参照チェックを入れるのが望ましい
        projectRepository.addProjectRelation(parentId, childId)
    }.onSuccess { Log.d("AddProjectRelationUseCase", "success") }
     .onFailure { Log.d("AddProjectRelationUseCase", "failure: ${it.message}") }
}
