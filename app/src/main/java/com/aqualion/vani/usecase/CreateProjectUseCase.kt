package com.aqualion.vani.usecase

import android.util.Log
import androidx.room.withTransaction
import com.aqualion.vani.data.AppDatabase
import com.aqualion.vani.data.ProjectRelationEntity
import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectRepository
import com.aqualion.vani.domain.toEntity
import jakarta.inject.Inject
import java.time.LocalDateTime

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val appDatabase: AppDatabase // トランザクション制御のため
) {
    /**
     * 引数の名前のプロジェクトを作成する。
     * UIスレッド上では実行しない事。
     * @param name 作成するプロジェクトの名前
     * @param parentId 親プロジェクトのID（任意）
     * @return 成功時、Unit、失敗時、例外
     */
    suspend operator fun invoke(name: String, parentId: Int? = null) = runCatching {
        val time = LocalDateTime.now().toString()
        val project = Project(
            id = 0,
            name = name,
            createdAt = time,
            updatedAt = time
        )

        val projectDao = appDatabase.projectDao()
        appDatabase.withTransaction {
            val newProjectId = projectDao.insert(project.toEntity()).toInt()
            if (newProjectId == 0) throw Exception("Failed to create project")

            if (parentId != null) {
                projectDao.insertRelation(ProjectRelationEntity(parentId = parentId, childId = newProjectId))
            }
        }
    }.onSuccess { Log.d("CreateNewProjectUseCase", "success") }
     .onFailure { Log.d("CreateNewProjectUseCase", "failure: ${it.message}") }
}
