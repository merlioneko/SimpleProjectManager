package com.aqualion.vani.ui.detailscreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aqualion.vani.ui.NoteUiModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import com.aqualion.vani.ui.ProjectUiModel
import com.aqualion.vani.ui.theme.VaniTheme
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment

@Composable
fun ProjectDetailScreen(viewModel: ProjectDetailViewModel = hiltViewModel(),
                        onProjectSelected: (ProjectUiModel) -> Unit = {}) {
    val uiState by viewModel.projectDetailUiState.collectAsStateWithLifecycle()
    ProjectDetailContent(
        uiState = uiState,
        onAddItemRequested = viewModel::onRequestAddItem,
        onProjectNameEdited = viewModel::onProjectNameChanged,
        onNoteSelected = viewModel::onNoteSelected,
        onProjectSelected = onProjectSelected,
        onLongPress = viewModel::onRequestDeleteNote
    )

    DisposableEffect(Unit) {
        onDispose {
            if (uiState.isEdited) {
                viewModel.onSave()
            }
        }
    }

    val selectedNote by viewModel.selectedNote.collectAsStateWithLifecycle()
    when(uiState.showDialog) {
        DialogPurpose.EDIT_NOTE -> NoteDialog(
            selectedNote = selectedNote,
            onSaved = viewModel::onSave,
            onDismiss = viewModel::onDismissDialog,
            onError = viewModel::onError
        )
        DialogPurpose.DELETE_NOTE -> DeleteNoteDialog(
            onConfirm = { viewModel.onDeleteNote(selectedNote!!) },
            onDismiss = viewModel::onDismissDialog
        )
        DialogPurpose.CREATE_ITEM -> AddItemDialog(
            onConfirm = { name, isProject -> viewModel.onAddItem(name, isProject) },
            onDismiss = viewModel::onDismissDialog
        )
        else -> {}
    }


    when(uiState.errorMessage) {
        null -> {}
        else -> {
            ErrorDialog(
                message=uiState.errorMessage?:"Unknown error",
                onConfirm = viewModel::onInitError,
                onDismiss = viewModel::onInitError
            )
        }
    }

}

@Composable
fun ErrorDialog(message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        title = { Text(text = "Error") },
        text = { Text(text = message) },
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm) { Text("Reload") }},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameTopAppBar(modifier: Modifier = Modifier,
                  name: String, size: Int = 24, onNameEdited: (String) -> Unit = {}) {
    TopAppBar(
        modifier=modifier.fillMaxWidth(),
        title= {
            TextField(
                modifier=modifier.fillMaxWidth(),
                value = name,
                textStyle = TextStyle(fontSize = size.sp),
                singleLine = true,
                placeholder = { Text("Project name") },
                onValueChange = {onNameEdited(it)},
                colors= TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    )
}

// TODO: このバケツリレーはたぶん適宜ViewModelを用意したりしたらいい気はする
@Composable
fun ProjectDetailContent(uiState: ProjectDetailUiState,
                         onAddItemRequested: () -> Unit = {},
                         onProjectNameEdited: (String) -> Unit = {},
                         onNoteSelected: (NoteUiModel) -> Unit = {},
                         onProjectSelected: (ProjectUiModel) -> Unit = {},
                         onLongPress: (NoteUiModel) -> Unit = {}) {
    Scaffold(
        modifier=Modifier.fillMaxWidth(),
        topBar = { NameTopAppBar(name = uiState.projectName, onNameEdited = onProjectNameEdited) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddItemRequested() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) {innerPadding ->
        Column(modifier=Modifier.padding(innerPadding).verticalScroll(rememberScrollState())) {
            uiState.subProjects.forEach { SubProjectItem(project = it, onProjectSelected = onProjectSelected) }
            uiState.notes.forEach { NoteItem(note = it, onLongPress=onLongPress, onNoteSelected=onNoteSelected) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubProjectItem(modifier: Modifier = Modifier,
                   project: ProjectUiModel,
                   onProjectSelected: (ProjectUiModel) -> Unit = {}) {
    Card(modifier = modifier.fillMaxWidth()
        .padding(8.dp)
        .requiredHeight(60.dp)
        .clickable { onProjectSelected(project) }
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(1f),
                text = "[Project] ${project.name}",
                style = TextStyle(fontSize = 18.sp))
        }
    }
}


@Composable
fun NewNoteItem(modifier: Modifier = Modifier,
                note: NoteUiModel?,
                focusRequester: FocusRequester = remember { FocusRequester() },
                onValueChange: (String?) -> Unit,
                onAddNote: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).requiredHeight(100.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                value = note?.name ?: "",
                singleLine = true,
                placeholder = { Text("Note name") },
                onValueChange = { onValueChange(it) },
                keyboardActions = KeyboardActions(
                    onDone={
                        onAddNote(note?.name ?: "")
                        onValueChange(null)
                        keyboardController?.hide()
                    }
                )
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(text = "",
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.StartEllipsis
            )
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteItem(modifier: Modifier = Modifier,
             note: NoteUiModel,
             onNoteSelected: (NoteUiModel) -> Unit = {},
             onLongPress: (NoteUiModel) -> Unit = {}) {
    Card(modifier = modifier.fillMaxWidth()
        .padding(8.dp)
        .requiredHeight(100.dp)
        .combinedClickable(
            onLongClick = { onLongPress(note) },
            onClick = {
                onNoteSelected(note)
            }
        )) {
        Column(modifier = modifier.padding(8.dp)) {
            Text(modifier = modifier.fillMaxWidth(),
                text = note.name)
            HorizontalDivider(modifier = modifier.fillMaxWidth())
            Text(text = note.value,
                modifier = modifier.weight(1f),
                overflow = TextOverflow.StartEllipsis)
        }
    }
}

@Composable
fun AddItemDialog(onConfirm: (String, Boolean) -> Unit, onDismiss: () -> Unit) {
    val name = remember { mutableStateOf("") }
    val isProject = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Item") },
        text = {
            Column {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    placeholder = { Text("Enter name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isProject.value,
                        onClick = { isProject.value = false }
                    )
                    Text("Note", modifier = Modifier.clickable { isProject.value = false })
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = isProject.value,
                        onClick = { isProject.value = true }
                    )
                    Text("Project", modifier = Modifier.clickable { isProject.value = true })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotBlank()) {
                        onConfirm(name.value, isProject.value)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteNoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest=onDismiss,
        title = { Text("Delete note") },
        text = { Text("Are you sure you want to delete this note?") },
        confirmButton = {Button(onClick = {
            onConfirm()
            onDismiss()
        }) { Text("Delete") }},
        dismissButton = {Button(onClick = onDismiss) { Text("Cancel") }}
    )
}





@Preview
@Composable
fun ProjectDetailContentPreview() {
    VaniTheme {
        ProjectDetailContent(
            uiState = ProjectDetailUiState(
                projectId = 1,
                projectName = "Project 1",
                notes = listOf(
                    NoteUiModel(
                        id = 1, name = "Note 1",
                        value = "Value 1\nValue 1\nValue 1\nValue 1\nValue 1\n",
                        projectId = 1, createdAt = "", updatedAt = ""
                    ),
                    NoteUiModel(
                        id = 1, name = "Note 1",
                        value = "Value 1\nValue 1\nValue 1\nValue 1\nValue 1\n",
                        projectId = 1, createdAt = "", updatedAt = ""
                    )
                )
            )
        )
    }
}