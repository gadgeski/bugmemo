// app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bugmemo.ui.navigation.AppNavHost
import com.example.bugmemo.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppScaffold(
    // ★ Changed: VM を必須引数に。内部で viewModel() しないことで“二重インスタンス”を防止
    // ★ Changed: 呼び出し側（MainActivity 等）で factory 済みの同一 VM を渡してください
    vm: NotesViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    // ★ keep: Snackbar（Message / UndoDelete）を受ける
    LaunchedEffect(Unit) {
        vm.events.collectLatest { e ->
            when (e) {
                is NotesViewModel.UiEvent.Message -> {
                    snackbarHostState.showSnackbar(
                        message = e.text,
                        withDismissAction = true,
                    )
                }
                is NotesViewModel.UiEvent.UndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        // ★ keep: タイトル非依存の文言で安全側に
                        message = "削除しました",
                        actionLabel = "取り消す",
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.undoDelete()
                    }
                }
            }
        }
    }

    val navItems = listOf(
        NavItem("Bugs",    Routes.BUGS)    { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs") },
        NavItem("Search",  Routes.SEARCH)  { Icon(Icons.Filled.Search, contentDescription = "Search") },
        NavItem("Folders", Routes.FOLDERS) { Icon(Icons.Filled.Folder, contentDescription = "Folders") },
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        icon = { item.icon() },
                        label = { Text(item.label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        // ★ Added: imePadding を付与してキーボード表示時の安全マージンを確保
        // ★ keep : innerPadding は Nav 側へ伝搬
        AppNavHost(
            navController = navController,
            vm = vm,
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
            // 上が追加したモノ
        )
    }
}

/** ボトムバー用モデル */
private data class NavItem(
    val label: String,
    val route: String,
    val icon: @Composable () -> Unit
)
