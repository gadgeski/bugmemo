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
    // ★ keep: 呼び出し側で生成した VM を受け取る
    vm: NotesViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

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
        NavItem("Bugs", Routes.BUGS) {
            // ★ Fixed: カンマ後の余計なスペースを削除（1スペースに）
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs")
        },
        NavItem("Search", Routes.SEARCH) {
            // ★ Fixed: 同上
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        NavItem("Folders", Routes.FOLDERS) {
            Icon(Icons.Filled.Folder, contentDescription = "Folders")
        },
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
        },
        // ★ Added: bottomBar 引数の後ろにトレーリングカンマを追加（マルチライン呼び出し推奨）
    ) { innerPadding ->
        // ★ keep: imePadding でキーボード重なりを回避
        AppNavHost(
            navController = navController,
            vm = vm,
            modifier = Modifier
                .padding(innerPadding)
                .imePadding(),
        )
    }
}

/** ボトムバー用モデル */
private data class NavItem(
    val label: String,
    val route: String,
    val icon: @Composable () -> Unit,
)
