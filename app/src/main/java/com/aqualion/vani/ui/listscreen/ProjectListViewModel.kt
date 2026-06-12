package com.aqualion.vani.ui.listscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqualion.vani.ui.ProjectUiModel
import com.aqualion.vani.ui.asUiState
import com.aqualion.vani.usecase.CreateProjectUseCaseTest
import com.aqualion.vani.usecase.DeleteProjectUseCase
import com.aqualion.vani.usecase.GetAllProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class DialogPurpose {
    ADD_PROJECT,
    DELETE_PROJECT
}

data class ProjectListUiState(
    val projects: List<ProjectUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDialog: DialogPurpose? = null
)

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val projectTest: CreateProjectUseCaseTest,
    private val getAllProjectsUseCase: GetAllProjectsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase
): ViewModel()  {
    private val _projectListUiState = MutableStateFlow(ProjectListUiState())
    val projectListUiState: StateFlow<ProjectListUiState> = _projectListUiState.asStateFlow()

    init {
        viewModelScope.launch {
            projectTest.execute()
        }
        refreshProjects()
    }

    fun refreshProjects() {
        viewModelScope.launch {
            getAllProjectsUseCase().onSuccess { projects ->
                _projectListUiState.update { it.copy(projects = projects.asUiState(), isLoading = false, errorMessage = null, showDialog = null) }
            }.onFailure { e ->
                _projectListUiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onAddProjectRequired() {
        _projectListUiState.update { it.copy(showDialog = DialogPurpose.ADD_PROJECT) }
    }
    fun onDialogDismiss() {
        _projectListUiState.update { it.copy(showDialog = null) }
    }

    private val _selectedProject = MutableStateFlow<ProjectUiModel?>(null)
    val selectedProject = _selectedProject.asStateFlow()

    fun onRequireDeleteProject(project: ProjectUiModel) {
        if (!_projectListUiState.value.projects.contains(project)) {
            _projectListUiState.update { it.copy(showDialog = null, errorMessage = "Project not found") }
        }
        _projectListUiState.update { it.copy(showDialog = DialogPurpose.DELETE_PROJECT) }
        _selectedProject.update { project }
    }

    fun onDeleteProject() {
        if (_selectedProject.value == null) throw IllegalArgumentException("No project selected")
        viewModelScope.launch {
            deleteProjectUseCase(_selectedProject.value!!.id)
        }
        refreshProjects()
    }
}