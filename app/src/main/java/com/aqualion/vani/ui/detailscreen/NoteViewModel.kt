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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val saveNotesUseCase: SaveNotesUseCase
): ViewModel() {
    private val emptyNote = NoteUiModel(-1, "", "", 0, "", "")
    private val _noteUiModel = MutableStateFlow(emptyNote)
    val noteUiModel = _noteUiModel.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                if (_noteUiModel.value.id == emptyNote.id) throw IllegalArgumentException("Note id is not set")
                getNoteUseCase.invoke(_noteUiModel.value.id).onSuccess {
                    it.collect { note ->
                        _noteUiModel.update {note.asUiState() }
                        Log.d("NoteViewModel", "note: $note")
                    }
                }
            }.onFailure {
                _errorMessage.update { it }
            }
        }
    }

    fun onInit(note: NoteUiModel) {
        _noteUiModel.update { note }
    }

    private val _isEdited = MutableStateFlow(false)
    val isEdited = _isEdited.asStateFlow()
    fun onEdited() {
        _isEdited.value = true
    }
    fun initEditState() {
        _isEdited.value = false
    }

    fun onValueChange(value: String) {
        _noteUiModel.value = _noteUiModel.value.copy(value = value)
        onEdited()
    }

    fun onNameChanged(name: String) {
        _noteUiModel.value = _noteUiModel.value.copy(name = name)
        onEdited()
    }

    fun onSave() {
        val note = Note(
            id = _noteUiModel.value.id,
            name = _noteUiModel.value.name,
            value = _noteUiModel.value.value,
            projectId = _noteUiModel.value.projectId,
            createdAt = _noteUiModel.value.createdAt,
            updatedAt = _noteUiModel.value.updatedAt
        )
        viewModelScope.launch {
            saveNotesUseCase.invoke(listOf(note))
        }
        initEditState()
    }
}