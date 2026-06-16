package com.aqualion.vani.ui.detailscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqualion.vani.domain.Note
import com.aqualion.vani.ui.NoteUiModel
import com.aqualion.vani.ui.asUiState
import com.aqualion.vani.usecase.GetNoteUseCase
import com.aqualion.vani.usecase.SaveNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class NoteUiState(
    val id: Int = -1,
    val name: String = "",
    val value: String = "",
    val projectId: Int = 0,
    val createdAt: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val saveNotesUseCase: SaveNotesUseCase
): ViewModel() {

    private val _noteUiState = MutableStateFlow<NoteUiState>(NoteUiState())
    val noteUiState = _noteUiState.asStateFlow()
    fun onError(message: String) {
        _noteUiState.update { it.copy(errorMessage = message) }
        Log.d("NoteViewModel:OnError", "error: $message")
    }
    fun initError() {
        _noteUiState.update { it.copy(errorMessage = null) }
    }
    
    init {
        viewModelScope.launch {
            init()
        }
    }

    @Throws(IllegalArgumentException::class)
    private suspend fun init() = runCatching {
        val id = _noteUiState.value.id
        getNoteUseCase.invoke(id).onSuccess {
            it.collect { note ->
                _noteUiState.update { NoteUiState(
                    id = note.id,
                    name = note.name,
                    value = note.value,
                    projectId = note.projectId,
                    createdAt = note.createdAt
                ) }
                Log.d("NoteViewModel", "note: $note")
            }
            setAutoSave()
        }
    }.onFailure { it ->
        _noteUiState.update { it.copy(errorMessage = it.errorMessage) }
        Log.d("NoteViewModel", "error: $it")
    }

    private fun setAutoSave() {
        flow {
            while (true) {
                delay(60_000.milliseconds)
                emit(Unit)
            }
        }.onEach {
            if (_isEdited.value) {
                onSave()
                Log.d("NoteViewModel", "Auto-saved")
            }
        }.launchIn(viewModelScope)
    }

    fun onInit(noteId: Int) {
        _noteUiState.update { NoteUiState(id = noteId) }
        viewModelScope.launch {
            init()
        }
    }

    private val _isEdited = MutableStateFlow(false)
    val isEdited = _isEdited.asStateFlow()
    fun onEdited() {
        _isEdited.update { true }
    }
    fun initEditState() {
        _isEdited.update { false }
    }

    fun onValueChange(value: String) {
        _noteUiState.update {it.copy(value = value)}
        onEdited()
    }

    fun onNameChanged(name: String) {
        _noteUiState.update {it.copy(name = name)}
        onEdited()
    }

    fun onSave() {
        val state = _noteUiState.value
        val note = Note(
            id = state.id,
            name = state.name,
            value = state.value,
            projectId = state.projectId,
            createdAt = state.createdAt,
        )
        viewModelScope.launch {
            saveNotesUseCase.invoke(listOf(note))
        }
        initEditState()
    }
}