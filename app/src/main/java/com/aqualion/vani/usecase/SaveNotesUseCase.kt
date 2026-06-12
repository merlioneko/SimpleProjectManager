package com.aqualion.vani.usecase

import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.ProjectRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime

class SaveNotesUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
){
    /**
     * Noteをデータベースに保存する。
     * 新規作成/更新どちらにも対応
     */
    suspend operator fun invoke(notes: List<Note>):Result<Boolean> = runCatching {
        val newNotes = notes.filter { it.id == 0 }.map {it.copy(updatedAt=LocalTime.now().toString())}
        val updatedNotes = notes.filter { it.id != 0 }.map {it.copy(updatedAt=LocalTime.now().toString())}

        //TODO: リポジトリ内でトランザクションできた方が良いのではないか？
        withContext(Dispatchers.IO) {
            val newNotesSuccessCount = projectRepository.addNotes(newNotes)
            if (newNotesSuccessCount != newNotes.size.toLong()) {
                throw RuntimeException("Failed to insert notes")
            }
            val updatedNotesSuccessCount = projectRepository.updateNotes(updatedNotes)
            if (updatedNotesSuccessCount != updatedNotes.size.toLong()) {
                throw RuntimeException("Failed to update notes")
            }
            return@withContext notes.size.toLong() == newNotesSuccessCount + updatedNotesSuccessCount
        }
    }
}