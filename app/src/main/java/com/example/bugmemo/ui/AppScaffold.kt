// app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
package com.example.bugmemo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding                         // ★ Added: inner を消費
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List        // ★ Added: AutoMirrored List アイコン
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map                      // MindMap 代替
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost                         // ★ Added: SnackbarHost
import androidx.compose.material3.SnackbarHostState                    // ★ Added
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect                          // ★ Added: collectLatest 用
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember                                // ★ Added
import androidx.compose.ui.Modifier                                     // ★ Added: Modifier を使用
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bugmemo.ui.navigation.AppNavHost
import com.example.bugmemo.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest                            // ★ Added: イベント購読

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    vm: NotesViewModel = viewModel()
) {
    val nav = rememberNavController()
    val backstack by nav.currentBackStackEntryAsState()
    val currentRoute = backstack?.destination?.route

    // ★ Added: Snackbar の状態を 1 箇所で管理
    val snackbarHostState = remember { SnackbarHostState() }

    // ★ Added: Tabs（Bugs / Search / Folders / MindMap）
    val tabs = listOf(
        TabSpec(NavRoutes.BUGS)    { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs") },
        TabSpec(NavRoutes.SEARCH)  { Icon(Icons.Filled.Search,            contentDescription = "Search") },
        TabSpec(NavRoutes.FOLDERS) { Icon(Icons.Filled.Folder,            contentDescription = "Folders") },
        TabSpec(NavRoutes.MINDMAP) { Icon(Icons.Filled.Map,               contentDescription = "MindMap") }
    )

    Scaffold(
        // ★ Added: SnackbarHost をスキャフォールドに配置
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                nav.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(NavRoutes.BUGS) { saveState = true }
                                }
                            }
                        },
                        icon = { tab.icon() }
                    )
                }
            }
        }
    ) { inner ->
        // ★ Added: ViewModel の UI イベントを購読してスナックバー表示
        LaunchedEffect(Unit) {
            vm.events.collectLatest { e ->
                when (e) {
                    is NotesViewModel.UiEvent.Message ->
                        snackbarHostState.showSnackbar(e.text)
                }
            }
        }

        // ★ Changed: inner を適用して、ボトムバーとの重なりを回避
        Box(Modifier.padding(inner)) {
            AppNavHost(
                nav = nav,
                vm = vm,
                startDestination = NavRoutes.BUGS
            )
        }
    }
}

// タブ定義（小さなヘルパー）
private data class TabSpec(
    val route: String,
    val icon: @Composable () -> Unit
)
