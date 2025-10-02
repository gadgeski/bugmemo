// app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class) // ★ Added: Material3 の実験的APIを許可

package com.example.bugmemo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppScaffold(
    vm: NotesViewModel = viewModel(
        factory = NotesViewModel.factory()
    )
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModel のイベントを受け取り、Snackbar（Undo 付き）を表示
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is NotesViewModel.UiEvent.Message -> {
                    snackbarHostState.showSnackbar(ev.text)
                }
                is NotesViewModel.UiEvent.UndoDelete -> {
                    val res = snackbarHostState.showSnackbar(
                        message = ev.text,
                        actionLabel = "元に戻す",
                        withDismissAction = true
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        vm.undoDelete()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("BugMemo") })
        }
        // bottomBar = { … 既存の BottomNavigation があれば戻してください … }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            // ここに既存のコンテンツ / NavHost を戻してください
            // 例）AppNavHost(...)
        }
    }
}
