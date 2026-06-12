package com.aqualion.vani.ui.listscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqualion.vani.usecase.CreateNewProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddProjectDialogViewModel @Inject constructor(
    private val createProjectUseCase: CreateNewProjectUseCase
) : ViewModel() {
    private val _projectName = MutableStateFlow("")
    val projectName = _projectName.asStateFlow()
    fun updateProjectName(name: String) {
        _projectName.update {name}
    }

    fun onAddProject() {
        viewModelScope.launch {
            createProjectUseCase.invoke(_projectName.value)
        }
    }

    fun onDismiss() {
        updateProjectName("")
    }
}