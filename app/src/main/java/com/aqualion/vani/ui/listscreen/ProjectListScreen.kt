package com.aqualion.vani.ui.listscreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aqualion.vani.ui.ProjectUiModel
import com.aqualion.vani.ui.theme.VaniTheme

@Composable
fun ProjectListScreen(viewModel: ProjectListViewModel = hiltViewModel(),
                      onProjectSelected: (ProjectUiModel) -> Unit) {
    val uiState by viewModel.projectListUiState.collectAsStateWithLifecycle()
    ProjectListScreenContent(
        projectListUiState = uiState,
        onAddProject = viewModel::onAddProjectRequired,
        onDialogDismiss = viewModel::onDialogDismiss,
        onProjectAdded = viewModel::refreshProjects,
        onDeleteProject = viewModel::onDeleteProject,
        onLongPressProject = viewModel::onRequireDeleteProject,
        onProjectSelected = onProjectSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreenContent(
    projectListUiState: ProjectListUiState,
    onAddProject: () -> Unit,
    onDialogDismiss: () -> Unit,
    onProjectAdded: () -> Unit,
    onProjectSelected: (ProjectUiModel) -> Unit,
    onLongPressProject: (ProjectUiModel) -> Unit = {},
    onDeleteProject: () -> Unit = {}
) {
    Scaffold(
        modifier=Modifier,
        topBar = { TopAppBar(title={Text(text = "Projects")}) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProject) {
                Icon(Icons.Filled.Add, contentDescription = "Add Project")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)
            .padding(16.dp)) {
            items(projectListUiState.projects) { project ->
                ProjectCard(modifier = Modifier,
                    project = project,
                    onProjectSelected = onProjectSelected,
                    onProjectLongPress = onLongPressProject
                )
            }
        }
    }

    when(projectListUiState.showDialog) {
        DialogPurpose.ADD_PROJECT -> AddProjectDialog(
            onConfirm = onProjectAdded,
            onDismiss = onDialogDismiss
        )
        DialogPurpose.DELETE_PROJECT -> DeleteProjectDialog(
            onConfirm = onDeleteProject,
            onDismiss = onDialogDismiss
        )
        else -> {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectCard(modifier: Modifier = Modifier,
                project: ProjectUiModel,
                onProjectSelected: (ProjectUiModel) -> Unit,
                onProjectLongPress: (ProjectUiModel) -> Unit = {}) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
            .combinedClickable(
                onLongClick = { onProjectLongPress(project) },
                onClick = { onProjectSelected(project) },
            ),
        colors= CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = project.name)
        }

    }
}

@Composable
fun AddProjectDialog(
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    dialogViewModel: AddProjectDialogViewModel = hiltViewModel()
) {
    val projectName by dialogViewModel.projectName.collectAsStateWithLifecycle()

    AddProjectDialogContent(
        projectName = projectName,
        onProjectNameChange = { dialogViewModel.updateProjectName(it) },
        onConfirm = {
            Log.d("addProject", "confirming")
            dialogViewModel.onAddProject()
            dialogViewModel.onDismiss()
            onConfirm()
            onDismiss() // Close the dialog after confirming
        },
        onDismiss = {
            dialogViewModel.onDismiss()
            onDismiss()
        }
    )
}

@Composable
fun AddProjectDialogContent(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(text = "Add Project") },
        text = {
            Row(modifier=Modifier) {
                Text(text = "Name:")
                TextField(value=projectName, onValueChange = onProjectNameChange)
            }},
        onDismissRequest = onDismiss,
        confirmButton = {Button(modifier=Modifier, onClick=onConfirm) { Text("OK") } },
        dismissButton = {Button(modifier=Modifier, onClick= onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun DeleteProjectDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete project") },
        text = { Text("Are you sure you want to delete this project?") },
        confirmButton = {
            Button(onClick = {
                    onConfirm()
                    onDismiss()
            }){ Text(text="OK") }
        },
        dismissButton = {
            Button(onClick = onDismiss){ Text(text="Cancel") }
        }
    )

}

@Preview
@Composable
fun ListScreenPreview() {
    VaniTheme {
        ProjectListScreenContent(
            projectListUiState = ProjectListUiState(
                projects = listOf(
                    ProjectUiModel(id = 1, name = "Project 1", createdAt = "", updatedAt = ""),
                    ProjectUiModel(id = 2, name = "Project 2", createdAt = "", updatedAt = "")
                )
            ),
            onAddProject = {},
            onDialogDismiss = {},
            onProjectAdded = {},
            onProjectSelected = {}
        )
    }
}