// app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bugmemo.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bugmemo.ui.navigation.AppNavHost
import com.example.bugmemo.ui.navigation.Routes

@Composable
fun AppScaffold(
    vm: NotesViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    // （任意）VM から UI メッセージ Flow を受け取って Snackbar 表示したい場合はここで
    // LaunchedEffect(Unit) {
    //     vm.uiMessages.collect { msg ->
    //         snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
    //     }
    // }

    val navItems = listOf(
        NavItem("Bugs",    Routes.BUGS)    { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs") },
        NavItem("Search",  Routes.SEARCH)  { Icon(Icons.Filled.Search, contentDescription = "Search") },
        NavItem("Folders", Routes.FOLDERS) { Icon(Icons.Filled.Folder, contentDescription = "Folders") }
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
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // ★ Changed: onShowMessage を渡すのをやめ、既存の引数だけで呼ぶ
        AppNavHost(
            navController = navController,
            vm = vm,
            modifier = Modifier.padding(innerPadding)   // ★ keep: padding を Nav 側に伝搬
        )
    }
}

/** ボトムバー用モデル */
private data class NavItem(
    val label: String,
    val route: String,
    val icon: @Composable () -> Unit
)
