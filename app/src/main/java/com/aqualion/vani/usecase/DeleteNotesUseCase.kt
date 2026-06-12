package com.aqualion.vani.usecase

import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteNotesUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(noteId: Int) = runCatching {
        projectRepository.deleteNote(noteId)
    }
}