package com.aqualion.vani.ui.detailscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqualion.vani.domain.Note
import com.aqualion.vani.usecase.SaveNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Deprecated("no longer needed")
@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val addNoteUseCase: SaveNotesUseCase
): ViewModel() {
    private val _noteName = MutableStateFlow("")
    val noteName = _noteName.asStateFlow()
    fun updateNoteName(name: String) {
        _noteName.update {name}
    }

    fun onAddNote() {
        viewModelScope.launch {
            val projectId = savedStateHandle.get<Int>("projectId") ?: throw IllegalArgumentException("Project ID not found")
            addNoteUseCase.invoke(
                listOf(
                    Note(
                        name=_noteName.value,
                        value="",
                        projectId=projectId
                    )
                )
            )
        }
    }

    fun onDismiss() {
        updateNoteName("")
    }
}