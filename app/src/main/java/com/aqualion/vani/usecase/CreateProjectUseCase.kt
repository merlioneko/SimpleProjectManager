package com.aqualion.vani.usecase

import android.util.Log
import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import java.time.LocalDateTime

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * 引数の名前のプロジェクトを作成する。
     * UIスレッド上では実行しない事。
     * @param name 作成するプロジェクトの名前
     * @return 成功時、Unit、失敗時、例外
     */
    suspend operator fun invoke(name: String) = runCatching {
        val time = LocalDateTime.now().toString()
        val project = Project(
            id = 0,
            name = name,
            createdAt = time,
            updatedAt = time
        )
        projectRepository.addProjects(listOf(project))
    }.onSuccess { Log.d("CreateNewProjectUseCase", "success") }
     .onFailure { Log.d("CreateNewProjectUseCase", "failure: ${it.message}") }
}
