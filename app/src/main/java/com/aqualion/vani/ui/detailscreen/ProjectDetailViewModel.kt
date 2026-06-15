package com.aqualion.vani.ui.detailscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.ui.NoteUiModel
import com.aqualion.vani.ui.asUiState
import com.aqualion.vani.usecase.DeleteNotesUseCase
import com.aqualion.vani.usecase.GetProjectDetailUseCase
import com.aqualion.vani.usecase.SaveNotesUseCase
import com.aqualion.vani.usecase.SaveProjectDetailUseCase
import com.aqualion.vani.usecase.SaveProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds

enum class DialogPurpose {
    EDIT_NOTE,
    DELETE_NOTE
}

data class ProjectDetailUiState(
    val projectId: Int,
    val projectName: String = "",
    val projectCreatedAt: String = "",
    val notes: List<NoteUiModel> = emptyList(),
    val newNote: NoteUiModel? = null,
    val isLoading: Boolean = false,
    val showDialog: DialogPurpose? = null,
    val errorMessage: String? = null,
    val isEdited: Boolean = false
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProjectDetailUseCase: GetProjectDetailUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
    private val saveNotesUseCase: SaveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val saveProjectDetailUseCase: SaveProjectDetailUseCase
): ViewModel() {
    private val emptyDetail = ProjectDetailUiState(-1)
    private val _projectDetailUiState = MutableStateFlow(emptyDetail)
    val projectDetailUiState: StateFlow<ProjectDetailUiState> = _projectDetailUiState.asStateFlow()

    init {
        val projectId = savedStateHandle.get<Int>("projectId")
        if (projectId != null) {
            viewModelScope.launch {
                initDetail(projectId).onFailure { e ->
                    _projectDetailUiState.update { it.copy(errorMessage = e.message) }
                }
            }
        }
    }

    /**
     * プロジェクト詳細を再取得する
     * @param projectId プロジェクトID
     */
    private suspend fun initDetail(projectId: Int): Result<Unit> = runCatching {
        getProjectDetailUseCase(projectId).onSuccess { projectDetail ->
            if (projectDetail == null) {
                _projectDetailUiState.update { it.copy(isLoading = false) }
                throw IllegalArgumentException("Project not found")
            }

            _projectDetailUiState.update { uiState ->
                uiState.copy(
                    projectId = projectDetail.project.id,
                    projectName = projectDetail.project.name,
                    notes = projectDetail.notes.asUiState(),
                    showDialog = null,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }

        // Auto-save every 1 minute if edited
        flow {
            while (true) {
                delay(60_000.milliseconds)
                emit(Unit)
            }
        }.onEach {
            if (_projectDetailUiState.value.isEdited) {
                onSave()
                Log.d("ProjectDetailViewModel", "Auto-saved")
            }
        }.launchIn(viewModelScope)
    }

    fun refreshDetail() {
        viewModelScope.launch {
            val projectId = _projectDetailUiState.value.projectId
            initDetail(projectId)
        }
    }

    fun onInitError() {
        _projectDetailUiState.update { it.copy(errorMessage = null) }
    }

    fun onProjectNameChanged(name: String) {
        _projectDetailUiState.update { it.copy(projectName = name) }
        onEdited()
    }

    fun onSaveProjectName() {
        val project = Project(
            _projectDetailUiState.value.projectId,
            _projectDetailUiState.value.projectName,
            _projectDetailUiState.value.projectCreatedAt,
            LocalTime.now().toString()
        )
        viewModelScope.launch {
            saveProjectUseCase(project)
        }
        _projectDetailUiState.update { it.copy(isEdited = false) }
        refreshDetail()
    }

    fun onRequestAddNewNote() {
        val newNote = NoteUiModel(name="", value="", projectId = _projectDetailUiState.value.projectId)
        _projectDetailUiState.update { it.copy(newNote = newNote) }
    }

    fun onChangedNewNoteName(name: String?) {
        _projectDetailUiState.update {
            if (name == null) {
                it.copy(newNote = null)
            } else {
                it.copy(newNote = it.newNote?.copy(name = name))
            }
        }
        Log.d("onNewNoteNameChanged", name?: "null")
        onEdited()
    }

    fun onAddNote(noteName: String) {
        _projectDetailUiState.update { it.copy(newNote = null) }
        viewModelScope.launch {
            saveNotesUseCase(
                listOf(
                    Note(name=noteName, value= "", projectId = _projectDetailUiState.value.projectId,)
                )
            )
        }
        Log.d("onAddNote", noteName)
        onEdited()
    }

    private val _selectedNote = MutableStateFlow<NoteUiModel?>(null)
    val selectedNote = _selectedNote.asStateFlow()
    fun onNoteSelected(note: NoteUiModel) {
        _selectedNote.update { note }
        savedStateHandle["selectedNoteId"] = note.id
        _projectDetailUiState.update { it.copy(showDialog = DialogPurpose.EDIT_NOTE) }
    }

    fun onEdited() {
        _projectDetailUiState.update { it.copy(isEdited = true) }
    }

    fun onRequestDeleteNote(note: NoteUiModel) {
        if (!_projectDetailUiState.value.notes.contains(note)) throw IllegalArgumentException("No note such id")
        _projectDetailUiState.update { it.copy(showDialog = DialogPurpose.DELETE_NOTE) }
        Log.d("onDeleteNote", note.toString())
    }

    fun onDeleteNote(note: NoteUiModel) {
        _projectDetailUiState.update { it.copy(showDialog = null) }
        viewModelScope.launch {
            deleteNotesUseCase(note.id)
        }
        refreshDetail()
    }

    fun onSave() {
        val project = Project(
            _projectDetailUiState.value.projectId,
            _projectDetailUiState.value.projectName,
            _projectDetailUiState.value.projectCreatedAt)
        val notes = _projectDetailUiState.value.notes.map { Note(it.id, it.name, it.value, it.projectId, it.createdAt, it.updatedAt)}
        val projectDetail = ProjectDetail(project, notes)
        viewModelScope.launch {
            saveProjectDetailUseCase(projectDetail)
        }
        _projectDetailUiState.update { it.copy(isEdited = false) }
        refreshDetail()
    }
}