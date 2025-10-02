// app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
package com.example.bugmemo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List   // ★ Added: AutoMirrored の List（import 名はそのまま）
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier                                  // ★ Added: Modifier の import
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bugmemo.ui.navigation.AppNavHost
import com.example.bugmemo.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    vm: NotesViewModel = viewModel()
) {
    val nav = rememberNavController()
    val backstack by nav.currentBackStackEntryAsState()
    val currentRoute = backstack?.destination?.route

    // ★ Changed: Icons.AutoMirrored.Filled.List を直接使用（衝突/未解決を回避）
    val tabs = listOf(
        TabSpec(route = NavRoutes.BUGS,    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs") }),
        TabSpec(route = NavRoutes.SEARCH,  icon = { Icon(Icons.Filled.Search,            contentDescription = "Search") }),
        TabSpec(route = NavRoutes.FOLDERS, icon = { Icon(Icons.Filled.Folder,            contentDescription = "Folders") }),
    )

    Scaffold(
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
        // bottomBar の分の余白を適用
        Box(Modifier.padding(inner)) {
            AppNavHost(
                nav = nav,
                vm = vm,
                startDestination = NavRoutes.BUGS
            )
        }
    }
}

private data class TabSpec(
    val route: String,
    val icon: @Composable () -> Unit
)
