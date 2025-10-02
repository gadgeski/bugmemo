// app/src/main/java/com/example/bugmemo/ui/navigation/Nav.kt
package com.example.bugmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bugmemo.ui.NotesViewModel
import com.example.bugmemo.ui.screens.BugsScreen
import com.example.bugmemo.ui.screens.FoldersScreen
import com.example.bugmemo.ui.screens.NoteEditorScreen
import com.example.bugmemo.ui.screens.SearchScreen

object NavRoutes {
    const val BUGS = "bugs"
    const val EDITOR = "editor"
    const val SEARCH = "search"
    const val FOLDERS = "folders"                         // ★ Added
}

@Composable
fun AppNavHost(
    nav: NavHostController,
    vm: NotesViewModel,
    startDestination: String = NavRoutes.BUGS
) {
    NavHost(navController = nav, startDestination = startDestination) {

        composable(NavRoutes.BUGS) {
            BugsScreen(
                vm = vm,
                onOpenEditor = { nav.navigate(NavRoutes.EDITOR) },
                onOpenSearch = { nav.navigate(NavRoutes.SEARCH) }
                // ※ Folders への導線は後で Bugs のTopBarやBottomNavに追加予定
                // 例: onOpenFolders = { nav.navigate(NavRoutes.FOLDERS) }
            )
        }

        composable(NavRoutes.EDITOR) {
            NoteEditorScreen(
                vm = vm,
                onClose = { nav.popBackStack() }
            )
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                vm = vm,
                onClose = { nav.popBackStack() }
            )
        }

        composable(NavRoutes.FOLDERS) {                     // ★ Added
            FoldersScreen(
                vm = vm,
                onClose = { nav.popBackStack() }            // ★ Back で戻る
            )
        }
    }
}
