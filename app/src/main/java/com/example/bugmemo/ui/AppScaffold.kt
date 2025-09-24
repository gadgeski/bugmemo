// ----------------------------------------
// file: app/src/main/java/com/example/bugmemo/ui/AppScaffold.kt
// ----------------------------------------
package com.example.bugmemo.ui

import androidx.compose.foundation.layout.padding    // ★ これが必要
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bugmemo.ui.navigation.AppNavHost
import com.example.bugmemo.ui.navigation.Dest
import com.example.bugmemo.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(vm: NotesViewModel = viewModel()) {
    val nav = rememberNavController()
    val backstack by nav.currentBackStackEntryAsState()
    val current = backstack?.destination?.route

    val tabs = listOf(Dest.Bugs, Dest.Folders, Dest.MindMap, Dest.Search)

    Scaffold(
        topBar = { TopAppBar(title = { Text("BugMemo") }) },
        bottomBar = {
            NavigationBar {
                tabs.forEach { item ->
                    NavigationBarItem(
                        selected = current?.startsWith(item.route) == true,
                        onClick = { NavRoutes.navigateSingleTop(nav, item.route) },
                        icon = {},
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // ★ 修正ポイント：PaddingValues → Modifier に変換して渡す
        AppNavHost(
            nav = nav,
            vm = vm,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
