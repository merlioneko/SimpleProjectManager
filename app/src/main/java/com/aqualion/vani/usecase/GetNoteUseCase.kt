package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject

class GetNoteUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(id: Int) = runCatching {
        projectRepository.getNote(id)
    }
}
