package com.aqualion.vani.ui.detailscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import com.aqualion.vani.ui.theme.VaniTheme
import kotlinx.coroutines.launch
import kotlin.collections.forEach

@Composable
fun ProjectDetailScreen(viewModel: ProjectDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.projectDetailUiState.collectAsStateWithLifecycle()
    ProjectDetailContent(
        uiState = uiState,
        onNewNoteRequired = viewModel::onRequestAddNewNote,
        onNewNoteNameChanged = viewModel::onChangedNewNoteName,
        onAddNote = {
            viewModel.onAddNote(it)
            viewModel.refreshDetail()
        },
        onProjectNameEdited = viewModel::onProjectNameChanged,
        onNoteSelected = viewModel::onNoteSelected,
        onLongPress = viewModel::onRequestDeleteNote
    )

    DisposableEffect(Unit) {
        onDispose {
            if (uiState.isEdited) {
                viewModel.onSave()
            }
            viewModel.refreshDetail()
        }
    }

    when(uiState.errorMessage) {
        null -> {}
        else -> {
            AlertDialog(
                title = { Text(text = "Error") },
                text = { Text(text = uiState.errorMessage?: "Unknown error") },
                onDismissRequest = viewModel::onInitError,
                confirmButton = {
                    Button(onClick = {
                        viewModel.onInitError()
                        viewModel.refreshDetail()
                    }) { Text("Reload") }},
            )
        }
    }
    val selectedNote by viewModel.selectedNote.collectAsStateWithLifecycle()
    when(uiState.showDialog) {
        DialogPurpose.EDIT_NOTE -> NoteDialog(selectedNote = selectedNote,
            onDismiss = {
                viewModel.refreshDetail()
            }
        )
        DialogPurpose.DELETE_NOTE -> DeleteNoteDialog(
            onConfirm = { viewModel.onDeleteNote(selectedNote!!) },
            onDismiss = { viewModel.refreshDetail() }
        )
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameTopAppBar(modifier: Modifier = Modifier, name: String, size: Int = 24, onNameEdited: (String) -> Unit = {}) {
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
                         onNewNoteRequired: () -> Unit = {},
                         onNewNoteNameChanged: (String?) -> Unit = {},
                         onAddNote: (String) -> Unit = {},
                         onProjectNameEdited: (String) -> Unit = {},
                         onNoteSelected: (NoteUiModel) -> Unit = {},
                         onLongPress: (NoteUiModel) -> Unit = {}) {
    Scaffold(
        modifier=Modifier.fillMaxWidth(),
        topBar = { NameTopAppBar(name = uiState.projectName, onNameEdited = onProjectNameEdited) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNewNoteRequired() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }
        }
    ) {innerPadding ->
        Column(modifier=Modifier.padding(innerPadding)) {
            uiState.notes.forEach { NoteItem(note = it, onLongPress=onLongPress, onNoteSelected=onNoteSelected) }
            when (uiState.newNote) {
                null -> {}
                else -> {
                    NewNoteItem(modifier=Modifier, note = uiState.newNote, onValueChange = onNewNoteNameChanged, onAddNote = onAddNote)
                }
            }
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

@Composable
fun NoteDialog(viewModel: NoteViewModel = hiltViewModel(), selectedNote: NoteUiModel?, onDismiss: () -> Unit) {
    if (selectedNote == null) {
        onDismiss()
    }
    LaunchedEffect(Unit) {
        viewModel.onInit(selectedNote!!)
    }

    val uiState by viewModel.noteUiModel.collectAsStateWithLifecycle()
    val isEdited by viewModel.isEdited.collectAsStateWithLifecycle()
    Dialog(
        onDismissRequest = {
            if (isEdited) {
                viewModel.onSave()
            }
            onDismiss()
            },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Scaffold(
            topBar = { NameTopAppBar(modifier=Modifier, name = uiState.name, size=18, onNameEdited = viewModel::onNameChanged) },
        ) {innerPadding ->
            val focusRequester = remember { FocusRequester() }

            val scrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            Column(modifier=Modifier.padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .clickable { focusRequester.requestFocus() }
                ) {
                TextField(
                    modifier=Modifier.padding(innerPadding)
                        .fillMaxSize()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    value=uiState.value,
                    onValueChange = viewModel::onValueChange,
                    singleLine = false,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}



@Preview
@Composable
fun ProjectDetailContentPreview() {
    VaniTheme {
        ProjectDetailContent(
            uiState = ProjectDetailUiState(
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