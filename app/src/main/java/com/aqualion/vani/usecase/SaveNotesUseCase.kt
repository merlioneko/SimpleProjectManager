package com.aqualion.vani.usecase

import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveNotesUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
){
    suspend operator fun invoke(notes: List<Note>):Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            projectRepository.saveNotes(notes)
        }
    }
}