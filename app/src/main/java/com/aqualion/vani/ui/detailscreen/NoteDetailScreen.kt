package com.aqualion.vani.ui.detailscreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aqualion.vani.ui.NoteUiModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteDialog(
    viewModel: NoteViewModel = hiltViewModel(), selectedNote: NoteUiModel?,
    onSaved: () -> Unit = {}, onDismiss: () -> Unit, onError: (String) -> Unit
) {
    if (selectedNote == null) {
        onError("No note selected")
        onDismiss()
        return
    }

    val uiState by viewModel.noteUiState.collectAsStateWithLifecycle()
    val isEdited by viewModel.isEdited.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.initEditState()
        viewModel.onInit(selectedNote.id)
    }

    Dialog(
        onDismissRequest = {
            if (isEdited) {
                viewModel.onSave()
                onSaved()
            }
            onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        NoteDetailContent(
            uiState=uiState,
            onNameEdited = viewModel::onNameChanged,
            onValueChange = viewModel::onValueChange,
            onError = viewModel::onError,
            isEdited = isEdited
        )
    }

    when(uiState.errorMessage) {
        null -> {}
        else -> {
            ErrorDialog(
                message=uiState.errorMessage ?: "Unknown error",
                onConfirm = {
                    viewModel.initError()
                    onDismiss()
                },
                onDismiss = { viewModel.initError() }
            )
        }
    }
}

@Composable
fun NoteDetailContent(uiState: NoteUiState?, isEdited: Boolean, onNameEdited: (String) -> Unit, onValueChange: (String) -> Unit, onError: (String) -> Unit = {}) {
    if (uiState == null) {
        onError("Note is not set")
        return
    }
    Scaffold(
        topBar = { NameTopAppBar(modifier=Modifier, name = uiState.name, size=18, onNameEdited = onNameEdited) },
    ) {innerPadding ->
        val focusRequester = remember { FocusRequester() }
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        Column(
            modifier=Modifier.padding(innerPadding)
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
                            coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                        }
                    },
                value=uiState.value,
                onValueChange = onValueChange,
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            if (isEdited) {
                Text(
                    text = "※編集中",
                    modifier = Modifier.wrapContentHeight(),
                    overflow = TextOverflow.StartEllipsis
                )
            }
        }
    }
}