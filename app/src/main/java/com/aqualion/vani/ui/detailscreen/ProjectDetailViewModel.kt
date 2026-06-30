package com.aqualion.vani.ui.detailscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqualion.vani.domain.Note
import com.aqualion.vani.domain.Project
import com.aqualion.vani.domain.ProjectDetail
import com.aqualion.vani.ui.NoteUiModel
import com.aqualion.vani.ui.ProjectUiModel
import com.aqualion.vani.ui.asUiState
import com.aqualion.vani.usecase.CreateProjectUseCase
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds

enum class DialogPurpose {
    EDIT_NOTE,
    DELETE_NOTE,
    CREATE_ITEM
}

data class ProjectDetailUiState(
    val projectId: Int,
    val projectName: String = "",
    val projectCreatedAt: String = "",
    val notes: List<NoteUiModel> = emptyList(),
    val subProjects: List<ProjectUiModel> = emptyList(),
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
    private val createProjectUseCase: CreateProjectUseCase,
    private val saveNotesUseCase: SaveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase
): ViewModel() {
    private val emptyDetail = ProjectDetailUiState(-1)
    private val _projectDetailUiState = MutableStateFlow(emptyDetail)
    val projectDetailUiState: StateFlow<ProjectDetailUiState> = _projectDetailUiState.asStateFlow()

    init {
        val projectId = savedStateHandle.get<Int>("projectId")
        if (projectId != null) {
            observeDetail(projectId)
            setAutoSave()
        }
    }

    /**
     * プロジェクト詳細を継続購読する。
     * Room の Flow を購読しているので、ノート保存・追加・削除などで
     * DB が変われば自動的に最新の値で UI が更新される（手動の再取得は不要）。
     * @param projectId プロジェクトID
     */
    private fun observeDetail(projectId: Int) {
        getProjectDetailUseCase(projectId)
            .onEach { projectDetail ->
                if (projectDetail == null) {
                    _projectDetailUiState.update { it.copy(errorMessage = "Project not found") }
                    return@onEach
                }

                _projectDetailUiState.update { current ->
                    current.copy(
                        projectId = projectDetail.project.id,
                        // プロジェクト名を編集中はユーザー入力を優先し、DB値で上書きしない
                        projectName = if (current.isEdited) current.projectName else projectDetail.project.name,
                        projectCreatedAt = projectDetail.project.createdAt,
                        notes = projectDetail.notes.asUiState(),
                        subProjects = projectDetail.subProjects.asUiState(),
                        isLoading = false
                    )
                }
            }
            .catch { e ->
                _projectDetailUiState.update { it.copy(errorMessage = e.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun setAutoSave() {
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

    /**
     * ダイアログを閉じる。ノート一覧は Flow で自動更新されるため、
     * ここでは表示中のダイアログ状態を閉じるだけでよい。
     */
    fun onDismissDialog() {
        _projectDetailUiState.update { it.copy(showDialog = null) }
    }

    fun onError(message: String) {
        _projectDetailUiState.update { it.copy(errorMessage = message) }
    }
    fun onInitError() {
        _projectDetailUiState.update { it.copy(errorMessage = null) }
    }

    fun onProjectNameChanged(name: String) {
        _projectDetailUiState.update { it.copy(projectName = name) }
        onEdited()
    }

    fun onRequestAddNewNote() {
        val newNote = NoteUiModel(name="", value="", projectId = _projectDetailUiState.value.projectId)
        _projectDetailUiState.update { it.copy(newNote = newNote) }
    }

    fun onRequestAddItem() {
        _projectDetailUiState.update { it.copy(showDialog = DialogPurpose.CREATE_ITEM) }
    }

    fun onAddItem(name: String, isProject: Boolean) {
        _projectDetailUiState.update { it.copy(showDialog = null) }
        viewModelScope.launch {
            if (isProject) {
                createProjectUseCase(name, _projectDetailUiState.value.projectId)
            } else {
                saveNotesUseCase(
                    listOf(
                        Note(name = name, value = "", projectId = _projectDetailUiState.value.projectId)
                    )
                )
            }
        }
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
        Log.d("onNoteSelected", "${note.id}")
        _selectedNote.update { note }
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
    }

    fun onSave() {
        val project = Project(
            _projectDetailUiState.value.projectId,
            _projectDetailUiState.value.projectName,
            _projectDetailUiState.value.projectCreatedAt)
        viewModelScope.launch {
            saveProjectUseCase(project)
        }
        _projectDetailUiState.update { it.copy(isEdited = false) }
    }
}